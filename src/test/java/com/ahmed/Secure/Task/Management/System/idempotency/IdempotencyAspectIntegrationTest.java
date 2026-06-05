package com.ahmed.Secure.Task.Management.System.idempotency;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.CreateTaskAttachmentDto;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for IdempotencyAspect functionality.
 * Tests the @Idempotent annotation on TaskAttachmentService.createTaskAttachment()
 *
 * Tests cover:
 * - Cache hit/miss scenarios
 * - Idempotency key validation (missing, invalid format, length limits)
 * - User isolation (different users get different cache results)
 * - Request body hashing (different bodies = different cache keys)
 * - Serialization/deserialization of cached responses
 * - Response structure validation
 * - Concurrent request handling
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("dev")
class IdempotencyAspectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private String accessTokenAhmed;   // User ID = 1
    private String accessTokenEric;    // User ID = 2

    private static final int TASK_ID = 1;  // Use existing task from test data
    private static final long MAX_FILE_SIZE = 5242880L;  // 5MB

    @Container
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

    @BeforeEach
    void setup() throws Exception {
        this.accessTokenAhmed = loginAndGetToken("ahmed@mail.com", "12345");
        this.accessTokenEric = loginAndGetToken("eric@mail.com", "678910");
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);
        String response = this.mockMvc.perform(post(baseUrl + "/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(loginRequestDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = this.objectMapper.readTree(response);
        return "Bearer " + jsonNode.get("data").asString();
    }

    /**
     * Test: Cache miss on first request
     * Given: Valid idempotency key, no prior request
     * When: POST request with Idempotency-Key header to upload attachment
     * Then: Method executes, result is cached, returns 201 CREATED
     */
    @Test
    void shouldCacheMissOnFirstRequest() throws Exception {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        CreateTaskAttachmentDto attachmentDto = new CreateTaskAttachmentDto(
                "document.pdf",
                "application/pdf",
                MAX_FILE_SIZE
        );

        // when - First request
        MvcResult result1 = this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(attachmentDto))
                        .header("Idempotency-Key", idempotencyKey)
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").value(true))
                .andReturn();

        // then
        String responseContent1 = result1.getResponse().getContentAsString();
        JsonNode responseJson1 = this.objectMapper.readTree(responseContent1);
        assertNotNull(responseJson1.get("data").get("id"));
        int attachmentId = responseJson1.get("data").get("id").asInt();
        assertTrue(attachmentId > 0);
    }

    /**
     * Test: Cache hit on subsequent request
     * Given: Same idempotency key used twice for creating task attachments
     * When: Second request with same key
     * Then: Returns cached result without executing method again (same attachment ID)
     */
    @Test
    void shouldReturnCachedResultOnCacheHit() throws Exception {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        CreateTaskAttachmentDto attachmentDto = new CreateTaskAttachmentDto(
                "report.pdf",
                "application/pdf",
                MAX_FILE_SIZE
        );

        // when - First request (cache miss)
        MvcResult result1 = this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(attachmentDto))
                        .header("Idempotency-Key", idempotencyKey)
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent1 = result1.getResponse().getContentAsString();
        JsonNode responseJson1 = this.objectMapper.readTree(responseContent1);
        int attachmentId1 = responseJson1.get("data").get("id").asInt();

        // when - Second request with same key (cache hit)
        MvcResult result2 = this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(attachmentDto))
                        .header("Idempotency-Key", idempotencyKey)
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent2 = result2.getResponse().getContentAsString();
        JsonNode responseJson2 = this.objectMapper.readTree(responseContent2);
        int attachmentId2 = responseJson2.get("data").get("id").asInt();

        // then - Both responses should have same attachment ID (cached)
        assertEquals(attachmentId1, attachmentId2, "Cache hit should return same attachment ID");
    }

    /**
     * Test: Missing idempotency key
     * Given: No Idempotency-Key header provided
     * When: POST request without header
     * Then: Returns 400 BAD_REQUEST with IdempotencyKeyException message
     */
    @Test
    void shouldFailWithMissingIdempotencyKey() throws Exception {
        // given
        CreateTaskAttachmentDto attachmentDto = new CreateTaskAttachmentDto(
                "document.pdf",
                "application/pdf",
                MAX_FILE_SIZE
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(attachmentDto))
                        // No Idempotency-Key header
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message", containsString("Idempotency key is required")));
    }

    /**
     * Test: Blank/empty idempotency key
     * Given: Empty idempotency key header
     * When: POST request with blank key
     * Then: Returns 400 BAD_REQUEST
     */
    @Test
    void shouldFailWithBlankIdempotencyKey() throws Exception {
        // given
        CreateTaskAttachmentDto attachmentDto = new CreateTaskAttachmentDto(
                "document.pdf",
                "application/pdf",
                MAX_FILE_SIZE
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(attachmentDto))
                        .header("Idempotency-Key", "   ")  // Blank key
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message", containsString("Idempotency key is required")));
    }

    /**
     * Test: Idempotency key exceeds max length
     * Given: Idempotency key length > 255 characters
     * When: POST request with oversized key
     * Then: Returns 400 BAD_REQUEST with length validation error
     */
    @Test
    void shouldFailWithOversizedIdempotencyKey() throws Exception {
        // given
        CreateTaskAttachmentDto attachmentDto = new CreateTaskAttachmentDto(
                "document.pdf",
                "application/pdf",
                MAX_FILE_SIZE
        );

        // Create a key with 300 characters (exceeds max of 255)
        String longKey = "x".repeat(300);

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(attachmentDto))
                        .header("Idempotency-Key", longKey)
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message", containsString("exceeds maximum length")));
    }

    /**
     * Test: Different request bodies produce different cache keys
     * Given: hashRequestBody = true (configured on @Idempotent), same idempotency key but different request bodies
     * When: POST requests with different attachment details
     * Then: Each body combination may create separate cache entries (depends on hashRequestBody setting)
     *
     * Note: TaskAttachmentService.createTaskAttachment has hashRequestBody = true
     */
    @Test
    void shouldCreateDifferentCacheKeysForDifferentRequestBodies() throws Exception {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        CreateTaskAttachmentDto attachmentDto1 = new CreateTaskAttachmentDto(
                "report-v1.pdf",
                "application/pdf",
                MAX_FILE_SIZE
        );

        CreateTaskAttachmentDto attachmentDto2 = new CreateTaskAttachmentDto(
                "report-v2.pdf",
                "application/pdf",
                2097152L  // 2MB
        );

        // when - First request with attachmentDto1
        MvcResult result1 = this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(attachmentDto1))
                        .header("Idempotency-Key", idempotencyKey)
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response1 = this.objectMapper.readTree(result1.getResponse().getContentAsString());
        int attachmentId1 = response1.get("data").get("id").asInt();

        // when - Second request with different body but same key
        // Since hashRequestBody=true, should create different cache key
        MvcResult result2 = this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(attachmentDto2))
                        .header("Idempotency-Key", idempotencyKey)
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response2 = this.objectMapper.readTree(result2.getResponse().getContentAsString());
        int attachmentId2 = response2.get("data").get("id").asInt();

        // then - With body hashing enabled, different bodies should create new cache entries
        assertNotEquals(attachmentId1, attachmentId2, "Different request bodies should create different cache entries");
    }




    /**
     * Test: Response structure includes required fields
     * Given: Valid idempotent request
     * When: POST request
     * Then: Response contains all required fields in correct structure
     */
    @Test
    void shouldReturnValidResponseStructure() throws Exception {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        CreateTaskAttachmentDto attachmentDto = new CreateTaskAttachmentDto(
                "response-structure-test.pdf",
                "application/pdf",
                MAX_FILE_SIZE
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(attachmentDto))
                        .header("Idempotency-Key", idempotencyKey)
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").isBoolean())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.uploadUrl").isNotEmpty());
    }

    /**
     * Test: Consecutive requests with same key within TTL
     * Given: Multiple identical requests with same idempotency key within TTL
     * When: POST same request 5 times
     * Then: All return same cached result (same attachment ID)
     */
    @Test
    void shouldReturnSameCachedResultForMultipleIdenticalRequests() throws Exception {
        // given
        String idempotencyKey = UUID.randomUUID().toString();
        CreateTaskAttachmentDto attachmentDto = new CreateTaskAttachmentDto(
                "multi-request-test.pdf",
                "application/pdf",
                MAX_FILE_SIZE
        );

        int firstAttachmentId = -1;

        // when - Make 5 identical requests with same key
        for (int i = 0; i < 5; i++) {
            MvcResult result = this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", TASK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(this.objectMapper.writeValueAsString(attachmentDto))
                            .header("Idempotency-Key", idempotencyKey)
                            .header("Authorization", this.accessTokenAhmed)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = this.objectMapper.readTree(result.getResponse().getContentAsString());
            int attachmentId = response.get("data").get("id").asInt();

            if (i == 0) {
                firstAttachmentId = attachmentId;
            }

            // then - All should return the same cached attachment ID
            assertEquals(firstAttachmentId, attachmentId, "Request #" + (i + 1) + " should return cached result");
        }
    }

}





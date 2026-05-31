package com.ahmed.Secure.Task.Management.System.taskAttachment;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.ahmed.Secure.Task.Management.System.client.fileStorage.FileStorageClient;
import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.CreateTaskAttachmentDto;
import com.ahmed.Secure.Task.Management.System.user.User;
import com.redis.testcontainers.RedisContainer;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@ActiveProfiles("dev")
class TaskAttachmentControllerIntegrationTest {

    @Autowired
     MockMvc mockMvc;

    @Autowired
     ObjectMapper objectMapper;

    @Autowired
     TaskAttachmentRepository taskAttachmentRepository;

    @Autowired
    EntityManager entityManager;


    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private String accessTokenAhmed;  // Task creator/owner (user id = 1)
    private String accessTokenEric;   // Different user (user id = 2)

    @Container
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

    @BeforeEach
    void setup() throws Exception {
        this.accessTokenAhmed = loginAndGetToken("ahmed@mail.com", "12345");
        this.accessTokenEric = loginAndGetToken("eric@mail.com", "678910");

        // Clear existing attachments
        taskAttachmentRepository.deleteAll();
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
     * Test: Generate upload URL successfully
     * Given: Valid task and attachment metadata
     * When: POST /api/v1/tasks/{taskId}/attachments/upload
     * Then: Returns 201 CREATED with presigned URL and attachment details
     */
    @Test
    void shouldGenerateUploadUrlSuccess() throws Exception {
        // given
        int taskId = 1;  // Task owned by Ahmed (from test data)
        CreateTaskAttachmentDto createTaskAttachmentDto = new CreateTaskAttachmentDto(
                "report.pdf",
                "application/pdf",
                5242880L  // 5MB
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createTaskAttachmentDto))
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Upload URL generated successfully"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.uploadUrl").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value(TaskAttachmentStatus.PENDING.toString()))
                .andExpect(jsonPath("$.data.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.data.maxFileSizeBytes").isNumber());

        // Verify attachment metadata saved in database
        long count = taskAttachmentRepository.count();
        assert count > 0;
    }

    /**
     * Test: Generate upload URL - File size exceeds limit
     * Given: File size > maxFileSize
     * When: POST /api/v1/tasks/{taskId}/attachments/upload
     * Then: Returns 400 BAD_REQUEST with FileValidationException
     */
    @Test
    void shouldGenerateUploadUrlFileSizeExceedsFail() throws Exception {
        // given
        int taskId = 1;
        CreateTaskAttachmentDto createTaskAttachmentDto = new CreateTaskAttachmentDto(
                "large-file.pdf",
                "application/pdf",
                1099511627776L  // 1TB (exceeds limit)
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createTaskAttachmentDto))
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message", containsString("Invalid input check data")));
    }

    /**
     * Test: Generate upload URL - Unsupported file type
     * Given: File type not in allowedFileTypes
     * When: POST /api/v1/tasks/{taskId}/attachments/upload
     * Then: Returns 400 BAD_REQUEST with FileValidationException
     */
    @Test
    void shouldGenerateUploadUrlUnsupportedFileTypeFail() throws Exception {
        // given
        int taskId = 1;
        CreateTaskAttachmentDto createTaskAttachmentDto = new CreateTaskAttachmentDto(
                "script.exe",
                "application/octet-stream",  // Unsupported
                1024000L
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createTaskAttachmentDto))
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message", containsString("Invalid input check data")));
    }

    /**
     * Test: Generate upload URL - Unsupported file extension
     * Given: File extension not in allowedFileExtensions
     * When: POST /api/v1/tasks/{taskId}/attachments/upload
     * Then: Returns 400 BAD_REQUEST with FileValidationException
     */
    @Test
    void shouldGenerateUploadUrlUnsupportedFileExtensionFail() throws Exception {
        // given
        int taskId = 1;
        CreateTaskAttachmentDto createTaskAttachmentDto = new CreateTaskAttachmentDto(
                "malware.bat",
                "application/x-msdownload",
                1024000L
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createTaskAttachmentDto))
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message", containsString("Invalid input check data")));
    }

    /**
     * Test: Generate upload URL - Task not found
     * Given: Task with id 999 does not exist
     * When: POST /api/v1/tasks/{taskId}/attachments/upload
     * Then: Returns 404 NOT_FOUND
     */
    @Test
    void shouldGenerateUploadUrlTaskNotFoundFail() throws Exception {
        // given
        int taskId = 999;
        CreateTaskAttachmentDto createTaskAttachmentDto = new CreateTaskAttachmentDto(
                "report.pdf",
                "application/pdf",
                5242880L
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createTaskAttachmentDto))
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message", containsString("task")));
    }

    /**
     * Test: Generate upload URL - Unauthorized (not task owner)
     * Given: Different user (Eric) tries to generate URL for Ahmed's task
     * When: POST /api/v1/tasks/{taskId}/attachments/upload
     * Then: Returns 403 FORBIDDEN
     */
    @Test
    void shouldGenerateUploadUrlUnauthorizedFail() throws Exception {
        // given
        int taskId = 3;  // Task owned by Ahmed
        CreateTaskAttachmentDto createTaskAttachmentDto = new CreateTaskAttachmentDto(
                "report.pdf",
                "application/pdf",
                5242880L
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createTaskAttachmentDto))
                        .header("Authorization", this.accessTokenEric)  // Different user
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message", containsString("permission")));
    }

    /**
     * Test: Generate upload URL - No authentication
     * Given: No authorization header
     * When: POST /api/v1/tasks/{taskId}/attachments/upload
     * Then: Returns 401 UNAUTHORIZED
     */
    @Test
    void shouldGenerateUploadUrlNoAuthFail() throws Exception {
        // given
        int taskId = 1;
        CreateTaskAttachmentDto createTaskAttachmentDto = new CreateTaskAttachmentDto(
                "report.pdf",
                "application/pdf",
                5242880L
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createTaskAttachmentDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: Generate upload URL - Missing required fields
     * Given: CreateTaskAttachmentDto with null/blank fields
     * When: POST /api/v1/tasks/{taskId}/attachments/upload
     * Then: Returns 400 BAD_REQUEST with validation error
     */
    @Test
    void shouldGenerateUploadUrlMissingFieldsFail() throws Exception {
        // given
        int taskId = 1;
        CreateTaskAttachmentDto createTaskAttachmentDto = new CreateTaskAttachmentDto(
                "",  // Empty filename
                "application/pdf",
                5242880L
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createTaskAttachmentDto))
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false));
    }

    /**
     * Test: Generate upload URL - Multiple attachments for same task
     * Given: Task already has attachments
     * When: Generate another upload URL for same task
     * Then: Returns 201 CREATED with new attachment (if under limit)
     */
    @Test
    void shouldGenerateMultipleUploadUrlsForSameTaskSuccess() throws Exception {
        // given
        int taskId = 1;

        CreateTaskAttachmentDto attachment1 = new CreateTaskAttachmentDto(
                "report1.pdf",
                "application/pdf",
                5242880L
        );

        CreateTaskAttachmentDto attachment2 = new CreateTaskAttachmentDto(
                "report2.pdf",
                "application/pdf",
                3145728L
        );

        // when then - First upload URL
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(attachment1))
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber());

        // when then - Second upload URL
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(attachment2))
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.status").value(TaskAttachmentStatus.PENDING.toString()));

        // Verify both attachments saved
        long count = taskAttachmentRepository.count();
        assert count == 2;
    }

    /**
     * Test: Presigned URL expiration time
     * Given: Upload URL generated
     * When: Verify expiresAt value
     * Then: Expiration time is set correctly (15 minutes from now)
     */
    @Test
    void shouldVerifyUploadUrlExpirationTime() throws Exception {
        // given
        int taskId = 1;
        Instant beforeRequest = Instant.now();

        CreateTaskAttachmentDto createTaskAttachmentDto = new CreateTaskAttachmentDto(
                "report.pdf",
                "application/pdf",
                5242880L
        );

        // when then
        String response = this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createTaskAttachmentDto))
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = this.objectMapper.readTree(response);
        String expiresAtStr = jsonNode.get("data").get("expiresAt").asText();
        Instant expiresAt = Instant.parse(expiresAtStr);

        // Verify expiration is ~15 minutes from request time
        Instant expectedExpiration = beforeRequest.plusSeconds(900);  // 15 minutes
        assert expiresAt.isAfter(beforeRequest);
        assert expiresAt.isBefore(expectedExpiration.plusSeconds(60));  // Allow 60s tolerance
    }

    /**
     * Test: Verify attachment metadata persisted correctly
     * Given: Upload URL generated
     * When: Query database for attachment
     * Then: All fields stored correctly (status=PENDING, columns match)
     */
    @Test
    void shouldVerifyAttachmentMetadataPersistedCorrectly() throws Exception {
        // given
        int taskId = 1;
        String fileName = "important.pdf";
        String fileType = "application/pdf";
        Long fileSize = 5242880L;

        CreateTaskAttachmentDto createTaskAttachmentDto = new CreateTaskAttachmentDto(
                fileName,
                fileType,
                fileSize
        );

        // when
        String response = this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createTaskAttachmentDto))
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = this.objectMapper.readTree(response);
        int attachmentId = jsonNode.get("data").get("id").asInt();

        // then - Verify in database
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId).orElseThrow();
        assert attachment.getOriginalFileName().equals(fileName);
        assert attachment.getType().equals(fileType);
        assert attachment.getSize().equals(fileSize);
        assert attachment.getStatus().equals(TaskAttachmentStatus.PENDING);
        assert attachment.getTask().getId() == taskId;
        assert attachment.getExpiresAt().isAfter(Instant.now());
    }

    /**
     * Test: Response structure validation
     * Given: Valid request
     * When: Generate upload URL
     * Then: Response follows standard Response<UploadUrlResponseDto> structure
     */
    @Test
    void shouldVerifyResponseStructure() throws Exception {
        // given
        int taskId = 1;
        CreateTaskAttachmentDto createTaskAttachmentDto = new CreateTaskAttachmentDto(
                "report.pdf",
                "application/pdf",
                5242880L
        );

        // when then
        this.mockMvc.perform(post(baseUrl + "/tasks/{taskId}/attachments/upload", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createTaskAttachmentDto))
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").isBoolean())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.uploadUrl").isNotEmpty())
                .andExpect(jsonPath("$.data.status").isString())
                .andExpect(jsonPath("$.data.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.data.maxFileSizeBytes").isNumber());
    }

    @Test
    void shouldGenerateDownloadUrlSuccess() throws Exception {
        TaskAttachment taskAttachment = TaskAttachment
                .builder()
                .createdBy(this.entityManager.getReference(User.class,1))
                .type("image")
                .originalFileName("testFile.txt")
                .size(1024L)
                .objectKey("testObjectKey")
                .status(TaskAttachmentStatus.COMPLETED)
                .task(this.entityManager.getReference(Task.class,1))
                .expiresAt(Instant.now().plusSeconds(900))
                .build();



        TaskAttachment savedTask = this.taskAttachmentRepository.save(taskAttachment);
        //given
        int taskId = 1;
        int attachmentId = taskAttachment.getId();

        //when then
        this.mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/tasks/{taskId}/attachments/{attachmentId}/download", taskId, attachmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", this.accessTokenAhmed)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").isBoolean())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Download URL generated successfully"))
                .andExpect(jsonPath("data.url").isString())
                .andExpect(jsonPath("$.data.expiresAt").isString());
    }
}
package com.ahmed.Secure.Task.Management.System.auth.refreshToken;


import com.ahmed.Secure.Task.Management.System.auth.config.SecurityProps;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class HashService {

    private final SecretKey secretKey;


    public HashService(SecurityProps props) {
       byte[] key = props.refreshTokenHmacSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = new SecretKeySpec(key, "HmacSHA256");
    }


    public String hash(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(this.secretKey);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException  e) {
            throw new RuntimeException("Failed to hash data: " + e.getMessage(), e);

        }

    }


}

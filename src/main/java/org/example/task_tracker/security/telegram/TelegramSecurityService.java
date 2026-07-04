package org.example.task_tracker.security.telegram;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class TelegramSecurityService {

    @Value("${bot.secret}")
    private String secretKey;


    private SecretKey getSigningKey(String key) {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean signatureIsValid(String dataToSign, String providedSignature) {
        try {

            SecretKey secretKey = getSigningKey(this.secretKey);

            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            sha512Hmac.init(secretKey);

            byte[] hashBytes = sha512Hmac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(hashBytes).equals(providedSignature);

        } catch (Exception e) {
            log.error("Произошла ошибка при проверке HMAC подписи для Telegram: {}", e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

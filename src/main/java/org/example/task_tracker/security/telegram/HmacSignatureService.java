package org.example.task_tracker.security.telegram;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example.task_tracker.security.DTO.social.signable.Signable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HmacSignatureService {

    @Value("${bot.secret}")
    private String secretKey;


    private SecretKey getSigningKey(String key) {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean signatureIsValid(Signable request) {
        try {
            String dataToSign = toCanonicalString(request);
            SecretKey secretKey = getSigningKey(this.secretKey);

            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            sha512Hmac.init(secretKey);

            byte[] hashBytes = sha512Hmac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(hashBytes).equals(request.getSignature());

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

    private String toCanonicalString(Signable signable) {
        List<Object> listOfFields = signable.getSignableFields();

        return listOfFields.stream()
                .map(field -> field.toString())
                .collect(Collectors.joining("|"));
    }
}

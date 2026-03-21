package com.tuniway.connect.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.time.Instant;

@Service
public class TotpService {
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    public boolean isValidCode(String base32Secret, String code) {
        if (base32Secret == null || base32Secret.isBlank()) {
            return false;
        }

        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }

        byte[] secret = decodeBase32(base32Secret);
        long nowCounter = Instant.now().getEpochSecond() / 30L;

        // Accept one time-step drift on each side to reduce false negatives.
        for (long i = -1; i <= 1; i++) {
            String generated = generateCode(secret, nowCounter + i);
            if (code.equals(generated)) {
                return true;
            }
        }

        return false;
    }

    private String generateCode(byte[] secret, long counter) {
        try {
            byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash = mac.doFinal(counterBytes);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate TOTP", e);
        }
    }

    private byte[] decodeBase32(String input) {
        String normalized = input.replace("=", "").replaceAll("\\s+", "").toUpperCase();
        int outputLength = normalized.length() * 5 / 8;
        byte[] result = new byte[outputLength];

        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;

        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            int value = BASE32_ALPHABET.indexOf(c);
            if (value < 0) {
                throw new RuntimeException("Invalid Base32 secret format");
            }

            buffer = (buffer << 5) | value;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                result[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }

        return result;
    }
}
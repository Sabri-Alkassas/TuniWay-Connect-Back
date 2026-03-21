package com.tuniway.connect.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TwoFactorChallengeService {
    private final ConcurrentHashMap<String, PendingChallenge> challenges = new ConcurrentHashMap<>();

    public String createChallenge(UUID userId, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(ttl);
        challenges.put(token, new PendingChallenge(userId, expiresAt));
        return token;
    }

    public UUID consumeChallenge(String token) {
        PendingChallenge challenge = challenges.remove(token);
        if (challenge == null) {
            throw new RuntimeException("Invalid or expired 2FA challenge");
        }

        if (challenge.expiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("2FA challenge has expired");
        }

        return challenge.userId();
    }

    private record PendingChallenge(UUID userId, Instant expiresAt) {
    }
}
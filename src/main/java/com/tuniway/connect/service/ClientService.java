package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.ClientAccountResponse;
import com.tuniway.connect.model.dto.ClientDashboardResponse;
import com.tuniway.connect.model.dto.UpdateClientAccountRequest;
import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.ClientProfile;
import com.tuniway.connect.model.entity.Role;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.ClientProfileRepository;
import com.tuniway.connect.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ClientService {
    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;

    public ClientService(UserRepository userRepository, ClientProfileRepository clientProfileRepository) {
        this.userRepository = userRepository;
        this.clientProfileRepository = clientProfileRepository;
    }

    public ClientDashboardResponse getDashboard(UUID clientId) {
        User user = requireClientUser(clientId);
        ClientProfile profile = requireClientProfile(clientId);
        List<String> missingProfileFields = collectMissingProfileFields(profile);

        ClientDashboardResponse response = new ClientDashboardResponse();
        response.setSuccess(true);
        response.setMessage("Client dashboard retrieved successfully");
        response.setClientId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(profile.getUsername());
        response.setDisplayName(buildDisplayName(profile));
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setEmailVerified(user.getStatus() == AccountStatus.ACTIVE);
        response.setProfileComplete(missingProfileFields.isEmpty());
        response.setMissingProfileFields(missingProfileFields);
        return response;
    }

    public ClientAccountResponse getAccount(UUID clientId) {
        User user = requireClientUser(clientId);
        ClientProfile profile = requireClientProfile(clientId);
        return buildAccountResponse(user, profile, "Client account retrieved successfully");
    }

    @Transactional
    public ClientAccountResponse updateAccount(UUID clientId, UpdateClientAccountRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        User user = requireClientUser(clientId);
        ClientProfile profile = requireClientProfile(clientId);
        boolean hasChanges = false;

        String email = normalizeToNull(request.getEmail());
        if (email != null) {
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("A client account with this email already exists");
            }
            user.setEmail(email);
            hasChanges = true;
        }

        String password = normalizeToNull(request.getPassword_hash());
        if (password != null) {
            user.setPassword_hash(password);
            hasChanges = true;
        }

        String username = normalizeToNull(request.getUsername());
        if (username != null) {
            clientProfileRepository.findByUsername(username)
                .filter(existingProfile -> !existingProfile.getUserId().equals(clientId))
                .ifPresent(existingProfile -> {
                    throw new IllegalArgumentException("A client with this username already exists");
                });
            profile.setUsername(username);
            hasChanges = true;
        }

        String firstName = normalizeToNull(request.getFirstName());
        if (firstName != null) {
            profile.setFirstName(firstName);
            hasChanges = true;
        }

        String lastName = normalizeToNull(request.getLastName());
        if (lastName != null) {
            profile.setLastName(lastName);
            hasChanges = true;
        }

        String phone = normalizeToNull(request.getPhone());
        if (phone != null) {
            profile.setPhone(phone);
            hasChanges = true;
        }

        if (request.getBirthDate() != null) {
            profile.setBirthDate(request.getBirthDate());
            hasChanges = true;
        }

        if (!hasChanges) {
            throw new IllegalArgumentException("At least one field is required");
        }

        userRepository.save(user);
        clientProfileRepository.save(profile);
        return buildAccountResponse(user, profile, "Client account updated successfully");
    }

    private User requireClientUser(UUID clientId) {
        User user = userRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client account not found"));

        if (user.getRole() != Role.CLIENT) {
            throw new IllegalArgumentException("The requested user is not a client account");
        }

        return user;
    }

    private ClientProfile requireClientProfile(UUID clientId) {
        return clientProfileRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client profile not found"));
    }

    private ClientAccountResponse buildAccountResponse(User user, ClientProfile profile, String message) {
        ClientAccountResponse response = new ClientAccountResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setUsername(profile.getUsername());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setPhone(profile.getPhone());
        response.setBirthDate(profile.getBirthDate());
        return response;
    }

    private String buildDisplayName(ClientProfile profile) {
        String firstName = normalizeToNull(profile.getFirstName());
        String lastName = normalizeToNull(profile.getLastName());

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        if (firstName != null) {
            return firstName;
        }
        if (lastName != null) {
            return lastName;
        }

        String username = normalizeToNull(profile.getUsername());
        return username != null ? username : "Client";
    }

    private List<String> collectMissingProfileFields(ClientProfile profile) {
        List<String> missingFields = new ArrayList<>();

        if (normalizeToNull(profile.getUsername()) == null) {
            missingFields.add("username");
        }
        if (normalizeToNull(profile.getFirstName()) == null) {
            missingFields.add("firstName");
        }
        if (normalizeToNull(profile.getLastName()) == null) {
            missingFields.add("lastName");
        }
        if (profile.getBirthDate() == null) {
            missingFields.add("birthDate");
        }

        return missingFields;
    }

    private String normalizeToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

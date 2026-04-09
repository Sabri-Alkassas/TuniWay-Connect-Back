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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientProfileRepository clientProfileRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void getDashboardBuildsClientSummaryFromPersistedData() {
        UUID clientId = UUID.randomUUID();
        User user = buildClientUser(clientId, "client@test.com");
        user.setLastLoginAt(Instant.parse("2026-04-09T10:15:30Z"));

        ClientProfile profile = new ClientProfile();
        profile.setUserId(clientId);
        profile.setUsername("clientuser");
        profile.setFirstName("Tuni");
        profile.setLastName("Way");

        when(userRepository.findById(clientId)).thenReturn(Optional.of(user));
        when(clientProfileRepository.findById(clientId)).thenReturn(Optional.of(profile));

        ClientDashboardResponse response = clientService.getDashboard(clientId);

        assertTrue(response.isSuccess());
        assertEquals(clientId, response.getClientId());
        assertEquals("Tuni Way", response.getDisplayName());
        assertFalse(response.isProfileComplete());
        assertEquals(1, response.getMissingProfileFields().size());
        assertEquals("birthDate", response.getMissingProfileFields().get(0));
    }

    @Test
    void updateAccountPersistsChangedFields() {
        UUID clientId = UUID.randomUUID();
        User user = buildClientUser(clientId, "old@test.com");
        ClientProfile profile = new ClientProfile();
        profile.setUserId(clientId);
        profile.setUsername("olduser");
        profile.setFirstName("Old");
        profile.setLastName("Name");

        UpdateClientAccountRequest request = new UpdateClientAccountRequest();
        request.setEmail("new@test.com");
        request.setUsername("newuser");
        request.setFirstName("New");
        request.setPhone("12345678");
        request.setBirthDate(Instant.parse("2000-01-01T00:00:00Z"));

        when(userRepository.findById(clientId)).thenReturn(Optional.of(user));
        when(clientProfileRepository.findById(clientId)).thenReturn(Optional.of(profile));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(clientProfileRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        when(clientProfileRepository.save(profile)).thenReturn(profile);

        ClientAccountResponse response = clientService.updateAccount(clientId, request);

        assertTrue(response.isSuccess());
        assertEquals("new@test.com", response.getEmail());
        assertEquals("newuser", response.getUsername());
        assertEquals("New", response.getFirstName());
        assertEquals("12345678", response.getPhone());
        assertEquals(Instant.parse("2000-01-01T00:00:00Z"), response.getBirthDate());
        verify(userRepository).save(user);
        verify(clientProfileRepository).save(profile);
    }

    @Test
    void updateAccountRejectsDuplicateUsernameOwnedByAnotherClient() {
        UUID clientId = UUID.randomUUID();
        User user = buildClientUser(clientId, "client@test.com");
        ClientProfile profile = new ClientProfile();
        profile.setUserId(clientId);
        profile.setUsername("currentuser");

        ClientProfile existingProfile = new ClientProfile();
        existingProfile.setUserId(UUID.randomUUID());
        existingProfile.setUsername("takenuser");

        UpdateClientAccountRequest request = new UpdateClientAccountRequest();
        request.setUsername("takenuser");

        when(userRepository.findById(clientId)).thenReturn(Optional.of(user));
        when(clientProfileRepository.findById(clientId)).thenReturn(Optional.of(profile));
        when(clientProfileRepository.findByUsername("takenuser")).thenReturn(Optional.of(existingProfile));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> clientService.updateAccount(clientId, request)
        );

        assertEquals("A client with this username already exists", exception.getMessage());
        verify(userRepository, never()).save(user);
        verify(clientProfileRepository, never()).save(profile);
    }

    private User buildClientUser(UUID clientId, String email) {
        User user = new User();
        user.setId(clientId);
        user.setEmail(email);
        user.setRole(Role.CLIENT);
        user.setStatus(AccountStatus.ACTIVE);
        return user;
    }
}

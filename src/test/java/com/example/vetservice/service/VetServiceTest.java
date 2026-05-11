package com.example.vetservice.service;

import com.example.vetservice.dto.VetRequest;
import com.example.vetservice.dto.VetResponse;
import com.example.vetservice.dto.VetUpdateRequest;
import com.example.vetservice.entity.Vet;
import com.example.vetservice.repository.VetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("VetService Unit Tests")
class VetServiceTest {

    @Mock
    private VetRepository vetRepository;

    @InjectMocks
    private VetService vetService;

    private UUID userId;
    private UUID vetId;
    private Vet testVet;
    private VetRequest vetRequest;
    private VetUpdateRequest vetUpdateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        vetId = UUID.randomUUID();

        testVet = new Vet();
        testVet.setId(vetId);
        testVet.setUserId(userId);
        testVet.setName("Veterinaria Central");
        testVet.setAddress("Calle Principal 123");
        testVet.setPhone("123456789");
        testVet.setLatitude(10.5);
        testVet.setLongitude(20.5);
        testVet.setImageUrl("http://example.com/image.jpg");
        testVet.setCreatedAt(LocalDateTime.now());
        testVet.setUpdatedAt(LocalDateTime.now());

        vetRequest = new VetRequest();
        vetRequest.setName("Nueva Veterinaria");
        vetRequest.setAddress("Calle Nueva 456");
        vetRequest.setPhone("987654321");
        vetRequest.setLatitude(15.5);
        vetRequest.setLongitude(25.5);
        vetRequest.setImageUrl("http://example.com/new-image.jpg");

        vetUpdateRequest = new VetUpdateRequest();
        vetUpdateRequest.setName("Veterinaria Actualizada");
        vetUpdateRequest.setPhone("555555555");
    }

    @Test
    @DisplayName("Should create a new vet successfully")
    void testCreateVet_Success() {
        when(vetRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(vetRepository.save(any(Vet.class))).thenReturn(testVet);

        VetResponse response = vetService.createVet(vetRequest, userId);

        assertNotNull(response);
        assertEquals("Veterinaria Central", response.getName());
        verify(vetRepository, times(1)).findByUserId(userId);
        verify(vetRepository, times(1)).save(any(Vet.class));
    }

    @Test
    @DisplayName("Should throw exception when user already has a vet")
    void testCreateVet_UserAlreadyHasVet() {
        when(vetRepository.findByUserId(userId)).thenReturn(Optional.of(testVet));

        assertThrows(RuntimeException.class, () -> vetService.createVet(vetRequest, userId));
        verify(vetRepository, times(1)).findByUserId(userId);
        verify(vetRepository, never()).save(any(Vet.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid latitude")
    void testCreateVet_InvalidLatitude() {
        vetRequest.setLatitude(95.0); // Invalid latitude

        assertThrows(IllegalArgumentException.class, () -> vetService.createVet(vetRequest, userId));
    }

    @Test
    @DisplayName("Should throw exception for invalid longitude")
    void testCreateVet_InvalidLongitude() {
        vetRequest.setLongitude(185.0); // Invalid longitude

        assertThrows(IllegalArgumentException.class, () -> vetService.createVet(vetRequest, userId));
    }

    @Test
    @DisplayName("Should update vet successfully")
    void testUpdateVet_Success() {
        when(vetRepository.findById(vetId)).thenReturn(Optional.of(testVet));
        when(vetRepository.save(any(Vet.class))).thenReturn(testVet);

        VetResponse response = vetService.updateVet(vetId, vetUpdateRequest, userId, false);

        assertNotNull(response);
        verify(vetRepository, times(1)).findById(vetId);
        verify(vetRepository, times(1)).save(any(Vet.class));
    }

    @Test
    @DisplayName("Should throw exception when vet not found for update")
    void testUpdateVet_NotFound() {
        when(vetRepository.findById(vetId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> vetService.updateVet(vetId, vetUpdateRequest, userId, false));
    }

    @Test
    @DisplayName("Should throw exception when user tries to update other user's vet")
    void testUpdateVet_AccessDenied() {
        Vet otherUserVet = new Vet();
        otherUserVet.setId(vetId);
        otherUserVet.setUserId(UUID.randomUUID()); // Different user

        when(vetRepository.findById(vetId)).thenReturn(Optional.of(otherUserVet));

        assertThrows(RuntimeException.class,
                () -> vetService.updateVet(vetId, vetUpdateRequest, userId, false));
    }

    @Test
    @DisplayName("Should allow admin to update any vet")
    void testUpdateVet_AdminAccess() {
        Vet otherUserVet = new Vet();
        otherUserVet.setId(vetId);
        otherUserVet.setUserId(UUID.randomUUID()); // Different user

        when(vetRepository.findById(vetId)).thenReturn(Optional.of(otherUserVet));
        when(vetRepository.save(any(Vet.class))).thenReturn(otherUserVet);

        VetResponse response = vetService.updateVet(vetId, vetUpdateRequest, userId, true);

        assertNotNull(response);
        verify(vetRepository, times(1)).save(any(Vet.class));
    }

    @Test
    @DisplayName("Should update my vet successfully")
    void testUpdateMyVet_Success() {
        when(vetRepository.findByUserId(userId)).thenReturn(Optional.of(testVet));
        when(vetRepository.save(any(Vet.class))).thenReturn(testVet);

        VetResponse response = vetService.updateMyVet(userId, vetUpdateRequest);

        assertNotNull(response);
        verify(vetRepository, times(1)).findByUserId(userId);
        verify(vetRepository, times(1)).save(any(Vet.class));
    }

    @Test
    @DisplayName("Should throw exception when own vet not found")
    void testUpdateMyVet_NotFound() {
        when(vetRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> vetService.updateMyVet(userId, vetUpdateRequest));
    }

    @Test
    @DisplayName("Should get all vets successfully")
    void testGetAllVets_Success() {
        Vet vet2 = new Vet();
        vet2.setId(UUID.randomUUID());
        vet2.setName("Veterinaria 2");

        when(vetRepository.findAll()).thenReturn(Arrays.asList(testVet, vet2));

        List<VetResponse> responses = vetService.getAllVets();

        assertEquals(2, responses.size());
        verify(vetRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get empty list when no vets exist")
    void testGetAllVets_Empty() {
        when(vetRepository.findAll()).thenReturn(Arrays.asList());

        List<VetResponse> responses = vetService.getAllVets();

        assertEquals(0, responses.size());
    }

    @Test
    @DisplayName("Should get vet by user id successfully")
    void testGetVetByUserId_Success() {
        when(vetRepository.findByUserId(userId)).thenReturn(Optional.of(testVet));

        VetResponse response = vetService.getVetByUserId(userId);

        assertNotNull(response);
        assertEquals("Veterinaria Central", response.getName());
        verify(vetRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Should throw exception when vet not found by user id")
    void testGetVetByUserId_NotFound() {
        when(vetRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> vetService.getVetByUserId(userId));
    }

    @Test
    @DisplayName("Should get vet by id successfully")
    void testGetVetById_Success() {
        when(vetRepository.findById(vetId)).thenReturn(Optional.of(testVet));

        VetResponse response = vetService.getVetById(vetId);

        assertNotNull(response);
        assertEquals("Veterinaria Central", response.getName());
        verify(vetRepository, times(1)).findById(vetId);
    }

    @Test
    @DisplayName("Should throw exception when vet not found by id")
    void testGetVetById_NotFound() {
        when(vetRepository.findById(vetId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> vetService.getVetById(vetId));
    }

    @Test
    @DisplayName("Should get nearby vets successfully")
    void testGetNearbyVets_Success() {
        Vet farVet = new Vet();
        farVet.setId(UUID.randomUUID());
        farVet.setName("Veterinaria Lejana");
        farVet.setLatitude(50.0);
        farVet.setLongitude(50.0);

        when(vetRepository.findAll()).thenReturn(Arrays.asList(testVet, farVet));

        List<VetResponse> responses = vetService.getNearbyVets(10.5, 20.5, 100.0);

        assertNotNull(responses);
        verify(vetRepository, times(1)).findAll();
    }
}

package com.example.vetservice.service;

import com.example.vetservice.dto.VetRequest;
import com.example.vetservice.dto.VetResponse;
import com.example.vetservice.dto.VetUpdateRequest;
import com.example.vetservice.entity.Vet;
import com.example.vetservice.repository.VetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VetService {

    private final VetRepository vetRepository;

    @Transactional
    public VetResponse createVet(VetRequest request, UUID userId) {
        log.info("Creating new vet for user {}: {}", userId, request.getName());
        validateCoordinates(request.getLatitude(), request.getLongitude());

        if (vetRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("El usuario ya tiene una veterinaria registrada");
        }

        Vet vet = mapToEntity(request, userId);
        Vet savedVet = vetRepository.save(vet);
        return mapToResponse(savedVet);
    }

    public VetResponse updateVet(UUID id, VetUpdateRequest request, UUID userId, boolean isAdmin) {
        log.info("Updating vet with id: {} by user {}", id, userId);
        Vet vet = vetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vet not found"));

        // Check permissions: only owner or admin can update
        if (!isAdmin && vet.getUserId() != null && !vet.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: cannot update other user's vet");
        }

        // If vet has no userId, assign it to current user
        if (vet.getUserId() == null) {
            vet.setUserId(userId);
        }

        if (request.getLatitude() != null || request.getLongitude() != null) {
            Double lat = request.getLatitude() != null ? request.getLatitude() : vet.getLatitude();
            Double lng = request.getLongitude() != null ? request.getLongitude() : vet.getLongitude();
            validateCoordinates(lat, lng);
        }

        updateEntity(vet, request);
        Vet updatedVet = vetRepository.save(vet);
        return mapToResponse(updatedVet);
    }

    public VetResponse updateMyVet(UUID userId, VetUpdateRequest request) {
        log.info("Updating my vet for user {}", userId);
        Vet vet = vetRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Vet not found for user"));

        if (request.getLatitude() != null || request.getLongitude() != null) {
            Double lat = request.getLatitude() != null ? request.getLatitude() : vet.getLatitude();
            Double lng = request.getLongitude() != null ? request.getLongitude() : vet.getLongitude();
            validateCoordinates(lat, lng);
        }

        updateEntity(vet, request);
        Vet updatedVet = vetRepository.save(vet);
        return mapToResponse(updatedVet);
    }

    public List<VetResponse> getAllVets() {
        log.info("Fetching all vets");
        return vetRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public VetResponse getVetByUserId(UUID userId) {
        log.info("Fetching vet by user id: {}", userId);
        Vet vet = vetRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Vet not found for user"));
        return mapToResponse(vet);
    }

    public VetResponse getVetById(UUID id) {
        log.info("Fetching vet by id: {}", id);
        Vet vet = vetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vet not found"));
        return mapToResponse(vet);
    }

    public List<VetResponse> getNearbyVets(double lat, double lng, double radiusKm) {
        log.info("Fetching nearby vets for lat: {}, lng: {}, radius: {} km", lat, lng, radiusKm);
        return vetRepository.findAll().stream()
                .filter(vet -> calculateDistance(lat, lng, vet.getLatitude(), vet.getLongitude()) <= radiusKm)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // Simple Euclidean distance approximation (not accurate for large distances)
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Vet mapToEntity(VetRequest request, UUID userId) {
        Vet vet = new Vet();
        vet.setUserId(userId);
        vet.setName(request.getName());
        vet.setAddress(request.getAddress());
        vet.setPhone(request.getPhone());
        vet.setLatitude(request.getLatitude());
        vet.setLongitude(request.getLongitude());
        vet.setImageUrl(request.getImageUrl());
        return vet;
    }

    private void updateEntity(Vet vet, VetUpdateRequest request) {
        if (request.getName() != null)
            vet.setName(request.getName());
        if (request.getAddress() != null)
            vet.setAddress(request.getAddress());
        if (request.getPhone() != null)
            vet.setPhone(request.getPhone());
        if (request.getLatitude() != null)
            vet.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)
            vet.setLongitude(request.getLongitude());
        if (request.getImageUrl() != null)
            vet.setImageUrl(request.getImageUrl());
    }

    private VetResponse mapToResponse(Vet vet) {
        return new VetResponse(
                vet.getId(),
                vet.getName(),
                vet.getAddress(),
                vet.getPhone(),
                vet.getLatitude(),
                vet.getLongitude(),
                vet.getImageUrl(),
                vet.getCreatedAt(),
                vet.getUpdatedAt());
    }
}
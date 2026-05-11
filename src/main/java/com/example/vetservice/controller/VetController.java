package com.example.vetservice.controller;

import com.example.vetservice.dto.VetRequest;
import com.example.vetservice.dto.VetResponse;
import com.example.vetservice.dto.VetUpdateRequest;
import com.example.vetservice.service.VetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vets")
@RequiredArgsConstructor
@Tag(name = "Vet Management", description = "API para gestionar veterinarias")
public class VetController {

        private final VetService vetService;

        @PostMapping
        @PreAuthorize("hasRole('ADMIN') or hasRole('REFUGIO')")
        @Operation(summary = "Crear una nueva veterinaria", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Veterinaria creada exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                        @ApiResponse(responseCode = "403", description = "No autorizado")
        })
        public ResponseEntity<VetResponse> createVet(@Valid @RequestBody VetRequest request,
                        Authentication authentication) {
                UUID userId = UUID.fromString(authentication.getName());
                VetResponse response = vetService.createVet(request, userId);
                return ResponseEntity.ok(response);
        }

        @PatchMapping("/my")
        @PreAuthorize("hasRole('REFUGIO') or hasRole('ADMIN')")
        @Operation(summary = "Actualizar mi veterinaria", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Veterinaria actualizada exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                        @ApiResponse(responseCode = "403", description = "No autorizado"),
                        @ApiResponse(responseCode = "404", description = "Veterinaria no encontrada")
        })
        public ResponseEntity<VetResponse> updateMyVet(@Valid @RequestBody VetUpdateRequest request,
                        Authentication authentication) {
                UUID userId = UUID.fromString(authentication.getName());
                VetResponse response = vetService.updateMyVet(userId, request);
                return ResponseEntity.ok(response);
        }

        @GetMapping
        @Operation(summary = "Obtener todas las veterinarias")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de veterinarias obtenida exitosamente")
        })
        public ResponseEntity<List<VetResponse>> getAllVets() {
                List<VetResponse> vets = vetService.getAllVets();
                return ResponseEntity.ok(vets);
        }

        @GetMapping("/my")
        @PreAuthorize("hasRole('REFUGIO') or hasRole('ADMIN')")
        @Operation(summary = "Obtener la veterinaria del usuario actual", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Veterinaria obtenida exitosamente"),
                        @ApiResponse(responseCode = "404", description = "Veterinaria no encontrada")
        })
        public ResponseEntity<VetResponse> getMyVet(Authentication authentication) {
                UUID userId = UUID.fromString(authentication.getName());
                VetResponse vet = vetService.getVetByUserId(userId);
                return ResponseEntity.ok(vet);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Obtener una veterinaria por ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Veterinaria obtenida exitosamente"),
                        @ApiResponse(responseCode = "404", description = "Veterinaria no encontrada")
        })
        public ResponseEntity<VetResponse> getVetById(@PathVariable UUID id) {
                VetResponse vet = vetService.getVetById(id);
                return ResponseEntity.ok(vet);
        }

        @GetMapping("/nearby")
        @Operation(summary = "Obtener veterinarias cercanas")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de veterinarias cercanas obtenida exitosamente")
        })
        public ResponseEntity<List<VetResponse>> getNearbyVets(
                        @RequestParam double lat,
                        @RequestParam double lng,
                        @RequestParam(defaultValue = "10") double radius) {
                List<VetResponse> vets = vetService.getNearbyVets(lat, lng, radius);
                return ResponseEntity.ok(vets);
        }
}
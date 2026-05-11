package com.example.vetservice.controller;

import com.example.vetservice.dto.VetRequest;
import com.example.vetservice.dto.VetResponse;
import com.example.vetservice.dto.VetUpdateRequest;
import com.example.vetservice.service.VetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("VetController Integration Tests")
class VetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VetService vetService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID vetId;
    private VetResponse vetResponse;
    private VetRequest vetRequest;
    private VetUpdateRequest vetUpdateRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        vetId = UUID.randomUUID();

        vetResponse = new VetResponse();
        vetResponse.setId(vetId);
        vetResponse.setName("Veterinaria Central");
        vetResponse.setAddress("Calle Principal 123");
        vetResponse.setPhone("123456789");
        vetResponse.setLatitude(10.5);
        vetResponse.setLongitude(20.5);
        vetResponse.setImageUrl("http://example.com/image.jpg");
        vetResponse.setCreatedAt(LocalDateTime.now());
        vetResponse.setUpdatedAt(LocalDateTime.now());

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
    @DisplayName("Should create vet with ADMIN role")
    @WithMockUser(roles = "ADMIN", username = "550e8400-e29b-41d4-a716-446655440000")
    void testCreateVet_Admin() throws Exception {
        when(vetService.createVet(any(VetRequest.class), any(UUID.class))).thenReturn(vetResponse);

        mockMvc.perform(post("/vets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Veterinaria Central"))
                .andExpect(jsonPath("$.phone").value("123456789"));
    }

    @Test
    @DisplayName("Should create vet with REFUGIO role")
    @WithMockUser(roles = "REFUGIO", username = "550e8400-e29b-41d4-a716-446655440001")
    void testCreateVet_Refugio() throws Exception {
        when(vetService.createVet(any(VetRequest.class), any(UUID.class))).thenReturn(vetResponse);

        mockMvc.perform(post("/vets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny create vet without authentication")
    void testCreateVet_NoAuth() throws Exception {
        mockMvc.perform(post("/vets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should update my vet with REFUGIO role")
    @WithMockUser(roles = "REFUGIO", username = "550e8400-e29b-41d4-a716-446655440000")
    void testUpdateMyVet_Refugio() throws Exception {
        when(vetService.updateMyVet(any(UUID.class), any(VetUpdateRequest.class))).thenReturn(vetResponse);

        mockMvc.perform(patch("/vets/my")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetUpdateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should update my vet with ADMIN role")
    @WithMockUser(roles = "ADMIN", username = "550e8400-e29b-41d4-a716-446655440000")
    void testUpdateMyVet_Admin() throws Exception {
        when(vetService.updateMyVet(any(UUID.class), any(VetUpdateRequest.class))).thenReturn(vetResponse);

        mockMvc.perform(patch("/vets/my")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetUpdateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny update my vet without authentication")
    void testUpdateMyVet_NoAuth() throws Exception {
        mockMvc.perform(patch("/vets/my")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetUpdateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get all vets without authentication")
    void testGetAllVets() throws Exception {
        VetResponse vet2 = new VetResponse();
        vet2.setId(UUID.randomUUID());
        vet2.setName("Veterinaria 2");

        List<VetResponse> vets = Arrays.asList(vetResponse, vet2);
        when(vetService.getAllVets()).thenReturn(vets);

        mockMvc.perform(get("/vets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Veterinaria Central"))
                .andExpect(jsonPath("$[1].name").value("Veterinaria 2"));
    }

    @Test
    @DisplayName("Should get my vet with authentication")
    @WithMockUser(roles = "REFUGIO", username = "550e8400-e29b-41d4-a716-446655440000")
    void testGetMyVet() throws Exception {
        when(vetService.getVetByUserId(any(UUID.class))).thenReturn(vetResponse);

        mockMvc.perform(get("/vets/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Veterinaria Central"));
    }

    @Test
    @DisplayName("Should deny get my vet without authentication")
    void testGetMyVet_NoAuth() throws Exception {
        mockMvc.perform(get("/vets/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get vet by id without authentication")
    @WithMockUser(roles = "USER")
    void testGetVetById() throws Exception {
        when(vetService.getVetById(vetId)).thenReturn(vetResponse);

        mockMvc.perform(get("/vets/" + vetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Veterinaria Central"))
                .andExpect(jsonPath("$.id").value(vetId.toString()));
    }

    @Test
    @DisplayName("Should return 400 for invalid latitude on create")
    @WithMockUser(roles = "ADMIN")
    void testCreateVet_InvalidLatitude() throws Exception {
        vetRequest.setLatitude(95.0);

        mockMvc.perform(post("/vets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for invalid longitude on create")
    @WithMockUser(roles = "ADMIN")
    void testCreateVet_InvalidLongitude() throws Exception {
        vetRequest.setLongitude(185.0);

        mockMvc.perform(post("/vets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vetRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for missing required fields")
    @WithMockUser(roles = "ADMIN")
    void testCreateVet_MissingFields() throws Exception {
        VetRequest invalidRequest = new VetRequest();
        invalidRequest.setName(""); // Empty name

        mockMvc.perform(post("/vets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}

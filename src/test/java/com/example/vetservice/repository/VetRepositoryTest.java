package com.example.vetservice.repository;

import com.example.vetservice.entity.Vet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("VetRepository Integration Tests")
class VetRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VetRepository vetRepository;

    private Vet testVet;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testVet = new Vet();
        testVet.setUserId(userId);
        testVet.setName("Veterinaria Test");
        testVet.setAddress("Calle Test 123");
        testVet.setPhone("123456789");
        testVet.setLatitude(10.5);
        testVet.setLongitude(20.5);
        testVet.setImageUrl("http://example.com/image.jpg");
    }

    @Test
    @DisplayName("Should save and retrieve vet by ID")
    void testSaveAndFindById() {
        Vet savedVet = vetRepository.save(testVet);

        assertNotNull(savedVet.getId());
        Optional<Vet> foundVet = vetRepository.findById(savedVet.getId());

        assertTrue(foundVet.isPresent());
        assertEquals("Veterinaria Test", foundVet.get().getName());
        assertEquals("123456789", foundVet.get().getPhone());
    }

    @Test
    @DisplayName("Should find vet by user ID")
    void testFindByUserId() {
        vetRepository.save(testVet);

        Optional<Vet> foundVet = vetRepository.findByUserId(userId);

        assertTrue(foundVet.isPresent());
        assertEquals(userId, foundVet.get().getUserId());
        assertEquals("Veterinaria Test", foundVet.get().getName());
    }

    @Test
    @DisplayName("Should return empty when vet not found by user ID")
    void testFindByUserId_NotFound() {
        UUID nonExistentUserId = UUID.randomUUID();

        Optional<Vet> foundVet = vetRepository.findByUserId(nonExistentUserId);

        assertTrue(foundVet.isEmpty());
    }

    @Test
    @DisplayName("Should update vet successfully")
    void testUpdateVet() {
        Vet savedVet = vetRepository.save(testVet);
        UUID vetId = savedVet.getId();

        savedVet.setName("Veterinaria Actualizada");
        savedVet.setPhone("987654321");
        Vet updatedVet = vetRepository.save(savedVet);

        Optional<Vet> retrievedVet = vetRepository.findById(vetId);

        assertTrue(retrievedVet.isPresent());
        assertEquals("Veterinaria Actualizada", retrievedVet.get().getName());
        assertEquals("987654321", retrievedVet.get().getPhone());
    }

    @Test
    @DisplayName("Should delete vet successfully")
    void testDeleteVet() {
        Vet savedVet = vetRepository.save(testVet);
        UUID vetId = savedVet.getId();

        vetRepository.deleteById(vetId);
        Optional<Vet> foundVet = vetRepository.findById(vetId);

        assertTrue(foundVet.isEmpty());
    }

    @Test
    @DisplayName("Should auto-set timestamps on creation")
    void testAutoTimestamps_OnCreation() {
        Vet savedVet = vetRepository.save(testVet);

        assertNotNull(savedVet.getCreatedAt());
        assertNotNull(savedVet.getUpdatedAt());
        assertEquals(savedVet.getCreatedAt().truncatedTo(java.time.temporal.ChronoUnit.MILLIS),
                savedVet.getUpdatedAt().truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
    }

    @Test
    @DisplayName("Should update timestamps on modification")
    void testAutoTimestamps_OnUpdate() throws InterruptedException {
        Vet savedVet = vetRepository.save(testVet);
        LocalDateTime originalCreatedAt = savedVet.getCreatedAt();
        LocalDateTime originalUpdatedAt = savedVet.getUpdatedAt();

        Thread.sleep(100);
        savedVet.setName("Nombre Modificado");
        vetRepository.save(savedVet);
        entityManager.flush();
        entityManager.clear();
        Vet updatedVet = vetRepository.findById(savedVet.getId()).get();

        assertEquals(originalCreatedAt.truncatedTo(java.time.temporal.ChronoUnit.MICROS),
                updatedVet.getCreatedAt().truncatedTo(java.time.temporal.ChronoUnit.MICROS));
        assertTrue(updatedVet.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should find all vets")
    void testFindAll() {
        Vet vet1 = new Vet();
        vet1.setUserId(UUID.randomUUID());
        vet1.setName("Veterinaria 1");
        vet1.setLatitude(10.0);
        vet1.setLongitude(20.0);

        Vet vet2 = new Vet();
        vet2.setUserId(UUID.randomUUID());
        vet2.setName("Veterinaria 2");
        vet2.setLatitude(15.0);
        vet2.setLongitude(25.0);

        vetRepository.save(vet1);
        vetRepository.save(vet2);
        vetRepository.save(testVet);

        int count = (int) vetRepository.count();

        assertTrue(count >= 3);
    }
}

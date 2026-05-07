package com.example.vetservice.repository;

import com.example.vetservice.entity.Vet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VetRepository extends JpaRepository<Vet, UUID> {

    Optional<Vet> findByUserId(UUID userId);
}
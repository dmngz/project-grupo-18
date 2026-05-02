package com.example.cybercert.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cybercert.models.Certification;


public interface CertificationRepository extends JpaRepository<Certification, Long> {
    Optional<Certification> findByName(String name);

}

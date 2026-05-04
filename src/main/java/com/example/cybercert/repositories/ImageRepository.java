package com.example.cybercert.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cybercert.models.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}

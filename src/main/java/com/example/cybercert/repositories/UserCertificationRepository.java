package com.example.cybercert.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cybercert.models.UserCertification;

public interface UserCertificationRepository extends JpaRepository<UserCertification, Long> {

    boolean existsByUserIdAndCertificationId(Long userId, Long certificationId);

    List<UserCertification> findByUserId(Long userId);

    List<UserCertification> findByUserIdOrderByPurchasedAtDesc(Long userId);
}

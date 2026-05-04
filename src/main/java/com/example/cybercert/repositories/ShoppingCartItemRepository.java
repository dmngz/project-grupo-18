package com.example.cybercert.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cybercert.models.ShoppingCartItem;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Long> {

    List<ShoppingCartItem> findByUserId(Long userId);

    Page<ShoppingCartItem> findByUserId(Long userId, Pageable pageable);

    Optional<ShoppingCartItem> findByUserIdAndCertificationId(Long userId, Long certificationId);

    boolean existsByUserIdAndCertificationId(Long userId, Long certificationId);

    void deleteByUserIdAndCertificationId(Long userId, Long certificationId);

    void deleteByUserId(Long userId);

    long countByUserId(Long userId);
}

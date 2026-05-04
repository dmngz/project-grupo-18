package com.example.cybercert.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.cybercert.models.Certification;
import com.example.cybercert.models.ShoppingCartItem;
import com.example.cybercert.models.User;
import com.example.cybercert.models.UserCertification;
import com.example.cybercert.repositories.ShoppingCartItemRepository;
import com.example.cybercert.repositories.UserCertificationRepository;

import jakarta.transaction.Transactional;

@Service
public class ShoppingCartService {

    public static final double CERTIFICATION_PRICE = 89.99;

    @Autowired
    private ShoppingCartItemRepository shoppingCartItemRepository;

    @Autowired
    private UserCertificationRepository userCertificationRepository;

    @Transactional
    public List<UserCertification> findByUserIdOrderByPurchasedAtDesc(Long userId) {
        return userCertificationRepository.findByUserIdOrderByPurchasedAtDesc(userId);
    }

    @Transactional
    public boolean hasPurchasedCertification(Long userId, Long certificationId) {
        if (userId == null || certificationId == null) {
            return false;
        }
        return userCertificationRepository.existsByUserIdAndCertificationId(userId, certificationId);
    }

    @Transactional
    public boolean isCertificationInCart(Long userId, Long certificationId) {
        if (userId == null || certificationId == null) {
            return false;
        }
        return shoppingCartItemRepository.existsByUserIdAndCertificationId(userId, certificationId);
    }

    @Transactional
    public Optional<ShoppingCartItem> findCartItem(Long userId, Long certificationId) {
        if (userId == null || certificationId == null) {
            return Optional.empty();
        }

        return shoppingCartItemRepository.findByUserIdAndCertificationId(userId, certificationId);
    }

    @Transactional
    public Page<ShoppingCartItem> getCartItemsPage(Long userId, Pageable pageable) {
        if (userId == null) {
            return Page.empty(pageable);
        }

        return shoppingCartItemRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public List<Certification> getCartCertifications(Long userId) {
        List<Certification> certifications = new ArrayList<>();
        if (userId == null) {
            return certifications;
        }

        List<ShoppingCartItem> items = shoppingCartItemRepository.findByUserId(userId);
        for (ShoppingCartItem item : items) {
            if (item.getCertification() != null) {
                certifications.add(item.getCertification());
            }
        }
        return certifications;
    }

    @Transactional
    public int getVisibleCartSize(Long userId) {
        if (userId == null) {
            return 0;
        }

        return Math.toIntExact(shoppingCartItemRepository.countByUserId(userId));
    }

    @Transactional
    public void addToCart(User user, Certification certification) {
        if (user == null || certification == null) {
            return;
        }

        if (hasPurchasedCertification(user.getId(), certification.getId())) {
            return;
        }

        if (isCertificationInCart(user.getId(), certification.getId())) {
            return;
        }

        ShoppingCartItem item = new ShoppingCartItem(user, certification, CERTIFICATION_PRICE);
        shoppingCartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(Long userId, Long certificationId) {
        if (userId == null || certificationId == null) {
            return;
        }
        shoppingCartItemRepository.deleteByUserIdAndCertificationId(userId, certificationId);
    }

    @Transactional
    public void completeCheckout(User user) {
        if (user == null) {
            return;
        }

        List<ShoppingCartItem> items = shoppingCartItemRepository.findByUserId(user.getId());
        for (ShoppingCartItem item : items) {
            Certification certification = item.getCertification();
            if (certification == null) {
                continue;
            }

            boolean alreadyOwned = userCertificationRepository.existsByUserIdAndCertificationId(user.getId(),
                    certification.getId());

            if (!alreadyOwned) {
                userCertificationRepository.save(new UserCertification(user, certification));
            }
        }

        shoppingCartItemRepository.deleteByUserId(user.getId());
    }

    @Transactional
    public void removeOwnedCertificationsFromCart(Long userId) {
        if (userId == null) {
            return;
        }

        List<ShoppingCartItem> items = shoppingCartItemRepository.findByUserId(userId);
        for (ShoppingCartItem item : items) {
            Certification certification = item.getCertification();
            if (certification == null) {
                shoppingCartItemRepository.deleteById(item.getId());
                continue;
            }

            boolean alreadyOwned = userCertificationRepository.existsByUserIdAndCertificationId(userId,
                    certification.getId());
            if (alreadyOwned) {
                shoppingCartItemRepository.deleteById(item.getId());
            }
        }
    }
}

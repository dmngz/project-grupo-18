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

    public List<UserCertification> findByUserIdOrderByPurchasedAtDesc(Long userId) {
        return userCertificationRepository.findByUserIdOrderByPurchasedAtDesc(userId);
    }

    public boolean hasPurchasedCertification(Long userId, Long certificationId) {
        if (userId == null || certificationId == null) {
            return false;
        }
        return userCertificationRepository.existsByUserIdAndCertificationId(userId, certificationId);
    }

    public boolean isCertificationInCart(Long userId, Long certificationId) {
        if (userId == null || certificationId == null) {
            return false;
        }
        return shoppingCartItemRepository.existsByUserIdAndCertificationId(userId, certificationId);
    }

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

    public int getVisibleCartSize(Long userId) {
        return getCartCertifications(userId).size();
    }

    public Page<ShoppingCartItem> findCartItems(Long userId, Pageable pageable) {
        if (userId == null) {
            return Page.empty(pageable);
        }

        return shoppingCartItemRepository.findByUserId(userId, pageable);
    }

    public Optional<ShoppingCartItem> findCartItem(Long userId, Long cartItemId) {
        if (userId == null || cartItemId == null) {
            return Optional.empty();
        }

        return shoppingCartItemRepository.findByIdAndUserId(cartItemId, userId);
    }

    @Transactional
    public ShoppingCartItem addToCart(User user, Certification certification) {
        if (user == null || certification == null) {
            return null;
        }

        if (hasPurchasedCertification(user.getId(), certification.getId())) {
            return null;
        }

        if (isCertificationInCart(user.getId(), certification.getId())) {
            return null;
        }

        ShoppingCartItem item = new ShoppingCartItem(user, certification, CERTIFICATION_PRICE);
        return shoppingCartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(Long userId, Long certificationId) {
        if (userId == null || certificationId == null) {
            return;
        }
        shoppingCartItemRepository.deleteByUserIdAndCertificationId(userId, certificationId);
    }

    @Transactional
    public boolean removeCartItem(Long userId, Long cartItemId) {
        if (userId == null || cartItemId == null) {
            return false;
        }

        Optional<ShoppingCartItem> cartItem = shoppingCartItemRepository.findByIdAndUserId(cartItemId, userId);
        if (cartItem.isEmpty()) {
            return false;
        }

        shoppingCartItemRepository.delete(cartItem.get());
        return true;
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

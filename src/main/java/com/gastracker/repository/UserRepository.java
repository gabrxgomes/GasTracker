package com.gastracker.repository;

import com.gastracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByTelegramUsername(String telegramUsername);

    Optional<User> findByChatId(Long chatId);

    boolean existsByTelegramUsername(String telegramUsername);

    boolean existsByChatId(Long chatId);

    List<User> findByIsActiveTrue();

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.maxGasPrice >= :gasPrice")
    List<User> findActiveUsersWithGasPriceAbove(Integer gasPrice);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
}

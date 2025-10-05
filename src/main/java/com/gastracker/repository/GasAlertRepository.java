package com.gastracker.repository;

import com.gastracker.model.GasAlert;
import com.gastracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GasAlertRepository extends JpaRepository<GasAlert, Long> {

    List<GasAlert> findByUser(User user);

    List<GasAlert> findByUserAndSentAtAfter(User user, LocalDateTime sentAt);

    @Query("SELECT COUNT(ga) FROM GasAlert ga WHERE ga.sentAt >= :startDate")
    long countAlertsSince(LocalDateTime startDate);

    @Query("SELECT COUNT(ga) FROM GasAlert ga WHERE ga.success = true AND ga.sentAt >= :startDate")
    long countSuccessfulAlertsSince(LocalDateTime startDate);
}

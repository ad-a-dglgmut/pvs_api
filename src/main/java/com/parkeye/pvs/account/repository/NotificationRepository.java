package com.parkeye.pvs.account.repository;

import com.parkeye.pvs.account.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByTicketIdAndKind(Long ticketId, String kind);

    @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.userId = :userId AND n.seen = false")
    long countUnread(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.seen = true WHERE n.userId = :userId AND n.seen = false")
    int markAllSeen(@Param("userId") Long userId);
}

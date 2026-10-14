package com.parkeye.pvs.service;

import com.parkeye.pvs.account.entity.NotificationEntity;
import com.parkeye.pvs.account.entity.NotificationType;
import com.parkeye.pvs.account.repository.NotificationRepository;
import com.parkeye.pvs.dto.ConsumerDtos.NotificationDto;
import com.parkeye.pvs.dto.ConsumerDtos.NotificationsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;
    private final Clock clock;

    public NotificationService(NotificationRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    @Transactional
    public void create(long userId, NotificationType type, String title, String body, Long ticketId, String kind) {
        NotificationEntity n = new NotificationEntity();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setCreatedAt(LocalDateTime.now(clock));
        n.setSeen(false);
        n.setTicketId(ticketId);
        n.setKind(kind);
        repo.save(n);
    }

    /** Creates the notification unless one with the same ticket + kind already exists. */
    @Transactional
    public boolean createOnce(long userId, NotificationType type, String title, String body, long ticketId, String kind) {
        if (repo.existsByTicketIdAndKind(ticketId, kind)) return false;
        create(userId, type, title, body, ticketId, kind);
        return true;
    }

    @Transactional(readOnly = true)
    public NotificationsResponse list(long userId) {
        List<NotificationDto> items = repo.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(n -> new NotificationDto(
                        n.getId(), n.getType(), n.getTitle(), n.getBody(),
                        n.getCreatedAt().atZone(clock.getZone()).toInstant(),
                        Boolean.TRUE.equals(n.getSeen())))
                .toList();
        return new NotificationsResponse(repo.countUnread(userId), items);
    }

    @Transactional
    public void markAllSeen(long userId) {
        repo.markAllSeen(userId);
    }
}

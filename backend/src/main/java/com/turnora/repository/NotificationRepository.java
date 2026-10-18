package com.turnora.repository;

import com.turnora.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByUserIdAndChannelOrderByCreatedAtDesc(Long userId, String channel);

    boolean existsByTicketIdAndType(Long ticketId, String type);
}

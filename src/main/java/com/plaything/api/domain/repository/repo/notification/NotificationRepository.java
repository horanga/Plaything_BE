package com.plaything.api.domain.repository.repo.notification;

import com.plaything.api.domain.repository.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiver_LoginId(String loginId);
}

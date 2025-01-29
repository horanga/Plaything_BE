package com.plaything.api.domain.repository.repo.notification;

import com.plaything.api.domain.repository.entity.notification.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByReceiver_LoginId(String loginId);
}

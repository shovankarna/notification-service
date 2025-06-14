package com.shovan.NotificationService.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shovan.NotificationService.entity.Notification;
import com.shovan.NotificationService.enums.NotificationStatus;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Retrieve all notifications with the given status.
     * Useful for retrying or monitoring pending items.
     *
     * @param status the status to filter by
     * @return list of matching notifications
     */
    List<Notification> findByStatus(NotificationStatus status);
}

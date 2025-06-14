package com.shovan.NotificationService.dto;

import lombok.*;

import java.util.List;

/**
 * DTO returned after enqueuing notifications,
 * containing the IDs of the persisted Notification records.
 * 
 * @param notificationIds list of database IDs for the newly created notifications
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    /** IDs of the Notification entities that were persisted and queued. */
    private List<Long> notificationIds;
}

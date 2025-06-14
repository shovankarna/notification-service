package com.shovan.NotificationService.entity;

import java.time.LocalDateTime;

import com.shovan.NotificationService.enums.NotificationStatus;
import com.shovan.NotificationService.enums.Channel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notification")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private String templateName;

    /**
     * JSON-serialized map of template parameters
     * (e.g. {"username":"Alice","link":"…"}).
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String parameters;

    /** Current delivery status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    /** How many times we’ve attempted to send this notification. */
    @Column(nullable = false)
    private int attempts;

    /** When this record was created. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** When this record was last updated. */
    private LocalDateTime updatedAt;

    /**
     * JPA callback: initialize timestamps and status on first save.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = NotificationStatus.PENDING;
        }
    }

    /**
     * JPA callback: update updatedAt on each change.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

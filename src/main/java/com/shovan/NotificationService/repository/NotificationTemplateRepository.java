package com.shovan.NotificationService.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shovan.NotificationService.entity.NotificationTemplate;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    /**
     * Find a template by its unique name.
     *
     * @param name the template’s unique key
     * @return an Optional containing the template if present
     */
    Optional<NotificationTemplate> findByName(String name);
}

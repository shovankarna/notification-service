package com.shovan.NotificationService.dto;

import java.util.List;
import java.util.Map;

import com.shovan.NotificationService.enums.Channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing the client’s request to send one or more notifications.
 * 
 * @param templateName the key of the template to render (must match a NotificationTemplate.name)
 * @param channels     list of channels (EMAIL, SMS, PUSH) to send through
 * @param parameters   map of template variables (e.g. username, link) to substitute
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationRequestDTO {

    /** Unique name/key of the template to render. */
    @NotBlank(message = "templateName is required")
    private String templateName;

    /** At least one channel must be specified (e.g. EMAIL, SMS, PUSH). */
    @NotEmpty(message = "At least one channel must be specified")
    private List<Channel> channels;

    /** Template parameters to substitute; must not be null. */
    @NotNull(message = "parameters must not be null")
    private Map<String, Object> parameters;
}

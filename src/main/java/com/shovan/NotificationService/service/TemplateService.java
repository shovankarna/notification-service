package com.shovan.NotificationService.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shovan.NotificationService.entity.NotificationTemplate;
import com.shovan.NotificationService.repository.NotificationTemplateRepository;
import com.shovan.NotificationService.util.TemplateRenderer;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Service responsible for:
 * 1) Loading NotificationTemplate entities from the database.
 * 2) Caching raw template content in Redis to avoid DB hits.
 * 3) Parsing request parameters (JSON) into a Map.
 * 4) Rendering the final content via TemplateRenderer.
 *
 * <p>
 * <b>Pattern:</b> Cache‐Aside (lazy load + cache) for the template content.
 * </p>
 */
@Service
public class TemplateService {

    private static final String CACHE_PREFIX = "template:"; // Redis key prefix
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final NotificationTemplateRepository templateRepository;
    private final StringRedisTemplate redisTemplate;
    private final TemplateRenderer templateRenderer;
    private final ObjectMapper objectMapper;

    public TemplateService(NotificationTemplateRepository templateRepository,
            StringRedisTemplate redisTemplate,
            TemplateRenderer templateRenderer,
            ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.redisTemplate = redisTemplate;
        this.templateRenderer = templateRenderer;
        this.objectMapper = objectMapper;
    }

    /**
     * Render a template identified by name, substituting parameters.
     *
     * @param templateName   the unique key of the NotificationTemplate
     * @param parametersJson JSON‐serialized map of variable names → values
     * @return fully rendered content (HTML/text)
     * @throws NotificationException if template not found or JSON invalid
     */
    public String render(String templateName, String parametersJson) {
        // 1) Load the raw template content (cache‐aside)
        String rawTemplate = loadTemplateContent(templateName);

        // 2) Deserialize parameters JSON into a Map
        Map<String, Object> variables;
        try {
            variables = objectMapper.readValue(
                    parametersJson, new TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception ex) {
            throw new NotificationException(
                    "Invalid template parameters JSON for template=" + templateName, ex);
        }

        // 3) Delegate to TemplateRenderer (Thymeleaf) for substitution
        return templateRenderer.render(rawTemplate, variables);
    }

    /**
     * Retrieve template content from Redis cache or DB if missing.
     *
     * @param templateName the key of the template to fetch
     * @return the raw template string
     * @throws NotificationException if no template is found in the DB
     */
    private String loadTemplateContent(String templateName) {
        String cacheKey = CACHE_PREFIX + templateName;
        // 1) Try Redis cache first
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2) Fall back to DB
        Optional<NotificationTemplate> opt = templateRepository.findByName(templateName);
        NotificationTemplate tpl = opt
                .orElseThrow(() -> new NotificationException("Template not found: " + templateName));
        String content = tpl.getContent();

        // 3) Cache in Redis for future requests
        redisTemplate.opsForValue()
                .set(cacheKey, content, CACHE_TTL);

        return content;
    }
}

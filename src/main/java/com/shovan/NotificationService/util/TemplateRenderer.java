package com.shovan.NotificationService.util;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Utility for rendering Thymeleaf templates provided as plain strings.
 *
 * <p>
 * No file‐based resolution here—each template is processed
 * from a String. We use StringTemplateResolver under the hood.
 * </p>
 */
@Component
public class TemplateRenderer {

    /** The Thymeleaf engine configured for string templates. */
    private final TemplateEngine templateEngine;

    public TemplateRenderer() {
        // 1) Create a resolver that reads templates from a String
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(false); // we’ll rely on Redis to cache full templates

        // 2) Build the engine with our resolver
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }

    /**
     * Render the given template content, substituting the provided variables.
     *
     * @param templateContent the raw Thymeleaf template string (may contain ${...}
     *                        placeholders)
     * @param variables       a map of variable names → values for substitution
     * @return the fully rendered result (HTML/text)
     */
    public String render(String templateContent, Map<String, Object> variables) {
        Context context = new Context();
        // Populate context with all variables
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }
        // Process and return the result
        return templateEngine.process(templateContent, context);
    }
}

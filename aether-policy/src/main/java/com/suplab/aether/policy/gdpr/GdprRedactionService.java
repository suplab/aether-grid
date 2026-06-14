package com.suplab.aether.policy.gdpr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

public class GdprRedactionService {

    private static final Logger log = LoggerFactory.getLogger(GdprRedactionService.class);
    private static final String REDACTED = "[REDACTED]";

    private static final List<RedactionRule> RULES = List.of(
            new RedactionRule("email",
                    Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}")),
            new RedactionRule("phone-e164",
                    Pattern.compile("\\+?[1-9]\\d{7,14}")),
            new RedactionRule("credit-card",
                    Pattern.compile("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13})\\b")),
            new RedactionRule("ssn",
                    Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b")),
            new RedactionRule("jwt-bearer",
                    Pattern.compile("Bearer\\s+[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+")),
            new RedactionRule("api-key",
                    Pattern.compile("(?i)(api[_-]?key|x-api-key|authorization)\\s*[=:]\\s*[^&\\s]+"))
    );

    public String redact(String text) {
        if (text == null || text.isBlank()) return text;
        var result = text;
        for (var rule : RULES) {
            var before = result;
            result = rule.pattern().matcher(result).replaceAll(REDACTED);
            if (!result.equals(before)) {
                log.debug("Redacted PII type={}", rule.name());
            }
        }
        return result;
    }

    public boolean containsPii(String text) {
        if (text == null || text.isBlank()) return false;
        return RULES.stream().anyMatch(rule -> rule.pattern().matcher(text).find());
    }

    private record RedactionRule(String name, Pattern pattern) {}
}

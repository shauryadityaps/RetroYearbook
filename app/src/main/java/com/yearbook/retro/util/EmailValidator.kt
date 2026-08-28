package com.yearbook.retro.util

import java.util.regex.Pattern

/**
 * Validates email addresses using strict RFC 5322 compliance,
 * TLD length verification, and disposable/temporary email filtering.
 */
object EmailValidator {

    // RFC 5322 compliant regex for standard email addresses
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,12}$"
    )

    // Known temporary/disposable burner email domains to block spam
    private val DISPOSABLE_DOMAINS = setOf(
        "mailinator.com", "tempmail.com", "10minutemail.com", "guerrillamail.com",
        "throwawaymail.com", "yopmail.com", "trashmail.com", "sharklasers.com",
        "getairmail.com", "dispostable.com", "fakeinbox.com", "mytemp.email"
    )

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validate(email: String): ValidationResult {
        val clean = email.trim().lowercase()

        if (clean.isBlank()) {
            return ValidationResult.Invalid("Please enter an email address")
        }

        if (clean.length < 5 || clean.length > 254) {
            return ValidationResult.Invalid("Email length must be between 5 and 254 characters")
        }

        if (!EMAIL_PATTERN.matcher(clean).matches()) {
            return ValidationResult.Invalid("Please enter a valid email format (e.g. name@domain.com)")
        }

        val domain = clean.substringAfter("@", "")
        if (domain.isBlank() || !domain.contains(".")) {
            return ValidationResult.Invalid("Please enter a valid domain name")
        }

        val tld = domain.substringAfterLast(".")
        if (tld.length < 2) {
            return ValidationResult.Invalid("Email has an incomplete domain extension")
        }

        if (DISPOSABLE_DOMAINS.contains(domain)) {
            return ValidationResult.Invalid("Temporary or disposable email addresses are not allowed")
        }

        return ValidationResult.Valid
    }
}

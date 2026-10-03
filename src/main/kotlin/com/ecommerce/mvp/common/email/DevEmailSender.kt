package com.ecommerce.mvp.common.email

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class DevEmailSender : EmailSender {

    private val log = LoggerFactory.getLogger(DevEmailSender::class.java)

    override fun sendPasswordResetEmail(userEmail: String, rawToken: String, frontendUrl: String) {
        val maskedEmail = maskEmail(userEmail)
        val resetLinkPreview = "$frontendUrl/reset-password?token=[redacted]"

        log.info(
            "Password reset email queued for {}. Subject: Reset your Nexus password. Link: {}. This link expires in 30 minutes. Ignore if you didn't request this.",
            maskedEmail,
            resetLinkPreview
        )
    }

    private fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        if (atIndex <= 1) {
            return "*${email.substringAfter('@')}"
        }

        val localPart = email.substring(0, atIndex)
        val domain = email.substring(atIndex + 1)
        val maskedLocal = localPart.take(2) + "*".repeat(localPart.length - 2)
        return "$maskedLocal@$domain"
    }
}


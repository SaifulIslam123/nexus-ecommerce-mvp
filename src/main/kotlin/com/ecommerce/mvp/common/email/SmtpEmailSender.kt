package com.ecommerce.mvp.common.email

import com.ecommerce.mvp.common.config.AppMailProperties
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
@Profile("prod")
class SmtpEmailSender(
    private val mailSender: JavaMailSender,
    private val appMailProperties: AppMailProperties
) : EmailSender {

    override fun sendPasswordResetEmail(userEmail: String, rawToken: String, frontendUrl: String) {
        val message = SimpleMailMessage().apply {
            setFrom(appMailProperties.from)
            setTo(userEmail)
            subject = "Reset your Nexus password"
            text = """
                We received a request to reset your password.

                Use this link to reset your password:
                ${frontendUrl}/reset-password?token=$rawToken

                This link expires in 30 minutes.
                If you didn't request this, you can safely ignore this email.
            """.trimIndent()
        }

        mailSender.send(message)
    }
}

package com.ecommerce.mvp.common.entity.audit

import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class AuditorAwareImpl : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
        // In a real application, you would get the current user from the SecurityContext
        // For this example, we return a hardcoded value:
        return Optional.of(SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN")
    }
}
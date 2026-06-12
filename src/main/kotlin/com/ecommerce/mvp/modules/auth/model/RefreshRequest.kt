package com.ecommerce.mvp.modules.auth.model

import jakarta.validation.constraints.NotBlank

data class RefreshRequest(@field:NotBlank val refreshToken: String)

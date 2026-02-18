package com.ecommerce.mvp.common.exception

import java.time.LocalDateTime;

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
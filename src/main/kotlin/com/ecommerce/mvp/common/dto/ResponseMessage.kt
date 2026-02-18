package com.ecommerce.mvp

data class ResponseMessage(
    val isSuccessful: Boolean,
    val code: Int,
    val message: String
)
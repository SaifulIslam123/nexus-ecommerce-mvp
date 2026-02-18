package com.practice.ecommerce.ecommerce

data class ResponseMessage(
    val isSuccessful: Boolean,
    val code: Int,
    val message: String
)
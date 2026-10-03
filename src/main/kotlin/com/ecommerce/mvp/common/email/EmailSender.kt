package com.ecommerce.mvp.common.email

interface EmailSender {
    fun sendPasswordResetEmail(userEmail: String, rawToken: String, frontendUrl: String)
}


package com.ecommerce.mvp.security

import org.springframework.security.access.prepost.PreAuthorize

const val ROLE_PREFIX = "ROLE_"

enum class ERole {
    SHOPPER,
    ADMIN,
    COURIER
}

private const val ROLE_ENUM_CLASS_NAME = "com.ecommerce.mvp.security.ERole"

// 1. Target must be FUNCTION (for methods) or CLASS (for whole controllers)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole(T($ROLE_ENUM_CLASS_NAME).ADMIN.name())")
annotation class IsAdmin

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole(T($ROLE_ENUM_CLASS_NAME).SHOPPER.name())")
annotation class IsShopper

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole(T($ROLE_ENUM_CLASS_NAME).COURIER.name())")
annotation class IsCourier
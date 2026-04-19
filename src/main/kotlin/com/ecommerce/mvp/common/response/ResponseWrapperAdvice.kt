package com.ecommerce.mvp.common.response
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice
class ResponseWrapperAdvice : ResponseBodyAdvice<Any> {

    private val excludedPaths = listOf("/v3/api-docs", "/swagger-ui", "/swagger-resources", "/webjars")

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        // Prevent double wrapping or wrapping the error response itself
        if (body is ApiResponse<*>) return body

        // Skip wrapping for Swagger / OpenAPI endpoints
        val path = request.uri.path
        if (excludedPaths.any { path.startsWith(it) }) return body

        return ApiResponse(
            success = true,
            message = "Operation successful",
            data = body
        )
    }
}
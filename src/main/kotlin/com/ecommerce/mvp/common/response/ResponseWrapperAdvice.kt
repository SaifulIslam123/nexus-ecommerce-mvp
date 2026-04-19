package com.ecommerce.mvp.common.response

import com.ecommerce.mvp.common.config.SwaggerProperties
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice
class ResponseWrapperAdvice(
    private val swaggerProperties: SwaggerProperties
) : ResponseBodyAdvice<Any> {

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
        // Prevent double wrapping
        if (body is ApiResponse<*>) return body

        // Skip wrapping for Swagger / OpenAPI endpoints (paths read from application.properties)
        val path = request.uri.path
        if (swaggerProperties.publicPaths.any { path.startsWith(it.trimEnd('*').trimEnd('/')) }) return body

        return ApiResponse(
            success = true,
            message = "Operation successful",
            data = body
        )
    }
}
package com.ecommerce.mvp.common.exception

import com.ecommerce.mvp.common.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import java.util.stream.Collectors


@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Resource not found"
        )
        //return ResponseEntity<ErrorResponse>(error, HttpStatus.NOT_FOUND)

    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: MethodArgumentNotValidException, request: HttpServletRequest): ApiResponse<Unit> {

        val errorMsg = ex.bindingResult
            .fieldErrors
            .stream()
            .map { e: FieldError? -> e!!.field + ": " + e.defaultMessage }
            .collect(Collectors.joining(", "))

        return ApiResponse(
            success = false,
            message = errorMsg
        )
        // return ResponseEntity<ErrorResponse?>(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleUsernameNotFound(ex: UsernameNotFoundException, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message.toString()
        )
    }

    @ExceptionHandler(HttpClientErrorException.Unauthorized::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnAuthorized(ex: Exception, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message.toString()
        )
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message.toString()
        )
    }
}

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ResourceNotFoundException(message: String) : RuntimeException(message)


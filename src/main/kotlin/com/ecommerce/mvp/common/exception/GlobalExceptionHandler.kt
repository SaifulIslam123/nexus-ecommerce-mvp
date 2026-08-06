package com.ecommerce.mvp.common.exception

import com.ecommerce.mvp.common.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.CannotAcquireLockException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.stream.Collectors


@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleResourceNotFound(ex: ResourceNotFoundException, request: HttpServletRequest): ApiResponse<Unit> {
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

    @ExceptionHandler(AuthenticationException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorized(ex: AuthenticationException, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Authentication is required to access this resource"
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

    @ExceptionHandler(BusinessValidationException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun handleBusinessValidation(ex: BusinessValidationException, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Invalid request data"
        )
    }

    @ExceptionHandler(ResourceAlreadyExistException::class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    fun handleResourceAlreadyExist(ex: ResourceAlreadyExistException, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Invalid request data"
        )
    }

    @ExceptionHandler(InvalidRefreshTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidRefreshToken(ex: InvalidRefreshTokenException, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Invalid or expired refresh token"
        )
    }

    @ExceptionHandler(OptimisticLockingFailureException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleOptimisticLockingFailure(ex: OptimisticLockingFailureException, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Resource has been modified by another transaction"
        )
    }

    @ExceptionHandler(CannotAcquireLockException::class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    fun handleLockTimeout(ex: CannotAcquireLockException, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = "The system is currently handling a high volume of inventory updates. Please try again shortly."
        )
    }

    @ExceptionHandler(PessimisticLockingFailureException::class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    fun handleLockTimeout(ex: PessimisticLockingFailureException, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message
                ?: "The system is currently handling a high volume of inventory updates. Please try again shortly."
        )
    }

    @ExceptionHandler(PaymentFailedException::class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    fun handlePaymentFailed(ex: PaymentFailedException, request: HttpServletRequest): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message.toString()
        )
    }
}


class ResourceNotFoundException(message: String) : RuntimeException(message)
class BusinessValidationException(message: String) : RuntimeException(message)
class ResourceAlreadyExistException(message: String) : RuntimeException(message)
class InvalidRefreshTokenException(message: String) : RuntimeException(message)
class PaymentFailedException(message: String) : RuntimeException(message)


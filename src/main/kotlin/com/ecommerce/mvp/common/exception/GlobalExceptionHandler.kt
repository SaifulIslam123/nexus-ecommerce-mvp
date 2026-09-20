package com.ecommerce.mvp.common.exception

import com.ecommerce.mvp.common.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.CannotAcquireLockException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.dao.QueryTimeoutException
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
    fun handleResourceNotFound(ex: ResourceNotFoundException): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Resource not found"
        )
        //return ResponseEntity<ErrorResponse>(error, HttpStatus.NOT_FOUND)

    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: MethodArgumentNotValidException): ApiResponse<Unit> {

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
    fun handleUsernameNotFound(ex: UsernameNotFoundException): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message.toString()
        )
    }

    @ExceptionHandler(AuthenticationException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorized(ex: AuthenticationException): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Authentication is required to access this resource"
        )
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneric(ex: Exception): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = "An unexpected error occurred"
        )
    }

    @ExceptionHandler(BusinessValidationException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun handleBusinessValidation(ex: BusinessValidationException): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Invalid request data"
        )
    }

    @ExceptionHandler(ResourceAlreadyExistException::class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    fun handleResourceAlreadyExist(ex: ResourceAlreadyExistException): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Invalid request data"
        )
    }

    @ExceptionHandler(InvalidRefreshTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidRefreshToken(ex: InvalidRefreshTokenException): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Invalid or expired refresh token"
        )
    }

    @ExceptionHandler(OptimisticLockingFailureException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleOptimisticLockingFailure(
        ex: OptimisticLockingFailureException,
        request: HttpServletRequest
    ): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message ?: "Resource has been modified by another transaction"
        )
    }

    @ExceptionHandler(CannotAcquireLockException::class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    fun handleLockTimeout(ex: CannotAcquireLockException): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = "The system is currently handling a high volume of inventory updates. Please try again shortly."
        )
    }

    @ExceptionHandler(PessimisticLockingFailureException::class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    fun handleLockTimeout(ex: PessimisticLockingFailureException): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message
                ?: "The system is currently handling a high volume of inventory updates. Please try again shortly."
        )
    }

    @ExceptionHandler(PaymentFailedException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handlePaymentFailed(ex: PaymentFailedException): ApiResponse<Unit> {
        return ApiResponse(
            success = false,
            message = ex.message.toString()
        )
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDataIntegrityViolation(
        ex: DataIntegrityViolationException,
        request: HttpServletRequest
    ): ApiResponse<Unit> {


        // Log the root cause message securely internally
        var errorMessage: String =
            "Database conflict: The requested operation violates database rules (e.g., duplicate entry or invalid reference)."

        return ApiResponse(
            success = false,
            message = "Data integrity violation occurred"
        )
    }

    @ExceptionHandler(QueryTimeoutException::class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    fun handleQueryTimeout(
        ex: QueryTimeoutException
    ): ApiResponse<Unit> {


        // Log the root cause message securely internally
        var errorMessage: String =
            "Database conflict: The requested operation violates database rules (e.g., duplicate entry or invalid reference)."

        return ApiResponse(
            success = false,
            message = "The database query took too long to respond. Please try again later."
        )
    }
}


class ResourceNotFoundException(message: String) : RuntimeException(message)
class BusinessValidationException(message: String) : RuntimeException(message)
class ResourceAlreadyExistException(message: String) : RuntimeException(message)
class InvalidRefreshTokenException(message: String) : RuntimeException(message)
class PaymentFailedException(message: String) : RuntimeException(message)


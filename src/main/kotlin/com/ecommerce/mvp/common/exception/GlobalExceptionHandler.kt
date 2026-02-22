package com.ecommerce.mvp.common.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.stream.Collectors


@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException, request: HttpServletRequest): ErrorResponse {
        return ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.reasonPhrase,
            ex.message.toString(),
            request.requestURI
        )
        //return ResponseEntity<ErrorResponse>(error, HttpStatus.NOT_FOUND)

    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: MethodArgumentNotValidException, request: HttpServletRequest): ErrorResponse{

        val errorMsg = ex.bindingResult
            .fieldErrors
            .stream()
            .map { e: FieldError? -> e!!.field + ": " + e.defaultMessage }
            .collect(Collectors.joining(", "))

        return ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            errorMsg,
            request.requestURI
        )
       // return ResponseEntity<ErrorResponse?>(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ErrorResponse {
        return ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            "Error: ${ex.message}",
            request.requestURI
        )
        // return ResponseEntity<ErrorResponse?>(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ResourceNotFoundException(message: String) : RuntimeException(message)


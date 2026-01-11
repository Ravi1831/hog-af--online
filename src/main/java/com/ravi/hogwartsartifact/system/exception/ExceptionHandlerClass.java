package com.ravi.hogwartsartifact.system.exception;

import com.ravi.hogwartsartifact.client.imageStorage.exception.BucketNotFoundException;
import com.ravi.hogwartsartifact.client.imageStorage.exception.ImageStorageException;
import com.ravi.hogwartsartifact.client.imageStorage.exception.ImageUploadException;
import com.ravi.hogwartsartifact.client.imageStorage.exception.InvalidFileException;
import com.ravi.hogwartsartifact.client.imageStorage.exception.S3ConnectionException;
import com.ravi.hogwartsartifact.system.Result;
import com.ravi.hogwartsartifact.system.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;
import java.util.stream.Collectors;


@RestControllerAdvice
public class ExceptionHandlerClass {


    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleObjectNotFoundException(ObjectNotFoundException ex) {
        return new Result(false, StatusCode.NOT_FOUND, ex.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    Result handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing + " ; " + replacement
                ));
        return new Result(false, StatusCode.INVALID_ARGUMENT, "Provided Argument are invalid, see data for details", errors);
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handleAuthenticationException(Exception ex) {
        return new Result(false, StatusCode.UNAUTHORIZED, "username or password is incorrect", ex.getMessage());
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handleInsufficientAuthenticationException(InsufficientAuthenticationException ex) {
        return new Result(false, StatusCode.UNAUTHORIZED, "Login credentials are missing", ex.getMessage());
    }

    @ExceptionHandler(AccountStatusException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handleAccountStatusException(AccountStatusException ex) {
        return new Result(false, StatusCode.UNAUTHORIZED, "user account is abnormal", ex.getMessage());
    }

    @ExceptionHandler(InvalidBearerTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handleInvalidBearerTokenException(InvalidBearerTokenException ex) {
        return new Result(false, StatusCode.UNAUTHORIZED, "The access token provided is expired, revoked, malfolded or invalid for other reason", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Result handleAccessDeniedException(AccessDeniedException ex) {
        return new Result(false, StatusCode.FORBIDDEN, "No permission.", ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return new Result(false, StatusCode.NOT_FOUND, "This API endpoint is not found.", ex.getMessage());
    }

    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    ResponseEntity<Result> handleRestClientException(HttpStatusCodeException ex) {
        return new ResponseEntity<>(new Result(false,
                ex.getStatusCode().value(),
                "A rest client error occur, see data for details",
                ex.getMessage()),
                ex.getStatusCode());
    }

    @ExceptionHandler(ResourceAccessException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    Result handleResourceAccessException(ResourceAccessException ex) {
        return new Result(false, StatusCode.SERVICE_UNAVAILABLE,
                "AI service is not available or connection timeout. Please check if the AI service is running.",
                ex.getMessage());
    }

    // S3 / Image Storage Exception Handlers

    @ExceptionHandler(InvalidFileException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result handleInvalidFileException(InvalidFileException ex) {
        return new Result(false, StatusCode.INVALID_ARGUMENT,
                "Invalid file: " + ex.getMessage(),
                ex.getMessage());
    }

    @ExceptionHandler(BucketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleBucketNotFoundException(BucketNotFoundException ex) {
        return new Result(false, StatusCode.NOT_FOUND,
                ex.getMessage(),
                "Please ensure the S3 bucket exists and LocalStack is running");
    }

    @ExceptionHandler(ImageUploadException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Result handleImageUploadException(ImageUploadException ex) {
        return new Result(false, StatusCode.INTERNAL_SERVER_ERROR,
                "Failed to upload image to S3",
                ex.getMessage());
    }

    @ExceptionHandler(S3ConnectionException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    Result handleS3ConnectionException(S3ConnectionException ex) {
        return new Result(false, StatusCode.SERVICE_UNAVAILABLE,
                "Cannot connect to S3 service. Please check if LocalStack/S3 is running.",
                ex.getMessage());
    }

    @ExceptionHandler(ImageStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Result handleImageStorageException(ImageStorageException ex) {
        return new Result(false, StatusCode.INTERNAL_SERVER_ERROR,
                "Image storage error occurred",
                ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Result handleOtherException(Exception ex) {
        return new Result(false, StatusCode.INTERNAL_SERVER_ERROR, "A server internal error occurs", ex.getMessage());
    }

}

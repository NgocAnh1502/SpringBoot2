package com.example.usermanagement.exception;

import com.example.usermanagement.constants.MessageKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import com.example.usermanagement.exception.KeycloakIntegrationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateException(DuplicateResourceException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorDetail)
                .collect(Collectors.toList());
        String message = messageSource.getMessage(MessageKey.ERROR_VALIDATION, null, LocaleContextHolder.getLocale());
        return buildResponse(HttpStatus.BAD_REQUEST, message, details);
    }

    private FieldErrorDetail toFieldErrorDetail(FieldError fieldError) {
        String code = fieldError.getDefaultMessage();
        String resolvedMessage;
        try {
            resolvedMessage = messageSource.getMessage(code, fieldError.getArguments(), LocaleContextHolder.getLocale());
        } catch (Exception e) {
            resolvedMessage = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
        }
        return FieldErrorDetail.builder()
                .field(fieldError.getField())
                .message(resolvedMessage)
                .build();
    }

    @ExceptionHandler(KeycloakIntegrationException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakIntegration(KeycloakIntegrationException ex) {
        log.error("Keycloak integration error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), null);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakCallFailed(RestClientException ex) {
        log.error("Gọi Keycloak Admin API thất bại: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage("error.keycloak.unavailable", null, LocaleContextHolder.getLocale());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, message, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Lỗi không xác định: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage(MessageKey.ERROR_INTERNAL, null, LocaleContextHolder.getLocale());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, List<FieldErrorDetail> details){
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .details(details)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}

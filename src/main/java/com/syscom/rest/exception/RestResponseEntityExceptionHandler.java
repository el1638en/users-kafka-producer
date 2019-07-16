package com.syscom.rest.exception;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.syscom.exceptions.BusinessException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	private final Logger loggerFactory = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = { BusinessException.class })
	protected ResponseEntity<Object> handleBusinessException(BusinessException businessException, WebRequest request) {
		logError(businessException);
		return handleExceptionInternal(businessException, getErrorDetails(businessException, request),
				new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(value = { AccessDeniedException.class })
	protected ResponseEntity<Object> handleForbiddenException(AccessDeniedException accessDeniedException,
			WebRequest request) {
		logError(accessDeniedException);
		return handleExceptionInternal(accessDeniedException, getErrorDetails(accessDeniedException, request),
				new HttpHeaders(), HttpStatus.FORBIDDEN, request);
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler(value = { IllegalArgumentException.class, IllegalStateException.class })
	protected ResponseEntity<Object> handleConflict(RuntimeException runtimeException, WebRequest request) {
		logError(runtimeException);
		return handleExceptionInternal(runtimeException, getErrorDetails(runtimeException, request), new HttpHeaders(),
				HttpStatus.CONFLICT, request);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = { Exception.class })
	protected ResponseEntity<Object> handleException(RuntimeException runtimeException, WebRequest request) {
		logError(runtimeException);
		return handleExceptionInternal(runtimeException, "Internal Server error. Try later !!", new HttpHeaders(),
				HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	private void logError(Exception exception) {
		loggerFactory.error(exception.getMessage(), exception);
	}

	private ErrorDetails getErrorDetails(Exception exception, WebRequest request) {
		return new ErrorDetails(LocalDateTime.now(), exception.getMessage(), request.getDescription(false));

	}

}

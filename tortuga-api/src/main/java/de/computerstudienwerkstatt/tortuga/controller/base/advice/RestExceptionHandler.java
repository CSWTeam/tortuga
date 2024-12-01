package de.computerstudienwerkstatt.tortuga.controller.base.advice;

import de.computerstudienwerkstatt.tortuga.controller.base.response.NotFoundResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import de.computerstudienwerkstatt.tortuga.controller.base.response.RestResponse;
import de.computerstudienwerkstatt.tortuga.security.token.TokenException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Mischa Holz
 */
@ControllerAdvice
@EnableWebMvc
public class RestExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    public static class ErrorResponse {

        private int errorCode;
        private String errorMessage;

        public ErrorResponse() {
        }

        public ErrorResponse(int errorCode, String errorMessage) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static class ValidationError extends ErrorResponse {

        public static String GLOBAL_ERROR_KEY = "_GLOBAL";

        private Map<String, String> errors = new LinkedHashMap<>();

        public ValidationError() {
        }

        public ValidationError(ConstraintViolationException e) {
            super(400, "Could not validate object");

            for(ConstraintViolation cv : e.getConstraintViolations()) {
                String key = cv.getPropertyPath().toString();
                if(key.isEmpty()) {
                    key = GLOBAL_ERROR_KEY;
                }

                String value = cv.getMessage();

                errors.put(key, value);
            }
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }

    private ResponseEntity<ErrorResponse> handleException(HttpStatus defaultStatus, String defaultMessage, Exception e) {

        LOGGER.warn("Handling REST exception: {} - {}", e.getClass().getSimpleName(), e.getMessage());

        HttpStatus errorCode = defaultStatus;
        String errorMessage = defaultMessage;

        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            errorCode = responseStatus.value();
            errorMessage = responseStatus.reason();
        }

        return new ResponseEntity<>(new ErrorResponse(errorCode.value(), errorMessage), errorCode);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> methodNotAllowedHandler(Exception e) {
        return handleException(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", e);
    }

    @ExceptionHandler(TokenException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> tokenExceptionHandler(Exception e) {
        return handleException(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
    }

    @ExceptionHandler(RestResponse.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> restExceptionHandler(Exception e) throws Exception {
        return handleException(((RestResponse) e).getStatus(), e.getMessage(), e);
    }

    @ExceptionHandler(NotFoundResponse.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> notFoundExceptionHandler(Exception e) throws Exception {
        String msg = e.getMessage();
        if(msg == null || msg.isEmpty()) {
            msg = "Resource not found";
        }
        return handleException(HttpStatus.NOT_FOUND, msg, e);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> emptyResultDataAccessExceptionHandler(Exception e) throws Exception {
        return notFoundExceptionHandler(e);
    }

    @ExceptionHandler(TypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(Exception e) {
        return handleException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleJsonMappingException(Exception e) {
        return handleException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(Exception e) {
        return handleException(HttpStatus.BAD_REQUEST, "Bad Request", e);
    }

    @ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleUnsatisfiedServletRequestParameterException(UnsatisfiedServletRequestParameterException e) {
        String message = "Parameter(s) missing or wrong: ";
        String delim = "";
        for (String param : e.getParamConditions()) {
            message += delim + "'" + param + "'";
            delim = ", ";
        }
        return handleException(HttpStatus.BAD_REQUEST, message, e);
    }

    @ExceptionHandler(TransactionSystemException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(Exception e, HttpServletResponse response) {
        Throwable t = e;
        while(t.getCause() != null) {
            t = t.getCause();
        }

        if(t instanceof ConstraintViolationException) {
            return new ResponseEntity<>(new ValidationError((ConstraintViolationException) t), HttpStatus.BAD_REQUEST);
        }

        return handleGeneralApiExceptions(e, response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception e) {
        LOGGER.info("Access Denied:" + e.getMessage());
        LOGGER.info("Access Denied", e);
        return handleException(HttpStatus.FORBIDDEN, "Access Denied", e);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleGeneralApiExceptions(Exception e, HttpServletResponse response) {
        if(!(e instanceof ClientAbortException)) {
            LOGGER.error("Unexpected exception", e);
        }
        if(response.isCommitted()) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));

        return handleException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage() + "\n\n" + baos.toString(), e);
    }
}

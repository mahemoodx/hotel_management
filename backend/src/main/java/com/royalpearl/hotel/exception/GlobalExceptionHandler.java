package com.royalpearl.hotel.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RFC 7807 Problem+JSON responses for every error path.
 * Content-Type: application/problem+json
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String PROBLEM_BASE = "https://royalpearlhyderabad.com/problems";

    // ---------------------------------------------------------------
    // 400 – Validation (@Valid bean-validation failures)
    // ---------------------------------------------------------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage(),
                        (a, b) -> a,           // keep first if duplicate field
                        LinkedHashMap::new
                ));

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setType(URI.create(PROBLEM_BASE + "/validation-error"));
        problem.setTitle("Validation Failed");
        problem.setDetail("One or more fields failed validation");
        problem.setProperty("errors", fieldErrors);
        problem.setProperty("timestamp", OffsetDateTime.now().toString());

        return ResponseEntity.unprocessableEntity()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(PROBLEM_BASE + "/malformed-request"));
        problem.setTitle("Malformed Request Body");
        problem.setDetail("Request body could not be parsed. Check JSON syntax and field types.");
        problem.setProperty("timestamp", OffsetDateTime.now().toString());

        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(PROBLEM_BASE + "/missing-parameter"));
        problem.setTitle("Missing Request Parameter");
        problem.setDetail("Required parameter '" + ex.getParameterName() + "' is missing");
        problem.setProperty("timestamp", OffsetDateTime.now().toString());

        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    // ---------------------------------------------------------------
    // 400 – Bad request (application-level)
    // ---------------------------------------------------------------

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(BadRequestException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(PROBLEM_BASE + "/bad-request"));
        problem.setTitle("Bad Request");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", OffsetDateTime.now().toString());
        return problem(problem);
    }

    // ---------------------------------------------------------------
    // 401 – Unauthenticated
    // ---------------------------------------------------------------

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ProblemDetail> handleAuthException(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setType(URI.create(PROBLEM_BASE + "/unauthorized"));
        problem.setTitle("Unauthorized");
        problem.setDetail("Invalid credentials or authentication token");
        problem.setProperty("timestamp", OffsetDateTime.now().toString());
        return problem(problem);
    }

    // ---------------------------------------------------------------
    // 403 – Forbidden
    // ---------------------------------------------------------------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setType(URI.create(PROBLEM_BASE + "/forbidden"));
        problem.setTitle("Forbidden");
        problem.setDetail("You do not have permission to access this resource");
        problem.setProperty("timestamp", OffsetDateTime.now().toString());
        return problem(problem);
    }

    // ---------------------------------------------------------------
    // 404 – Not found
    // ---------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create(PROBLEM_BASE + "/not-found"));
        problem.setTitle("Not Found");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", OffsetDateTime.now().toString());
        return problem(problem);
    }

    // ---------------------------------------------------------------
    // 409 – Conflict
    // ---------------------------------------------------------------

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setType(URI.create(PROBLEM_BASE + "/conflict"));
        problem.setTitle("Conflict");
        problem.setDetail(ex.getMessage());
        problem.setProperty("timestamp", OffsetDateTime.now().toString());
        return problem(problem);
    }

    // ---------------------------------------------------------------
    // 400 – Type mismatch (e.g. invalid UUID path variable)
    // ---------------------------------------------------------------

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create(PROBLEM_BASE + "/type-mismatch"));
        problem.setTitle("Invalid Parameter Type");
        problem.setDetail("Parameter '" + ex.getName() + "' has invalid value: " + ex.getValue());
        problem.setProperty("timestamp", OffsetDateTime.now().toString());
        return problem(problem);
    }

    // ---------------------------------------------------------------
    // 500 – Unexpected errors
    // ---------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAll(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}: {}", request.getMethod(),
                request.getRequestURI(), ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create(PROBLEM_BASE + "/internal-server-error"));
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred. Please try again later.");
        problem.setProperty("timestamp", OffsetDateTime.now().toString());
        return problem(problem);
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    private static ResponseEntity<ProblemDetail> problem(ProblemDetail pd) {
        return ResponseEntity.status(pd.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }
}

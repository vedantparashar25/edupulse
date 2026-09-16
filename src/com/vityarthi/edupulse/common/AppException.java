package com.vityarthi.edupulse.common;

/**
 * Root domain exception hierarchy for the EduPulse platform.
 */
public class AppException extends Exception {
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class AuthenticationException extends AppException {
        public AuthenticationException(String message) { super(message); }
    }

    public static class AuthorizationException extends AppException {
        public AuthorizationException(String message) { super(message); }
    }

    public static class ResourceNotFoundException extends AppException {
        public ResourceNotFoundException(String message) { super(message); }
    }

    public static class ValidationException extends AppException {
        public ValidationException(String message) { super(message); }
    }

    public static class CourseCapacityException extends AppException {
        public CourseCapacityException(String message) { super(message); }
    }

    public static class DuplicateResourceException extends AppException {
        public DuplicateResourceException(String message) { super(message); }
    }
}

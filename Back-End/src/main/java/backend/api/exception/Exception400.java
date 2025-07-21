package backend.api.exception;

public class Exception400 {

    public static class BadRequestException extends CustomException {
        public BadRequestException(String tip, String nume, String descriere) {
            super(tip, nume, descriere);
        }

        public BadRequestException(String tip, String nume, Throwable cauza) {
            super(tip, nume, cauza);
        }
    }

    public static class UnauthorizedException extends CustomException {
        public UnauthorizedException(String tip, String nume, String descriere) {
            super(tip, nume, descriere);
        }
    }

    public static class ForbiddenException extends CustomException {
        public ForbiddenException(String tip, String nume, String descriere) {
            super(tip, nume, descriere);
        }
    }

    public static class NotFoundException extends CustomException {
        public NotFoundException(String tip, String nume, String descriere) {
            super(tip, nume, descriere);
        }

    }

    public static class MethodNotAllowedException extends CustomException {
        public MethodNotAllowedException(String tip, String nume, String descriere) {
            super(tip, nume, descriere);
        }
    }

    public static class ConflictException extends CustomException {
        public ConflictException(String tip, String nume, String descriere) {
            super(tip, nume, descriere);
        }
    }
}
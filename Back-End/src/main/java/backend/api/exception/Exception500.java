package backend.api.exception;

public class Exception500 {

    public static class InternalServerErrorException extends CustomException {
        public InternalServerErrorException(String tip, String nume, String descriere) {
            super(tip, nume, descriere);
        }
        public InternalServerErrorException (String tip, String nume, String descriere,Throwable cauza) {
            super(tip, nume, cauza);
        }
    }

    public static class BadGatewayException extends CustomException {
        public BadGatewayException(String tip, String nume, String descriere) {
            super(tip, nume, descriere);
        }

    }

    public static class ServiceUnavailableException extends CustomException {
        public ServiceUnavailableException(String tip, String nume, String descriere) {
            super(tip, nume, descriere);
        }

    }

}
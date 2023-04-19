package co.za.entelect.exceptions;

public class DateException extends RuntimeException {
    public DateException(String message) {
        super(message);
    }

    public static class InvalidDateFormatException extends DateException {
        public InvalidDateFormatException(String message) {
            super(message);
        }
    }

    public static class DateInPastException extends DateException {
        public DateInPastException(String message) {
            super(message);
        }
    }

    public static class DateBeforeStartDateException extends DateException {
        public DateBeforeStartDateException(String message) {
            super(message);
        }
    }

}

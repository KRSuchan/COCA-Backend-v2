package project.coca.base.exception;

public class AlreadyReportedException extends RuntimeException {
    public AlreadyReportedException(String message) {
        super(message);
    }
}

package project.coca.common.exception;

public class AlreadyReportedException extends RuntimeException {
    public AlreadyReportedException(String message) {
        super(message);
    }
}

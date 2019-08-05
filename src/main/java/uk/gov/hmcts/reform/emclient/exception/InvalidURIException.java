package uk.gov.hmcts.reform.emclient.exception;

public class InvalidURIException extends RuntimeException {
    private static final long serialVersionUID = 8758617259382387538L;

    public InvalidURIException(String message) {
        super(message);
    }
}

package uk.gov.hmcts.reform.emclient.exception;

public class InvalidUriException extends RuntimeException {
    private static final long serialVersionUID = 8758617259382387538L;

    public InvalidUriException(String message) {
        super(message);
    }
}

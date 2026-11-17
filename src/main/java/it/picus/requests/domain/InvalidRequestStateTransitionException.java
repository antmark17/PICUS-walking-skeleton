package it.picus.requests.domain;

public class InvalidRequestStateTransitionException extends IllegalStateException {
    public InvalidRequestStateTransitionException(
            RequestStatus currentStatus,
            RequestStatus targetStatus) {
        super("Transizione non consentita da " + currentStatus + " a " + targetStatus);
    }
}

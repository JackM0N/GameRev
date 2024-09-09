package pl.ttsw.GameRev.enums;

public enum CompletionStatus {
    COMPLETED("Completed"),
    IN_PROGRESS("In-progress"),
    ON_HOLD("On-hold"),
    PLANNING("Planning"),
    DROPPED("Dropped");

    final String status;

    CompletionStatus(String completionStatus) {
        this.status = completionStatus;
    }
}

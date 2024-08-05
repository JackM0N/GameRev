package pl.ttsw.GameRev.enums;

public enum ReleaseStatus {
    RELEASED("Released"),
    EARLY_ACCESS("Early Access"),
    ANNOUNCED("Announced"),
    CANCELED("Canceled"),
    END_OF_SERVICE("End Of Service");

    String status;

    ReleaseStatus(String status) {
        this.status = status;
    }
}

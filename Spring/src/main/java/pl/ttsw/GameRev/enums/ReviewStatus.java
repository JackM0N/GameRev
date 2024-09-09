package pl.ttsw.GameRev.enums;

public enum ReviewStatus {
    APPROVED("Approved"),
    PENDING("Pending"),
    DELETED("Deleted"),
    EDITED("Edited");

    final String status;

    ReviewStatus(String status) {
        this.status = status;
    }
}

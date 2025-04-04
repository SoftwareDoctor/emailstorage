package it.softwaredoctor.emailstorage.model;

public enum EmailStatus {
    LETTA,
    ELABORATA;

    public static EmailStatus fromString(String status) {
        for (EmailStatus emailStatus : EmailStatus.values()) {
            if (emailStatus.name().equalsIgnoreCase(status)) {
                return emailStatus;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }
}

package ai.xenopilot.channel;

public enum CommunicationStatus {

    PENDING(0),
    SENT(1),
    DELIVERED(2),
    READ(3),
    CLICKED(4),
    CONVERTED(5),
    FAILED(99);

    private final int rank;

    CommunicationStatus(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public boolean canTransitionTo(CommunicationStatus target) {

        // FAILED is terminal
        if (this == FAILED) {
            return false;
        }

        // Cannot move backwards
        if (target.getRank() <= this.getRank()) {
            return false;
        }

        return true;
    }
}
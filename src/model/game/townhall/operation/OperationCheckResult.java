package model.game.townhall.operation;

// Immutable result carrying its own contextual message
public final class OperationCheckResult {

    private static final String DEFAULT_IMPOSSIBLE_MESSAGE = "Impossible operation!";

    private final OperationStatus status;
    private final String message;

    private OperationCheckResult(OperationStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public static OperationCheckResult possible() {
        return new OperationCheckResult(OperationStatus.POSSIBLE, null);
    }

    public static OperationCheckResult impossible(String message) {
        return new OperationCheckResult(OperationStatus.IMPOSSIBLE, message);
    }

    public static OperationCheckResult impossible() {
        return impossible(DEFAULT_IMPOSSIBLE_MESSAGE);
    }

    public String getMessage() {
        return message;
    }

    public boolean isPossible() {
        return status == OperationStatus.POSSIBLE;
    }

    public OperationStatus getStatus() {
        return status;
    }
}

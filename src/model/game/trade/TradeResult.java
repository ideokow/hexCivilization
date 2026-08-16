package model.game.trade;

public final class TradeResult {

    private final TradeStatus status;
    private final int soldAmount;
    private final int receivedAmount;
    private final int addedAmount;
    private final int conversionRatePercent;

    private TradeResult(
            TradeStatus status,
            int soldAmount,
            int receivedAmount,
            int addedAmount,
            int conversionRatePercent
    ) {
        this.status = status;
        this.soldAmount = soldAmount;
        this.receivedAmount = receivedAmount;
        this.addedAmount = addedAmount;
        this.conversionRatePercent = conversionRatePercent;
    }

    public static TradeResult success(
            int soldAmount,
            int receivedAmount,
            int addedAmount,
            int conversionRatePercent
    ) {
        return new TradeResult(
                TradeStatus.SUCCESS,
                soldAmount,
                receivedAmount,
                addedAmount,
                conversionRatePercent
        );
    }

    public static TradeResult failure(TradeStatus status) {
        if (status == null || status == TradeStatus.SUCCESS) {
            throw new IllegalArgumentException("A failure must have a non-success status.");
        }
        return new TradeResult(status, 0, 0, 0, 0);
    }

    public TradeStatus getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == TradeStatus.SUCCESS;
    }

    public boolean isSuccessful() {
        return isSuccess();
    }

    public int getSoldAmount() {
        return soldAmount;
    }

    public int getAmountSold() {
        return soldAmount;
    }

    public int getReceivedAmount() {
        return receivedAmount;
    }

    public int getAmountReceived() {
        return receivedAmount;
    }

    public int getAddedAmount() {
        return addedAmount;
    }

    public int getStoredAmount() {
        return addedAmount;
    }

    public int getConversionRatePercent() {
        return conversionRatePercent;
    }

    public double getConversionRate() {
        return conversionRatePercent / 100.0;
    }

    public String getMessage() {
        return status.getMessage();
    }
}

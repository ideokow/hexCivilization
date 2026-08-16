package model.game.trade;

public interface TradeStrategy {

    int getConversionRatePercent();

    int getSoldAmount(int requestedAmount);

    default int calculateReceivedAmount(int soldAmount) {
        return (int) (((long) soldAmount * getConversionRatePercent()) / 100L);
    }
}

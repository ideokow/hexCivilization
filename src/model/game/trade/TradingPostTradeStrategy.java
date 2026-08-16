package model.game.trade;

public final class TradingPostTradeStrategy implements TradeStrategy {

    public static final int CONVERSION_RATE_PERCENT = 80;

    @Override
    public int getConversionRatePercent() {
        return CONVERSION_RATE_PERCENT;
    }

    @Override
    public int getSoldAmount(int requestedAmount) {
        return requestedAmount;
    }
}

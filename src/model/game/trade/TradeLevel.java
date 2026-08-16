package model.game.trade;

public enum TradeLevel {
    LEVEL_1(10, 50),
    LEVEL_2(100, 60),
    LEVEL_3(500, 70);

    private final int soldAmount;
    private final int conversionRatePercent;

    TradeLevel(int soldAmount, int conversionRatePercent) {
        this.soldAmount = soldAmount;
        this.conversionRatePercent = conversionRatePercent;
    }

    public int getSoldAmount() {
        return soldAmount;
    }

    public int getAmountSold() {
        return soldAmount;
    }

    public int getConversionRatePercent() {
        return conversionRatePercent;
    }

    public double getConversionRate() {
        return conversionRatePercent / 100.0;
    }
}

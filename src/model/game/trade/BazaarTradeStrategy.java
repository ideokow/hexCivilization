package model.game.trade;

import java.util.Objects;

public final class BazaarTradeStrategy implements TradeStrategy {

    private final TradeLevel level;

    public BazaarTradeStrategy(TradeLevel level) {
        this.level = Objects.requireNonNull(level, "level");
    }

    @Override
    public int getConversionRatePercent() {
        return level.getConversionRatePercent();
    }

    @Override
    public int getSoldAmount(int requestedAmount) {
        return level.getSoldAmount();
    }

    public TradeLevel getLevel() {
        return level;
    }
}

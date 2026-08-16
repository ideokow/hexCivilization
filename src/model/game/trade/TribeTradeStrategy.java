package model.game.trade;

import model.game.tribe.Tribe;
import model.game.tribe.TribeType;

import java.util.Objects;

public final class TribeTradeStrategy implements TradeStrategy {

    public static final int STANDARD_RATE_PERCENT = 75;
    public static final int MERCHANT_RATE_PERCENT = 80;
    public static final int TRADE_ROUTE_BONUS_PERCENT = 10;
    public static final int TRADE_RELATION_INCREASE = 1;

    private final Tribe tribe;

    public TribeTradeStrategy(Tribe tribe) {
        this.tribe = Objects.requireNonNull(tribe, "tribe");
    }

    @Override
    public int getConversionRatePercent() {
        if (tribe.getTribeType() == TribeType.TRADER) {
            int rate = MERCHANT_RATE_PERCENT;
            if (tribe.isHadTradeRouteMission()) {
                rate += TRADE_ROUTE_BONUS_PERCENT;
            }
            return Math.min(100, rate);
        }
        return STANDARD_RATE_PERCENT;
    }

    @Override
    public int getSoldAmount(int requestedAmount) {
        return requestedAmount;
    }

    public Tribe getTribe() {
        return tribe;
    }
}

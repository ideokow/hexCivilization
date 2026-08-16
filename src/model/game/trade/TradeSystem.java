package model.game.trade;

import model.game.building.Bazaar;
import model.game.building.TradingPost;
import model.game.hex.Resource;
import model.game.player.Player;
import model.game.townhall.TownHall;
import model.game.tribe.Tribe;
import model.game.tribe.TribeType;

import java.util.Objects;

public class TradeSystem {

    private final TownHall townHall;
    private final Player player;

    private int currentTurn = 1;
    private int lastBazaarTradeTurn = -1;
    private int lastTradingPostTradeTurn = -1;

    public TradeSystem(TownHall townHall, Player player) {
        this.townHall = Objects.requireNonNull(townHall, "townHall");
        this.player = player;
    }

    public TradeSystem(TownHall townHall) {
        this(townHall, null);
    }

    public TradeResult tradeAtBazaar(
            Bazaar bazaar,
            Resource soldResource,
            Resource receivedResource,
            TradeLevel level
    ) {
        return tradeAtBazaar(
                bazaar,
                soldResource,
                receivedResource,
                level,
                currentTurn
        );
    }

    public TradeResult tradeAtBazaar(
            Bazaar bazaar,
            Resource soldResource,
            Resource receivedResource,
            TradeLevel level,
            int turn
    ) {
        if (bazaar == null || level == null) {
            return TradeResult.failure(TradeStatus.NULL_ARGUMENT);
        }
        if (turn < 0) {
            return TradeResult.failure(TradeStatus.INVALID_TURN);
        }
        currentTurn = turn;
        if (!bazaar.isAvailableForTrade(townHall)) {
            if (townHall.getLevel().getLevelN() < 2) {
                return TradeResult.failure(TradeStatus.BAZAAR_LEVEL_REQUIRED);
            }
            return TradeResult.failure(TradeStatus.BAZAAR_NOT_AVAILABLE);
        }
        if (player != null && !player.ownsTerritory(bazaar.getPosition())) {
            return TradeResult.failure(TradeStatus.BAZAAR_OUTSIDE_TERRITORY);
        }
        if (lastBazaarTradeTurn == turn) {
            return TradeResult.failure(TradeStatus.BAZAAR_ALREADY_USED_THIS_TURN);
        }

        TradeResult result = executeTrade(
                soldResource,
                receivedResource,
                new BazaarTradeStrategy(level),
                0
        );
        if (result.isSuccess()) {
            lastBazaarTradeTurn = turn;
        }
        return result;
    }

    public TradeResult tradeAtTradingPost(
            TradingPost tradingPost,
            Resource soldResource,
            Resource receivedResource,
            int amount
    ) {
        return tradeAtTradingPost(
                tradingPost,
                soldResource,
                receivedResource,
                amount,
                currentTurn
        );
    }

    public TradeResult tradeAtTradingPost(
            TradingPost tradingPost,
            Resource soldResource,
            Resource receivedResource,
            int amount,
            int turn
    ) {
        if (tradingPost == null) {
            return TradeResult.failure(TradeStatus.NULL_ARGUMENT);
        }
        if (turn < 0) {
            return TradeResult.failure(TradeStatus.INVALID_TURN);
        }
        currentTurn = turn;
        if (!tradingPost.isAvailableForTrade()) {
            return TradeResult.failure(TradeStatus.TRADING_POST_NOT_AVAILABLE);
        }
        if (player == null || !player.ownsTerritory(tradingPost.getPosition())) {
            return TradeResult.failure(TradeStatus.TRADING_POST_OUTSIDE_TERRITORY);
        }
        if (lastTradingPostTradeTurn == turn) {
            return TradeResult.failure(TradeStatus.TRADING_POST_ALREADY_USED_THIS_TURN);
        }

        TradeResult result = executeTrade(
                soldResource,
                receivedResource,
                new TradingPostTradeStrategy(),
                amount
        );
        if (result.isSuccess()) {
            lastTradingPostTradeTurn = turn;
        }
        return result;
    }

    public TradeResult tradeWithTribe(
            Tribe tribe,
            Resource soldResource,
            Resource receivedResource,
            int amount
    ) {
        return tradeWithTribe(
                tribe,
                soldResource,
                receivedResource,
                amount,
                currentTurn
        );
    }

    public TradeResult tradeWithTribe(
            Tribe tribe,
            Resource soldResource,
            Resource receivedResource,
            int amount,
            int turn
    ) {
        if (tribe == null) {
            return TradeResult.failure(TradeStatus.NULL_ARGUMENT);
        }
        if (turn < 0) {
            return TradeResult.failure(TradeStatus.INVALID_TURN);
        }
        currentTurn = turn;
        if (tribe.getRelatedTownHall() != townHall) {
            return TradeResult.failure(TradeStatus.WRONG_TOWN_HALL);
        }
        if (!tribe.canTrade()) {
            return TradeResult.failure(TradeStatus.TRIBE_NOT_AVAILABLE);
        }
        if (tribe.hasTradedThisTurn(turn)) {
            return TradeResult.failure(TradeStatus.TRIBE_ALREADY_USED_THIS_TURN);
        }
        if (!supportsReceivedResource(tribe.getTribeType(), receivedResource)) {
            return TradeResult.failure(TradeStatus.TRIBE_RESOURCE_NOT_SUPPORTED);
        }

        TradeResult result = executeTrade(
                soldResource,
                receivedResource,
                new TribeTradeStrategy(tribe),
                amount
        );
        if (result.isSuccess()) {
            tribe.markTrade(turn);
            tribe.increaseRelation(TribeTradeStrategy.TRADE_RELATION_INCREASE);
        }
        return result;
    }

    public void setCurrentTurn(int currentTurn) {
        if (currentTurn < 0) {
            throw new IllegalArgumentException("Turn cannot be negative.");
        }
        this.currentTurn = currentTurn;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    private TradeResult executeTrade(
            Resource soldResource,
            Resource receivedResource,
            TradeStrategy strategy,
            int requestedAmount
    ) {
        if (soldResource == null || receivedResource == null || strategy == null) {
            return TradeResult.failure(TradeStatus.NULL_ARGUMENT);
        }
        if (soldResource == receivedResource) {
            return TradeResult.failure(TradeStatus.INVALID_RESOURCE_PAIR);
        }

        int soldAmount = strategy.getSoldAmount(requestedAmount);
        if (soldAmount <= 0) {
            return TradeResult.failure(TradeStatus.INVALID_AMOUNT);
        }
        if (townHall.getResourceAmount(soldResource) < soldAmount) {
            return TradeResult.failure(TradeStatus.INSUFFICIENT_RESOURCES);
        }

        int receivedAmount = strategy.calculateReceivedAmount(soldAmount);
        townHall.deductResource(soldResource, soldAmount);
        int addedAmount = townHall.addResource(receivedResource, receivedAmount);

        return TradeResult.success(
                soldAmount,
                receivedAmount,
                addedAmount,
                strategy.getConversionRatePercent()
        );
    }

    private boolean supportsReceivedResource(TribeType tribeType, Resource receivedResource) {
        if (tribeType == null || receivedResource == null) {
            return false;
        }
        return switch (tribeType) {
            case FARMER, COASTAL -> receivedResource == Resource.FOOD;
            case MOUNTAINEER -> receivedResource == Resource.STONE
                    || receivedResource == Resource.IRON;
            case TRADER -> true;
            case FIGHTER -> false;
        };
    }
}

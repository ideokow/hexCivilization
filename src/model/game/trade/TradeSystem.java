package model.game.trade;

import model.game.building.Bazaar;
import model.game.building.TradingPost;
import model.game.hex.Resource;
import model.game.player.Player;
import model.game.townhall.TownHall;
import model.game.tribe.Tribe;

public class TradeSystem {

    private int currentTurn = 1;
    private int lastBazaarTradeTurn = -1;
    private int lastTradingPostTradeTurn = -1;

    public TradeSystem() {}

    public TradeResult tradeAtBazaar(
            TownHall townHall,
            Player player,
            Bazaar bazaar,
            Resource soldResource,
            Resource receivedResource,
            TradeLevel level
    ) {
        return tradeAtBazaar(
                townHall,
                player,
                bazaar,
                soldResource,
                receivedResource,
                level,
                currentTurn
        );
    }

    public TradeResult tradeAtBazaar(
            TownHall townHall,
            Player player,
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
                townHall,
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
            TownHall townHall,
            Player player,
            TradingPost tradingPost,
            Resource soldResource,
            Resource receivedResource,
            int amount
    ) {
        return tradeAtTradingPost(
                townHall,
                player,
                tradingPost,
                soldResource,
                receivedResource,
                amount,
                currentTurn
        );
    }

    public TradeResult tradeAtTradingPost(
            TownHall townHall,
            Player player,
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
                townHall,
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
            TownHall townHall,
            Tribe tribe,
            Resource soldResource,
            Resource receivedResource,
            int amount
    ) {
        return tradeWithTribe(
                townHall,
                tribe,
                soldResource,
                receivedResource,
                amount,
                currentTurn
        );
    }

    public TradeResult tradeWithTribe(
            TownHall townHall,
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
        if (!tribe.canProvideResource(receivedResource)) {
            return TradeResult.failure(TradeStatus.TRIBE_RESOURCE_NOT_SUPPORTED);
        }

        TradeResult result = executeTrade(
                townHall,
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

    public boolean hasTradedAtBazaarThisTurn() {
        return lastBazaarTradeTurn == currentTurn;
    }

    public boolean hasTradedAtTradingPostThisTurn() {
        return lastTradingPostTradeTurn == currentTurn;
    }

    public boolean hasTradedWithTribeThisTurn(Tribe tribe) {
        return tribe != null && tribe.hasTradedThisTurn(currentTurn);
    }

    private TradeResult executeTrade(
            TownHall townHall,
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
        if (townHall.getAvailableStorage(receivedResource) < receivedAmount) {
            return TradeResult.failure(TradeStatus.INSUFFICIENT_STORAGE);
        }

        townHall.deductResource(soldResource, soldAmount);
        int addedAmount = townHall.addResource(receivedResource, receivedAmount);

        return TradeResult.success(
                soldAmount,
                receivedAmount,
                addedAmount,
                strategy.getConversionRatePercent()
        );
    }
}

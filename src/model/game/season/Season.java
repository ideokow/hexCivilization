package model.game.season;

public class Season {
    public static SeasonName getSeason(int turn) {
        SeasonName[] seasons = new SeasonName[]{
                SeasonName.SPRING,
                SeasonName.SUMMER,
                SeasonName.FALL,
                SeasonName.WINTER,
        };
        return seasons[(turn / 10) % 4];
    }
}

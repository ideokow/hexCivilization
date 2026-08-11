package model.game.season;

public enum SeasonName {
    SPRING("Spring"),
    SUMMER("Summer"),
    FALL("Fall"),
    WINTER("Winter");

    private final String name;

    SeasonName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

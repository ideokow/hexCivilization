package model.game.happiness;

public enum Era {
    GOLDEN_ERA
            ("Golden Age!"),
    NORMAL_ERA
            ("Normal Public Consent"),
    DISCONTENT_ERA
            ("Discontent Era"),
    REBELLION_ERA
            ("Rebellion!");

    private final String name;

    Era(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

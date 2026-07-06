package model.game;

/*
resources which are available in hex
 */
public enum HexResource {
    // Produced by forest hexes
    WOOD("Wood"),

    // Produced by mountain hexes (all mountains have stone)
    STONE("Stone"),

    // Produced by some mountain hexes (not guaranteed)
    IRON("Iron"),

    // Produced by grassland (wheat/rice) and plains (cattle/sheep) hexes
    FOOD("Food");

    private final String displayName;

    HexResource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

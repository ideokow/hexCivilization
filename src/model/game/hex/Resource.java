package model.game.hex;

/*
 * Four primary material types in the game
 */
public enum Resource {
    WOOD("Wood"),
    STONE("Stone"),
    IRON("Iron"),
    FOOD("Food");

    private final String displayName;

    Resource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

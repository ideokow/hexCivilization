package model.game.happiness;

import model.game.building.Building;
import model.game.building.Monument;
import model.game.registry.BuildingRegistry;

import java.util.Collection;

public class Happiness {

    private int value;

    public Happiness() {
        value = 0;
    }

    public void addHappiness(int amount) {
        value += amount;
    }

    public void checkMonuments() {
        Collection<Building> allBuildings = BuildingRegistry.getInstance().getBuildingMap().values();
        for (Building building: allBuildings) {
            if (building instanceof Monument) {
                addHappiness(2);
            }
        }
    }

    public Era getEra() {
        if (value >= 3) {
            return Era.GOLDEN_ERA;
        }
        else if (value >= -2) {
            return Era.NORMAL_ERA;
        }
        else if (value >= -4) {
            return Era.DISCONTENT_ERA;
        }
        else {
            return Era.REBELLION_ERA;
        }
    }

    public int getValue() {
        return value;
    }
}

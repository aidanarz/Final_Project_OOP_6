package com.NetRoyale.patterns.factory;

import com.NetRoyale.models.UnitData;
import com.NetRoyale.models.UnitColor;
import java.util.HashMap;
import java.util.Map;

// Factory + Singleton untuk unit data
public class UnitFactory {
    private static UnitFactory instance;
    private Map<String, UnitData> unitRegistry;

    private UnitFactory() {
        unitRegistry = new HashMap<>();
        initializeUnits();
    }

    public static UnitFactory getInstance() {
        if (instance == null) {
            instance = new UnitFactory();
        }
        return instance;
    }

    private void initializeUnits() {
        // Register all units - matching HTML data exactly
        unitRegistry.put("knight", new UnitData("Knight", "⚔️", 3, 120, 15, 40f, 30f, 1.0f, 15f, "melee", UnitColor.KNIGHT));
        unitRegistry.put("archer", new UnitData("Archer", "🏹", 3, 70, 12, 50f, 180f, 0.8f, 12f, "ranged", UnitColor.ARCHER));
        unitRegistry.put("giant", new UnitData("Giant", "🦍", 5, 500, 30, 20f, 40f, 1.5f, 24f, "tank", UnitColor.GIANT));
        unitRegistry.put("goblin", new UnitData("Goblin", "👺", 2, 50, 20, 75f, 25f, 0.7f, 10f, "fast", UnitColor.GOBLIN));
        unitRegistry.put("skeleton", new UnitData("Skelly", "💀", 1, 30, 10, 55f, 25f, 1.0f, 8f, "swarm", UnitColor.SKELETON));
        unitRegistry.put("wizard", new UnitData("Wizard", "🧙‍♂️", 5, 90, 40, 35f, 160f, 1.4f, 14f, "splash", UnitColor.WIZARD));
        unitRegistry.put("golem", new UnitData("Golem", "🗿", 8, 1200, 50, 15f, 35f, 2.0f, 30f, "super-tank", UnitColor.GOLEM));
        unitRegistry.put("bomber", new UnitData("Bomber", "💣", 3, 60, 35, 45f, 100f, 1.2f, 11f, "splash", UnitColor.BOMBER));
        unitRegistry.put("musketeer", new UnitData("Musket", "🤠", 4, 100, 30, 40f, 220f, 1.1f, 14f, "sniper", UnitColor.MUSKETEER));
        unitRegistry.put("valkyrie", new UnitData("Valkyrie", "🪓", 4, 180, 25, 50f, 40f, 1.0f, 16f, "area-melee", UnitColor.VALKYRIE));
    }

    public UnitData getUnit(String key) {
        return unitRegistry.get(key);
    }

    public Map<String, UnitData> getAllUnits() {
        return new HashMap<>(unitRegistry);
    }
}

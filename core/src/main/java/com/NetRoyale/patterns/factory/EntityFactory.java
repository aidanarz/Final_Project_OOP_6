package com.NetRoyale.patterns.factory;

import com.NetRoyale.models.Entity;
import com.NetRoyale.models.RangedEntity;
import com.NetRoyale.models.TowerEntity;
import com.NetRoyale.models.UnitData;
import com.NetRoyale.models.UnitColor;
import com.NetRoyale.models.Team;
import com.NetRoyale.patterns.pool.ProjectilePool;

// Factory untuk buat entity
public class EntityFactory {
    
    public static Entity createEntity(String key, UnitData data, Team team, float x, float y, ProjectilePool projectilePool) {
        Entity entity;
        
        // Check if tower FIRST (before checking ranged)
        if (key.equals("king") || key.equals("tower") || key.equals("princess")) {
            boolean isKing = key.equals("king");
            entity = new TowerEntity(key, data, team, x, y, isKing, projectilePool);
        }
        // Check if ranged unit (archer, wizard, musketeer, etc)
        else if (data.getType().equals("ranged") || data.getType().equals("sniper") || data.getType().equals("splash")) {
            boolean isSplash = data.getType().equals("splash");
            entity = new RangedEntity(key, data, team, x, y, projectilePool, isSplash);
        } 
        // Melee unit
        else {
            entity = new Entity(key, data, team, x, y);
        }
        
        return entity;
    }
    
    // Buat melee entity
    public static Entity createMeleeEntity(String key, UnitData data, Team team, float x, float y) {
        return new Entity(key, data, team, x, y);
    }
    
    // Buat data tower
    public static UnitData createTowerData(boolean isKing, Team team) {
        if (isKing) {
            return new UnitData(
                "King Tower", "🏰", 0, 1200, 25, 0f, 200f, 1.0f, 40f, "building",
                team == Team.PLAYER ? UnitColor.PLAYER_TOWER : UnitColor.ENEMY_TOWER
            );
        } else {
            return new UnitData(
                "Princess Tower", "🗼", 0, 700, 18, 0f, 170f, 0.8f, 25f, "building",
                team == Team.PLAYER ? UnitColor.PLAYER_TOWER : UnitColor.ENEMY_TOWER
            );
        }
    }
}

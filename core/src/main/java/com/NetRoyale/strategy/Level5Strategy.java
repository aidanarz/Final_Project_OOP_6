package com.NetRoyale.strategy;

import com.NetRoyale.models.UnitData;

// Level 5: Master (2.5x)
public class Level5Strategy implements LevelStrategy {
    
    @Override
    public int getLevelNumber() {
        return 5;
    }
    
    @Override
    public String getLevelName() {
        return "Level 5: MASTER";
    }
    
    @Override
    public float getDamageMultiplier() {
        return 2.5f;
    }
    
    @Override
    public UnitData applyDifficulty(UnitData originalData) {
        return new UnitData(
            originalData.getName(),
            originalData.getIcon(),
            originalData.getCost(),
            originalData.getHp(),
            (int)(originalData.getDamage() * getDamageMultiplier()),
            originalData.getSpeed(),
            originalData.getRange(),
            originalData.getCooldown(),
            originalData.getSize(),
            originalData.getType(),
            originalData.getColor()
        );
    }
    
    @Override
    public String getLevelColor() {
        return "🟣";
    }
}

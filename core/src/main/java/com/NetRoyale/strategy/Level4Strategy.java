package com.NetRoyale.strategy;

import com.NetRoyale.models.UnitData;

// Level 4: Expert (2.0x)
public class Level4Strategy implements LevelStrategy {
    
    @Override
    public int getLevelNumber() {
        return 4;
    }
    
    @Override
    public String getLevelName() {
        return "Level 4: Expert";
    }
    
    @Override
    public float getDamageMultiplier() {
        return 2.0f;
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
        return "🔴";
    }
}

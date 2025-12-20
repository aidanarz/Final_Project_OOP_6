package com.NetRoyale.patterns.strategy;

import com.NetRoyale.models.UnitData;

// Level 2: Easy (1.3x)
public class Level2Strategy implements LevelStrategy {
    
    @Override
    public int getLevelNumber() {
        return 2;
    }
    
    @Override
    public String getLevelName() {
        return "Level 2: Easy";
    }
    
    @Override
    public float getDamageMultiplier() {
        return 1.3f;
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
        return "🔵";
    }
}

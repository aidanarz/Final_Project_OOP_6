package com.NetRoyale.patterns.strategy;

import com.NetRoyale.models.UnitData;

// Level 3: Hard (1.6x)
public class Level3Strategy implements LevelStrategy {
    
    @Override
    public int getLevelNumber() {
        return 3;
    }
    
    @Override
    public String getLevelName() {
        return "Level 3: Hard";
    }
    
    @Override
    public float getDamageMultiplier() {
        return 1.6f;
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
        return "🟠";
    }
}

package com.NetRoyale.patterns.strategy;

import com.NetRoyale.models.UnitData;

// Level 1: Beginner (1.0x)
public class Level1Strategy implements LevelStrategy {
    
    @Override
    public int getLevelNumber() {
        return 1;
    }
    
    @Override
    public String getLevelName() {
        return "Level 1: Beginner";
    }
    
    @Override
    public float getDamageMultiplier() {
        return 1.0f;
    }
    
    @Override
    public UnitData applyDifficulty(UnitData originalData) {
        return originalData;
    }
    
    @Override
    public String getLevelColor() {
        return "🟢";
    }
}

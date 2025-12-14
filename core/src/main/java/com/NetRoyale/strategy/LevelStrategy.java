package com.NetRoyale.strategy;

import com.NetRoyale.models.UnitData;

// Strategy interface untuk level difficulty
public interface LevelStrategy {
    int getLevelNumber();
    String getLevelName();
    float getDamageMultiplier();
    UnitData applyDifficulty(UnitData originalData);
    String getLevelColor();
}

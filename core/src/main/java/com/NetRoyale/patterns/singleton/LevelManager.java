package com.NetRoyale.patterns.singleton;

import com.NetRoyale.patterns.strategy.*;

// Singleton untuk manage level
public class LevelManager {
    private static LevelManager instance;

    private int currentLevel;
    private int maxLevelReached;
    private LevelStrategy currentStrategy;
    private static final int MAX_LEVEL = 5;

    private LevelManager() {
        this.currentLevel = 1;
        this.maxLevelReached = 1;
        this.currentStrategy = new Level1Strategy();
    }

    public static LevelManager getInstance() {
        if (instance == null) {
            instance = new LevelManager();
        }
        return instance;
    }

    public LevelStrategy getCurrentStrategy() {
        return currentStrategy;
    }

    public void setCurrentLevel(int level) {
        if (level < 1 || level > MAX_LEVEL) return;

        this.currentLevel = level;
        this.currentStrategy = getLevelStrategy(level);
    }

    // Level selesai
    public void onLevelComplete() {
        if (currentLevel < MAX_LEVEL) {
            currentLevel++;
            if (currentLevel > maxLevelReached) {
                maxLevelReached = currentLevel;
            }
            currentStrategy = getLevelStrategy(currentLevel);
        }
    }

    public void onLevelFailed() {
        // Stay on same level, player can retry
    }

    public boolean hasCompletedAllLevels() {
        return maxLevelReached >= MAX_LEVEL;
    }

    public boolean isMaxLevel() {
        return currentLevel >= MAX_LEVEL;
    }

    public void resetProgress() {
        this.currentLevel = 1;
        this.maxLevelReached = 1;
        this.currentStrategy = new Level1Strategy();
    }

    private LevelStrategy getLevelStrategy(int level) {
        switch (level) {
            case 1: return new Level1Strategy();
            case 2: return new Level2Strategy();
            case 3: return new Level3Strategy();
            case 4: return new Level4Strategy();
            case 5: return new Level5Strategy();
            default: return new Level1Strategy();
        }
    }

    public int getCurrentLevel() { return currentLevel; }
    public int getMaxLevelReached() { return maxLevelReached; }
    public int getMaxLevel() { return MAX_LEVEL; }

    public String getLevelInfo() {
        return currentStrategy.getLevelColor() + " " + currentStrategy.getLevelName();
    }

    public String getDifficultyInfo() {
        float mult = currentStrategy.getDamageMultiplier();
        return "Enemy Damage: " + (int)(mult * 100) + "%";
    }
}

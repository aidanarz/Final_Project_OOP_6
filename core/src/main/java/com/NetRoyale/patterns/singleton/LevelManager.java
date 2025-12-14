package com.NetRoyale.patterns.singleton;

import com.NetRoyale.patterns.strategy.*;

/**
 *  * Singleton Pattern - LevelManager
 *  *
 *  * Purpose: Manage level progression dan current level
 *  * - Track current level (1-5)
 * - Handle level completion
 * - Provide current level strategy
 * - Persist level progress
 */
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

    /**
     * Get strategy for current level
     */
    public LevelStrategy getCurrentStrategy() {
        return currentStrategy;
    }

    /**
     * Set current level and update strategy
     */
    public void setCurrentLevel(int level) {
        if (level < 1 || level > MAX_LEVEL) return;

        this.currentLevel = level;
        this.currentStrategy = getLevelStrategy(level);
    }

    /**
     * Handle level victory - advance to next level
     */
    public void onLevelComplete() {
        if (currentLevel < MAX_LEVEL) {
            currentLevel++;
            if (currentLevel > maxLevelReached) {
                maxLevelReached = currentLevel;
            }
            currentStrategy = getLevelStrategy(currentLevel);
        }
    }

    /**
     * Handle level defeat - retry same level
     */
    public void onLevelFailed() {
        // Stay on same level, player can retry
    }

    /**
     * Check if player beat all levels
     */
    public boolean hasCompletedAllLevels() {
        return maxLevelReached >= MAX_LEVEL;
    }

    /**
     * Check if current level is the last level
     */
    public boolean isMaxLevel() {
        return currentLevel >= MAX_LEVEL;
    }

    /**
     * Reset progress to level 1
     */
    public void resetProgress() {
        this.currentLevel = 1;
        this.maxLevelReached = 1;
        this.currentStrategy = new Level1Strategy();
    }

    /**
     * Get level strategy by level number
     */
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

    // Getters
    public int getCurrentLevel() { return currentLevel; }
    public int getMaxLevelReached() { return maxLevelReached; }
    public int getMaxLevel() { return MAX_LEVEL; }

    /**
     * Get level info string for display
     */
    public String getLevelInfo() {
        return currentStrategy.getLevelColor() + " " + currentStrategy.getLevelName();
    }

    /**
     * Get difficulty description
     */
    public String getDifficultyInfo() {
        float mult = currentStrategy.getDamageMultiplier();
        return "Enemy Damage: " + (int)(mult * 100) + "%";
    }
}

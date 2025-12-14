package com.NetRoyale.patterns.singleton;

/**
 * Singleton Pattern - GameManager
 * Manages game state: Elixir, Timer, and game status
 *
 * Purpose: Memastikan hanya ada satu instance yang mengatur state game
 * - Mengelola Elixir untuk player dan enemy
 * - Mengelola timer game (2 menit)
 * - Mengelola status running/paused
 */
public class GameManager {
    private static GameManager instance;

    // Game state
    private float playerElixir;
    private float enemyElixir;
    private int gameTime;
    private boolean running;
    private boolean paused;
    private float elixirTimer;

    // Constants
    private static final float MAX_ELIXIR = 10f;
    private static final float ELIXIR_REGEN_RATE = 0.5f; // per second
    private static final float ENEMY_ELIXIR_REGEN_RATE = 0.45f; // per second
    private static final int INITIAL_TIME = 120; // 2 minutes

    private GameManager() {
        reset();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void reset() {
        playerElixir = 5f;
        enemyElixir = 5f;
        gameTime = INITIAL_TIME;
        running = false;
        paused = false;
        elixirTimer = 0;
    }

    public void startGame() {
        running = true;
        paused = false;
    }

    public void stopGame() {
        running = false;
    }

    public void pauseGame() {
        paused = true;
    }

    public void resumeGame() {
        paused = false;
    }

    // Elixir management
    public void regenerateElixir(float delta) {
        if (!running || paused) return;

        elixirTimer += delta;
        if (elixirTimer >= 1.0f) {
            // Regenerate elixir per second
            if (playerElixir < MAX_ELIXIR) {
                playerElixir = Math.min(MAX_ELIXIR, playerElixir + ELIXIR_REGEN_RATE);
            }
            if (enemyElixir < MAX_ELIXIR) {
                enemyElixir = Math.min(MAX_ELIXIR, enemyElixir + ENEMY_ELIXIR_REGEN_RATE);
            }
            elixirTimer -= 1.0f;
        }
    }

    public boolean canAfford(float cost, boolean isPlayer) {
        return isPlayer ? playerElixir >= cost : enemyElixir >= cost;
    }

    public void spendElixir(float cost, boolean isPlayer) {
        if (isPlayer) {
            playerElixir = Math.max(0, playerElixir - cost);
        } else {
            enemyElixir = Math.max(0, enemyElixir - cost);
        }
    }

    public void refundElixir(float cost, boolean isPlayer) {
        if (isPlayer) {
            playerElixir = Math.min(MAX_ELIXIR, playerElixir + cost);
        } else {
            enemyElixir = Math.min(MAX_ELIXIR, enemyElixir + cost);
        }
    }

    // Timer management
    public void decrementTime() {
        if (!running || paused) return;

        gameTime--;
        if (gameTime <= 0) {
            gameTime = 0;
            stopGame();
        }
    }

    // Getters
    public float getPlayerElixir() { return playerElixir; }
    public float getEnemyElixir() { return enemyElixir; }
    public int getGameTime() { return gameTime; }
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
    public float getMaxElixir() { return MAX_ELIXIR; }
}

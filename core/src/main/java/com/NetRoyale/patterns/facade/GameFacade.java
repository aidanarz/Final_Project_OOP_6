

package com.NetRoyale.patterns.facade;

import com.NetRoyale.models.Entity;
import com.NetRoyale.models.UnitData;
import com.NetRoyale.models.Team;
import com.NetRoyale.models.Projectile;
import com.NetRoyale.patterns.singleton.GameManager;
import com.NetRoyale.patterns.pool.ProjectilePool;
import com.NetRoyale.patterns.pool.EntityPool;
import com.NetRoyale.patterns.command.CommandHistory;
import com.NetRoyale.patterns.command.SpawnCommand;
import com.NetRoyale.patterns.singleton.LevelManager;
import com.NetRoyale.patterns.strategy.LevelStrategy;
import com.NetRoyale.patterns.factory.UnitFactory;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

/**
 * Facade Pattern - GameFacade
 * 
 * Purpose: Menyederhanakan akses ke subsystem (GameManager, ProjectilePool, EntityPool, CommandHistory)
 * - Interface tunggal untuk game logic
 * - Hide complex interactions antar subsystem
 * - Integrasi Object Pool Pattern untuk Projectiles dan Entities
 */
public class GameFacade {
    private GameManager gameManager;
    private Array<Entity> playerEntities;
    private Array<Entity> enemyEntities;
    private ProjectilePool projectilePool;
    private EntityPool entityPool;
    private CommandHistory commandHistory;
    private float clockTimer;
    
    // Enemy AI spawning
    private LevelManager levelManager;
    private float aiSpawnTimer;
    private Random random;
    private static final float GAME_WIDTH = 850f;
    private static final float GAME_HEIGHT = 480f;
    
    public GameFacade() {
        this.gameManager = GameManager.getInstance();
        this.playerEntities = new Array<>();
        this.enemyEntities = new Array<>();
        this.projectilePool = new ProjectilePool();
        this.entityPool = new EntityPool(projectilePool);
        this.commandHistory = new CommandHistory();
        this.clockTimer = 0;
        
        // Initialize AI spawning
        this.levelManager = LevelManager.getInstance();
        this.aiSpawnTimer = 0;
        this.random = new Random();
    }
    
    public void initializeGame(float gameWidth, float gameHeight) {
        gameManager.reset();
        playerEntities.clear();
        enemyEntities.clear();
        projectilePool.clear();
        entityPool.reset();
        commandHistory.clear();
        clockTimer = 0;
        aiSpawnTimer = 0;
        
        // Spawn towers
        spawnInitialTowers(gameWidth, gameHeight);
        
        gameManager.startGame();
    }
    
    private void spawnInitialTowers(float gameWidth, float gameHeight) {
        float cy = gameHeight / 2;
        
        // Player towers
        spawnTower("king", 60, cy, true, Team.PLAYER);
        spawnTower("princess", 150, 100, false, Team.PLAYER);
        spawnTower("princess", 150, gameHeight - 100, false, Team.PLAYER);
        
        // Enemy towers
        spawnTower("king", gameWidth - 60, cy, true, Team.ENEMY);
        spawnTower("princess", gameWidth - 150, 100, false, Team.ENEMY);
        spawnTower("princess", gameWidth - 150, gameHeight - 100, false, Team.ENEMY);
    }
    
    private void spawnTower(String type, float x, float y, boolean isKing, Team team) {
        UnitData towerData = com.NetRoyale.patterns.factory.EntityFactory.createTowerData(isKing, team);
        Entity tower = com.NetRoyale.patterns.factory.EntityFactory.createEntity(type, towerData, team, x, y, projectilePool);
        
        if (team == Team.PLAYER) {
            playerEntities.add(tower);
        } else {
            enemyEntities.add(tower);
        }
    }
    
    public void update(float delta) {
        if (!gameManager.isRunning() || gameManager.isPaused()) return;
        
        // Update clock timer
        clockTimer += delta;
        if (clockTimer >= 1.0f) {
            gameManager.decrementTime();
            clockTimer -= 1.0f;
        }
        
        // Update elixir (pass delta for per-second regen)
        gameManager.regenerateElixir(delta);
        
        // Update AI spawning timer (enemy spawning system with LevelStrategy)
        updateAISpawning(delta);
        
        // Update all entities (movement, attacking, state transitions)
        updateEntities(delta);
        
        // Update projectiles
        updateProjectiles(delta);
        
        // Cleanup inactive projectiles (Object Pool Pattern)
        projectilePool.cleanupInactive();
        
        // Cleanup dead entities (Object Pool Pattern)
        entityPool.cleanupDeadEntities();
        cleanupDeadEntities();
    }
    
    private void updateAISpawning(float delta) {
        // AI spawning interval varies by level (harder = faster spawning)
        float spawnInterval = getAISpawnInterval();
        
        aiSpawnTimer += delta;
        if (aiSpawnTimer >= spawnInterval) {
            aiSpawnTimer = 0;
            spawnEnemyUnit();
        }
    }
    
    private float getAISpawnInterval() {
        // Spawn interval decreases with level (level 1 = slow, level 5 = fast)
        int level = levelManager.getCurrentLevel();
        switch (level) {
            case 1: return 8.0f;  // Level 1: Every 8 seconds (beginner)
            case 2: return 6.5f;  // Level 2: Every 6.5 seconds
            case 3: return 5.0f;  // Level 3: Every 5 seconds
            case 4: return 3.5f;  // Level 4: Every 3.5 seconds
            case 5: return 2.5f;  // Level 5: Every 2.5 seconds (master)
            default: return 8.0f;
        }
    }
    
    private void spawnEnemyUnit() {
        float enemyElixir = gameManager.getEnemyElixir();
        if (enemyElixir < 3) return;
        
        // Get random unit from UnitFactory
        UnitFactory factory = UnitFactory.getInstance();
        String[] allKeys = factory.getAllUnits().keySet().toArray(new String[0]);
        String pick = allKeys[random.nextInt(allKeys.length)];
        UnitData unitData = factory.getUnit(pick);
        
        // Apply level difficulty to enemy units (Strategy Pattern)
        LevelStrategy levelStrategy = levelManager.getCurrentStrategy();
        unitData = levelStrategy.applyDifficulty(unitData);
        
        if (enemyElixir >= unitData.getCost()) {
            // Spawn on enemy side (right side of map)
            float[] lanes = {100f, GAME_HEIGHT / 2, GAME_HEIGHT - 100f};
            float y = lanes[random.nextInt(3)] + (random.nextFloat() * 40 - 20);
            float x = GAME_WIDTH - 120 - (random.nextFloat() * 50);
            
            // Spawn enemy unit using SpawnCommand (Command Pattern)
            SpawnCommand cmd = new SpawnCommand(pick, unitData, x, y, Team.ENEMY, enemyEntities, entityPool);
            cmd.execute();  // Direct execute tanpa commandHistory karena ini AI, bukan player action
            
            // Note: Elixir sudah dikurangi di SpawnCommand.execute()
        }
    }
    
    private void updateEntities(float delta) {
        // Convert Array to List for Entity.update()
        java.util.List<Entity> enemyList = new java.util.ArrayList<>();
        for (Entity e : enemyEntities) enemyList.add(e);
        
        java.util.List<Entity> playerList = new java.util.ArrayList<>();
        for (Entity e : playerEntities) playerList.add(e);
        
        // Update player entities (they attack enemy entities)
        for (Entity e : playerEntities) {
            if (!e.isDead()) {
                e.update(delta, enemyList);
            }
        }
        
        // Update enemy entities (they attack player entities)
        for (Entity e : enemyEntities) {
            if (!e.isDead()) {
                e.update(delta, playerList);
            }
        }
    }
    
    private void updateProjectiles(float delta) {
        Array<Projectile> projectiles = projectilePool.getActiveProjectiles();
        for (Projectile p : projectiles) {
            if (p.isActive()) {
                p.update(delta);
            }
        }
    }
    
    private void cleanupDeadEntities() {
        for (int i = playerEntities.size - 1; i >= 0; i--) {
            if (playerEntities.get(i).isDead()) {
                playerEntities.removeIndex(i);
            }
        }
        for (int i = enemyEntities.size - 1; i >= 0; i--) {
            if (enemyEntities.get(i).isDead()) {
                enemyEntities.removeIndex(i);
            }
        }
    }
    
    public void spawnUnit(String unitKey, UnitData unitData, float x, float y, Team team) {
        Array<Entity> targetList = (team == Team.PLAYER) ? playerEntities : enemyEntities;
        SpawnCommand cmd = new SpawnCommand(unitKey, unitData, x, y, team, targetList, entityPool);
        commandHistory.executeCommand(cmd);
    }
    
    public void undoLastSpawn() {
        commandHistory.undo();
    }
    
    public void redoSpawn() {
        commandHistory.redo();
    }
    
    public void replayAllSpawns() {
        commandHistory.replay();
    }
    
    public boolean checkWinCondition() {
        boolean playerTowerAlive = false;
        boolean enemyTowerAlive = false;
        
        for (Entity e : playerEntities) {
            if (e.isKing() && !e.isDead()) {
                playerTowerAlive = true;
                break;
            }
        }
        
        for (Entity e : enemyEntities) {
            if (e.isKing() && !e.isDead()) {
                enemyTowerAlive = true;
                break;
            }
        }
        
        // Return true if game over
        if (!playerTowerAlive || !enemyTowerAlive || gameManager.getGameTime() <= 0) {
            gameManager.stopGame();
            return true;
        }
        
        return false;
    }
    
    public String getWinnerMessage() {
        boolean playerTowerAlive = false;
        boolean enemyTowerAlive = false;
        int playerTowerHP = 0;
        int enemyTowerHP = 0;
        
        for (Entity e : playerEntities) {
            if (e.isKing() && !e.isDead()) {
                playerTowerAlive = true;
                playerTowerHP = (int) e.getHp();
                break;
            }
        }
        
        for (Entity e : enemyEntities) {
            if (e.isKing() && !e.isDead()) {
                enemyTowerAlive = true;
                enemyTowerHP = (int) e.getHp();
                break;
            }
        }
        
        if (!playerTowerAlive) return "DEFEAT! Enemy destroyed your tower!";
        if (!enemyTowerAlive) return "VICTORY! You destroyed enemy tower!";
        if (gameManager.getGameTime() <= 0) {
            if (playerTowerHP > enemyTowerHP) return "VICTORY! Your tower has more HP!";
            if (enemyTowerHP > playerTowerHP) return "DEFEAT! Enemy tower has more HP!";
            return "DRAW! Both towers have equal HP!";
        }
        
        return "";
    }
    
    // Getters for subsystems
    public GameManager getGameManager() { return gameManager; }
    public Array<Entity> getPlayerEntities() { return playerEntities; }
    public Array<Entity> getEnemyEntities() { return enemyEntities; }
    public ProjectilePool getProjectilePool() { return projectilePool; }
    public EntityPool getEntityPool() { return entityPool; }
    public CommandHistory getCommandHistory() { return commandHistory; }
    
    // Helper getters for rendering
    public Array<Entity> getAllEntities() {
        Array<Entity> all = new Array<>();
        all.addAll(playerEntities);
        all.addAll(enemyEntities);
        return all;
    }
    
    public Array<Projectile> getProjectiles() {
        return projectilePool.getActiveProjectiles();
    }
}

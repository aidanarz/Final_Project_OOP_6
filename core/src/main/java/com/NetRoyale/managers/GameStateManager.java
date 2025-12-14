package com.NetRoyale.managers;

import com.NetRoyale.models.*;
import java.util.ArrayList;
import java.util.List;

// Singleton untuk manage game state
public class GameStateManager {
    private static GameStateManager instance;
    
    private List<Entity> entities;
    private List<Projectile> projectiles;
    private List<Particle> particles;
    private List<String> playerDeck;
    
    private float playerElixir;
    private float enemyElixir;
    private int gameTime;
    private boolean running;
    private String selectedCard;

    private GameStateManager() {
        entities = new ArrayList<>();
        projectiles = new ArrayList<>();
        particles = new ArrayList<>();
        playerDeck = new ArrayList<>();
        playerElixir = 5f;
        enemyElixir = 5f;
        gameTime = 120;
        running = false;
    }

    public static GameStateManager getInstance() {
        if (instance == null) {
            instance = new GameStateManager();
        }
        return instance;
    }

    public void reset() {
        entities.clear();
        projectiles.clear();
        particles.clear();
        playerElixir = 5f;
        enemyElixir = 5f;
        gameTime = 120;
        running = false;
        selectedCard = null;
    }

    public void initializeTowers() {
        float cy = 480 / 2f;
        // Player towers
        entities.add(new Tower("king", Team.PLAYER, 60, cy, true));
        entities.add(new Tower("princess", Team.PLAYER, 150, 100, false));
        entities.add(new Tower("princess", Team.PLAYER, 150, 380, false));
        
        // Enemy towers
        entities.add(new Tower("king", Team.ENEMY, 790, cy, true));
        entities.add(new Tower("princess", Team.ENEMY, 700, 100, false));
        entities.add(new Tower("princess", Team.ENEMY, 700, 380, false));
    }

    public void update(float delta) {
        if (!running) return;

        // Filter teams
        List<Entity> players = new ArrayList<>();
        List<Entity> enemies = new ArrayList<>();
        
        for (Entity e : entities) {
            if (e.getTeam() == Team.PLAYER) players.add(e);
            else enemies.add(e);
        }

        // Update entities
        for (Entity e : entities) {
            List<Entity> targets = (e.getTeam() == Team.PLAYER) ? enemies : players;
            e.update(delta, targets);
        }

        // Update projectiles
        projectiles.removeIf(p -> !p.isActive());
        for (Projectile p : projectiles) {
            p.update(delta);
        }

        // Update particles
        particles.removeIf(p -> !p.isAlive());
        for (Particle p : particles) {
            p.update(delta);
        }

        // Remove dead entities
        entities.removeIf(Entity::isDead);

        // Check win conditions
        checkWinConditions(players, enemies);
    }

    private void checkWinConditions(List<Entity> players, List<Entity> enemies) {
        boolean playerKingAlive = false;
        boolean enemyKingAlive = false;

        for (Entity e : players) {
            if (e.isKing()) playerKingAlive = true;
        }
        for (Entity e : enemies) {
            if (e.isKing()) enemyKingAlive = true;
        }

        if (!playerKingAlive) {
            endGame(false);
        } else if (!enemyKingAlive) {
            endGame(true);
        }
    }

    private void endGame(boolean victory) {
        running = false;
        // Will be handled by screen
    }

    public void increaseElixir(float delta) {
        if (playerElixir < 10) {
            playerElixir = Math.min(10, playerElixir + delta);
        }
        if (enemyElixir < 10) {
            enemyElixir = Math.min(10, enemyElixir + delta * 0.9f);
        }
    }

    public void decreasePlayerElixir(float amount) {
        playerElixir -= amount;
    }

    public void decreaseEnemyElixir(float amount) {
        enemyElixir -= amount;
    }

    public void decreaseTime() {
        gameTime--;
        if (gameTime <= 0) {
            running = false;
        }
    }

    // Add entities
    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    public void addParticles(float x, float y, int count, com.badlogic.gdx.graphics.Color color) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, color));
        }
    }

    public List<Entity> getEntities() { return entities; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<Particle> getParticles() { return particles; }
    public List<String> getPlayerDeck() { return playerDeck; }
    public void setPlayerDeck(List<String> deck) { this.playerDeck = new ArrayList<>(deck); }
    public float getPlayerElixir() { return playerElixir; }
    public float getEnemyElixir() { return enemyElixir; }
    public int getGameTime() { return gameTime; }
    public boolean isRunning() { return running; }
    public void setRunning(boolean running) { this.running = running; }
    public String getSelectedCard() { return selectedCard; }
    public void setSelectedCard(String card) { this.selectedCard = card; }
}

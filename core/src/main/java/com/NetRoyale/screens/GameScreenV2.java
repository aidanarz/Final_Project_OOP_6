package com.NetRoyale.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.NetRoyale.NetRoyale;
import com.NetRoyale.managers.GameStateManager;
import com.NetRoyale.managers.RenderManager;
import com.NetRoyale.models.*;
import com.NetRoyale.patterns.factory.UnitFactory;
import com.NetRoyale.patterns.pool.ProjectilePool;

import java.util.ArrayList;
import java.util.List;

/**
 * Game Screen V2 - Full HTML feature match
 */
public class GameScreenV2 implements Screen {
    private static final float GAME_WIDTH = 850f;
    private static final float GAME_HEIGHT = 480f;
    private static final float SPAWN_ZONE_X = GAME_WIDTH * 0.4f;
    
    private NetRoyale game;
    private GameStateManager gameState;
    private RenderManager renderManager;
    
    private List<Entity> entities;
    private List<Projectile> projectiles;
    private List<Particle> particles;
    private List<String> playerDeck;
    private List<CardUI> deckCards;
    private ProjectilePool projectilePool;
    
    private float playerElixir;
    private float enemyElixir;
    private int gameTime;
    private boolean running;
    
    private float elixirTimer;
    private float aiTimer;
    private float clockTimer;
    
    private CardUI selectedCard;
    private CardUI draggedCard;
    private Vector2 dragStart;

    public GameScreenV2(NetRoyale game) {
        this.game = game;
        this.gameState = GameStateManager.getInstance();
        this.renderManager = RenderManager.getInstance();
        
        this.entities = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.particles = new ArrayList<>();
        this.playerDeck = new ArrayList<>(gameState.getPlayerDeck());
        this.deckCards = new ArrayList<>();
        this.dragStart = new Vector2();
        this.projectilePool = new ProjectilePool();
        
        initGame();
    }

    private void initGame() {
        playerElixir = 5f;
        enemyElixir = 5f;
        gameTime = 120; // 2 minutes
        running = true;
        
        elixirTimer = 0;
        aiTimer = 0;
        clockTimer = 0;
        
        // Create towers
        float cy = GAME_HEIGHT / 2;
        
        // Player side
        entities.add(createTower(Team.PLAYER, 60, cy, true));
        entities.add(createTower(Team.PLAYER, 150, 100, false));
        entities.add(createTower(Team.PLAYER, 150, GAME_HEIGHT - 100, false));
        
        // Enemy side
        entities.add(createTower(Team.ENEMY, GAME_WIDTH - 60, cy, true));
        entities.add(createTower(Team.ENEMY, GAME_WIDTH - 150, 100, false));
        entities.add(createTower(Team.ENEMY, GAME_WIDTH - 150, GAME_HEIGHT - 100, false));
        
        // Create deck UI cards
        float cardY = 20;
        float startX = (GAME_WIDTH - (4 * CardUI.CARD_WIDTH + 3 * 15)) / 2;
        for (int i = 0; i < playerDeck.size(); i++) {
            String key = playerDeck.get(i);
            UnitData unit = UnitFactory.getInstance().getUnit(key);
            float x = startX + i * (CardUI.CARD_WIDTH + 15);
            deckCards.add(new CardUI(key, unit, x, cardY));
        }
    }

    private Entity createTower(Team team, float x, float y, boolean isKing) {
        String type = isKing ? "king" : "princess";
        int hp = isKing ? 1200 : 700;
        int dmg = isKing ? 25 : 18;
        float range = isKing ? 200 : 170;
        float cd = isKing ? 1.0f : 0.8f;
        float size = isKing ? 40 : 25;
        
        UnitData towerData = new UnitData(
            isKing ? "King Tower" : "Princess Tower",
            isKing ? "🏰" : "🗼",
            0, hp, dmg, 0, range, cd, size, "building",
            team == Team.PLAYER ? UnitColor.PLAYER_TOWER : UnitColor.ENEMY_TOWER
        );
        
        return new TowerEntity(type, towerData, team, x, y, isKing, projectilePool);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        if (!running) return;
        
        // Update timers
        elixirTimer += delta;
        if (elixirTimer >= 1.0f) {
            if (playerElixir < 10) playerElixir += 0.5f;
            if (enemyElixir < 10) enemyElixir += 0.45f; // Slightly slower for enemy
            elixirTimer = 0;
            
            // Update card availability
            updateCardAvailability();
        }
        
        aiTimer += delta;
        if (aiTimer >= 2.2f) {
            executeAI();
            aiTimer = 0;
        }
        
        clockTimer += delta;
        if (clockTimer >= 1.0f) {
            gameTime--;
            clockTimer = 0;
            if (gameTime <= 0) {
                endGame();
            }
        }
        
        // Update entities
        List<Entity> playerEntities = new ArrayList<>();
        List<Entity> enemyEntities = new ArrayList<>();
        
        for (Entity e : entities) {
            if (e.getTeam() == Team.PLAYER) playerEntities.add(e);
            else enemyEntities.add(e);
        }
        
        for (Entity e : entities) {
            List<Entity> targets = (e.getTeam() == Team.PLAYER) ? enemyEntities : playerEntities;
            e.update(delta, targets);
        }
        
        // Update projectiles
        for (Projectile p : new ArrayList<>(projectiles)) {
            p.update(delta);
            if (!p.isActive()) projectiles.remove(p);
        }
        
        // Update particles
        for (Particle p : new ArrayList<>(particles)) {
            p.update(delta);
            if (!p.isAlive()) particles.remove(p);
        }
        
        // Remove dead entities
        for (Entity e : new ArrayList<>(entities)) {
            if (e.isDead()) {
                createDeathParticles(e);
                entities.remove(e);
            }
        }
        
        // Check win condition
        checkWinCondition();
        
        // Handle input
        handleInput();
    }

    private void updateCardAvailability() {
        for (CardUI card : deckCards) {
            card.setDisabled(playerElixir < card.getUnit().getCost());
        }
    }

    private void executeAI() {
        if (enemyElixir < 3) return;
        
        // Bot picks random unit from ALL 10 units
        String[] allUnits = {"knight", "archer", "giant", "goblin", "skeleton", 
                             "wizard", "golem", "bomber", "musketeer", "valkyrie"};
        String pick = allUnits[(int)(Math.random() * allUnits.length)];
        UnitData unit = UnitFactory.getInstance().getUnit(pick);
        
        if (enemyElixir >= unit.getCost()) {
            // Spawn in one of 3 lanes
            float[] lanes = {100, GAME_HEIGHT / 2, GAME_HEIGHT - 100};
            float y = lanes[(int)(Math.random() * 3)] + (float)(Math.random() * 40 - 20);
            float x = GAME_WIDTH - 120 - (float)(Math.random() * 50);
            
            spawnUnit(pick, x, y, Team.ENEMY);
            enemyElixir -= unit.getCost();
        }
    }

    private void checkWinCondition() {
        boolean playerKingAlive = false;
        boolean enemyKingAlive = false;
        
        for (Entity e : entities) {
            if (e.getKey().equals("king")) {
                if (e.getTeam() == Team.PLAYER) playerKingAlive = true;
                else enemyKingAlive = true;
            }
        }
        
        if (!playerKingAlive) {
            gameOver(false, "Your King has fallen!");
        } else if (!enemyKingAlive) {
            gameOver(true, "Enemy King Destroyed!");
        }
    }

    private void endGame() {
        running = false;
        gameOver(false, "Time limit reached");
    }

    private void gameOver(boolean victory, String message) {
        running = false;
        game.setScreen(new GameOverScreenV2(game, victory, message));
    }

    private void handleInput() {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        
        if (Gdx.input.justTouched()) {
            // Check card clicks
            for (CardUI card : deckCards) {
                if (card.contains(mouseX, mouseY) && !card.isDisabled()) {
                    startCardDrag(card, mouseX, mouseY);
                    return;
                }
            }
        }
        
        // Update drag
        if (draggedCard != null && Gdx.input.isTouched()) {
            draggedCard.updateDrag(mouseX, mouseY);
        }
        
        // Release drag
        if (draggedCard != null && !Gdx.input.isTouched()) {
            handleCardDrop(mouseX, mouseY);
        }
    }

    private void startCardDrag(CardUI card, float x, float y) {
        UnitData unit = card.getUnit();
        
        // Create temporary drag card
        draggedCard = new CardUI(card.getKey(), unit, x, y);
        draggedCard.startDrag(x, y);
        dragStart.set(card.getBounds().x, card.getBounds().y);
        selectedCard = card;
    }

    private void handleCardDrop(float x, float y) {
        // Check if in spawn zone
        if (x < SPAWN_ZONE_X && y > CardUI.CARD_HEIGHT + 30 && y < GAME_HEIGHT - 30) {
            // Valid spawn location
            spawnUnit(draggedCard.getKey(), x, y, Team.PLAYER);
            playerElixir -= draggedCard.getUnit().getCost();
            updateCardAvailability();
        }
        
        draggedCard = null;
        selectedCard = null;
    }

    private void spawnUnit(String key, float x, float y, Team team) {
        UnitData unit = UnitFactory.getInstance().getUnit(key);
        
        // Determine entity type based on unit type
        Entity entity;
        
        if (unit.getType().equals("ranged") || unit.getType().equals("sniper") || 
            unit.getType().equals("splash")) {
            entity = new RangedEntity(key, unit, team, x, y, projectilePool, 
                                     unit.getType().equals("splash"));
        } else {
            entity = new Entity(key, unit, team, x, y);
        }
        
        entities.add(entity);
    }

    private void createDeathParticles(Entity e) {
        Vector2 pos = e.getPosition();
        Color color = (e.getTeam() == Team.PLAYER) ?
            new Color(0.29f, 0.537f, 0.863f, 1) : new Color(0.855f, 0.267f, 0.325f, 1);
        
        for (int i = 0; i < 8; i++) {
            particles.add(new Particle(pos.x, pos.y, color));
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.549f, 0.757f, 0.322f, 1); // Green ground
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderManager.getCamera().update();
        SpriteBatch batch = renderManager.getBatch();
        ShapeRenderer sr = renderManager.getShapeRenderer();
        BitmapFont font = renderManager.getFont();
        
        batch.setProjectionMatrix(renderManager.getCamera().combined);
        sr.setProjectionMatrix(renderManager.getCamera().combined);
        
        // Draw background elements
        drawBackground(sr);
        
        // Draw spawn zone indicator
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(1, 1, 1, 0.2f);
        Gdx.gl.glLineWidth(2);
        for (int i = 0; i < 10; i++) {
            sr.line(SPAWN_ZONE_X, i * 20, SPAWN_ZONE_X, i * 20 + 10);
        }
        Gdx.gl.glLineWidth(1);
        sr.end();
        
        // Draw entities
        drawEntities(sr, batch, font);
        
        // Draw projectiles
        drawProjectiles(sr);
        
        // Draw particles
        drawParticles(sr);
        
        // Draw UI
        drawUI(sr, batch, font);
        
        // Draw dragged card last
        if (draggedCard != null) {
            draggedCard.render(sr, batch, font);
        }
    }

    private void drawBackground(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        
        // River
        sr.setColor(0.365f, 0.612f, 0.925f, 1); // Blue
        sr.rect(GAME_WIDTH / 2 - 30, 0, 60, GAME_HEIGHT);
        
        // Bridges
        sr.setColor(0.843f, 0.8f, 0.784f, 1); // Brownish
        sr.rect(GAME_WIDTH / 2 - 35, 80, 70, 40);
        sr.rect(GAME_WIDTH / 2 - 35, GAME_HEIGHT - 120, 70, 40);
        sr.rect(GAME_WIDTH / 2 - 35, GAME_HEIGHT / 2 - 20, 70, 40);
        
        sr.end();
    }

    private void drawEntities(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        // Sort by Y for depth
        entities.sort((a, b) -> Float.compare(a.getPosition().y, b.getPosition().y));
        
        for (Entity e : entities) {
            Vector2 pos = e.getPosition();
            float size = e.getData().getSize();
            boolean isPlayer = e.getTeam() == Team.PLAYER;
            
            // Shadow
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0, 0, 0, 0.3f);
            sr.ellipse(pos.x - size, pos.y + size / 2 - size * 0.3f, size * 2, size * 0.6f);
            sr.end();
            
            // Draw unit shape based on type
            drawUnitShape(sr, e, pos, size, isPlayer);
            
            // HP bar
            sr.begin(ShapeRenderer.ShapeType.Filled);
            float hpPct = e.getHp() / e.getMaxHp();
            float barWidth = 24;
            sr.setColor(0.067f, 0.067f, 0.067f, 1);
            sr.rect(pos.x - barWidth / 2, pos.y - size - 10, barWidth, 4);
            sr.setColor(isPlayer ? Color.GREEN : Color.RED);
            sr.rect(pos.x - barWidth / 2, pos.y - size - 10, barWidth * hpPct, 4);
            sr.end();
        }
    }

    private void drawUnitShape(ShapeRenderer sr, Entity e, Vector2 pos, float size, boolean isPlayer) {
        String type = e.getKey();
        Color teamColor = isPlayer ? 
            new Color(0.29f, 0.537f, 0.863f, 1) : new Color(0.855f, 0.267f, 0.325f, 1);
        
        sr.begin(ShapeRenderer.ShapeType.Filled);
        
        if (type.equals("king") || type.equals("princess")) {
            // Tower - square with roof
            sr.setColor(0.443f, 0.502f, 0.588f, 1); // Gray
            sr.rect(pos.x - size, pos.y - size, size * 2, size * 2);
            sr.setColor(teamColor);
            sr.rect(pos.x - size + 4, pos.y + size, size * 2 - 8, 15);
        } else if (type.equals("giant") || type.equals("golem")) {
            sr.setColor(type.equals("golem") ? 
                new Color(0.627f, 0.682f, 0.753f, 1) : teamColor);
            sr.rect(pos.x - size / 2, pos.y - size, size, size * 1.5f);
        } else {
            // Default circle
            sr.setColor(teamColor);
            sr.circle(pos.x, pos.y, size);
        }
        
        sr.end();
    }

    private void drawProjectiles(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (Projectile p : projectiles) {
            sr.setColor(p.isSplash() ? Color.BLACK : Color.GOLD);
            sr.circle(p.getPosition().x, p.getPosition().y, p.isSplash() ? 5 : 3);
        }
        sr.end();
    }

    private void drawParticles(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (Particle p : particles) {
            Color c = p.getColor();
            sr.setColor(c.r, c.g, c.b, p.getLife() * 2);
            sr.rect(p.getPosition().x, p.getPosition().y, 4, 4);
        }
        sr.end();
    }

    private void drawUI(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        // Top HUD
        batch.begin();
        font.getData().setScale(1.2f);
        
        // Player label
        font.setColor(0.29f, 0.537f, 0.863f, 1);
        font.draw(batch, "PLAYER", 20, GAME_HEIGHT - 10);
        
        // Timer
        int minutes = gameTime / 60;
        int seconds = gameTime % 60;
        String timeStr = String.format("%02d:%02d", minutes, seconds);
        font.setColor(Color.WHITE);
        font.draw(batch, timeStr, GAME_WIDTH / 2 - 30, GAME_HEIGHT - 10);
        
        // Enemy label
        font.setColor(0.855f, 0.267f, 0.325f, 1);
        font.draw(batch, "ENEMY", GAME_WIDTH - 100, GAME_HEIGHT - 10);
        
        font.getData().setScale(1f);
        batch.end();
        
        // Elixir bar
        float barWidth = GAME_WIDTH * 0.5f;
        float barHeight = 14;
        float barX = (GAME_WIDTH - barWidth) / 2;
        float barY = 130;
        
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.067f, 0.067f, 0.067f, 1);
        sr.rect(barX, barY, barWidth, barHeight);
        
        // Elixir fill
        float fillPct = playerElixir / 10f;
        sr.setColor(0.843f, 0.439f, 0.678f, 1); // Purple/pink
        sr.rect(barX, barY, barWidth * fillPct, barHeight);
        sr.end();
        
        // Elixir text
        batch.begin();
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
        String elixirStr = String.format("%.0f", playerElixir);
        font.draw(batch, elixirStr, GAME_WIDTH / 2 - 10, barY + 30);
        batch.end();
        
        // Draw deck cards
        for (CardUI card : deckCards) {
            if (draggedCard == null || !card.getKey().equals(draggedCard.getKey())) {
                card.render(sr, batch, font);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        renderManager.getCamera().viewportWidth = GAME_WIDTH;
        renderManager.getCamera().viewportHeight = GAME_HEIGHT;
        renderManager.getCamera().position.set(GAME_WIDTH / 2, GAME_HEIGHT / 2, 0);
        renderManager.getCamera().update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}

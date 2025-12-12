package com.NetRoyale.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.NetRoyale.NetRoyale;
import com.NetRoyale.managers.GameStateManager;
import com.NetRoyale.managers.RenderManager;
import com.NetRoyale.models.*;
import com.NetRoyale.patterns.factory.UnitFactory;
import com.NetRoyale.patterns.RandomAIStrategy;

import java.util.List;

/**
 * Main Game Screen - Handles gameplay
 */
public class GameScreen implements Screen {
    private static final float GAME_WIDTH = 850f;
    private static final float GAME_HEIGHT = 480f;
    
    private NetRoyale game;
    private GameStateManager gameState;
    private RenderManager renderManager;
    
    private float elixirTimer;
    private float aiTimer;
    private float gameTimer;

    public GameScreen(NetRoyale game) {
        this.game = game;
        this.gameState = GameStateManager.getInstance();
        this.renderManager = RenderManager.getInstance();
        
        initGame();
    }

    private void initGame() {
        gameState.reset();
        gameState.initializeTowers();
        gameState.setRunning(true);
        gameState.setAiStrategy(new RandomAIStrategy());
        
        elixirTimer = 0;
        aiTimer = 0;
        gameTimer = 0;
    }

    @Override
    public void show() {
        // Setup input
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        if (!gameState.isRunning()) {
            // Check for game over
            checkGameOver();
            return;
        }

        // Update game state
        gameState.update(delta);

        // Elixir regeneration
        elixirTimer += delta;
        if (elixirTimer >= 1.0f) {
            gameState.increaseElixir(0.5f);
            elixirTimer = 0;
        }

        // AI logic
        aiTimer += delta;
        if (aiTimer >= 2.2f) {
            List<Entity> players = gameState.getEntities().stream()
                .filter(e -> e.getTeam() == Team.PLAYER)
                .collect(java.util.stream.Collectors.toList());
            List<Entity> enemies = gameState.getEntities().stream()
                .filter(e -> e.getTeam() == Team.ENEMY)
                .collect(java.util.stream.Collectors.toList());
                
            gameState.getAiStrategy().execute(gameState.getEnemyElixir(), players, enemies);
            aiTimer = 0;
        }

        // Game timer
        gameTimer += delta;
        if (gameTimer >= 1.0f) {
            gameState.decreaseTime();
            gameTimer = 0;
        }

        // Input handling
        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            renderManager.getCamera().unproject(touchPos);
            
            float x = touchPos.x;
            float y = touchPos.y;
            
            // Check if in spawn zone (left 40%)
            if (x < GAME_WIDTH * 0.4f && gameState.getSelectedCard() != null) {
                spawnUnit(gameState.getSelectedCard(), x, y);
                gameState.setSelectedCard(null);
            }
        }

        // Card selection (1-4 keys)
        List<String> deck = gameState.getPlayerDeck();
        if (deck.size() > 0 && Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) selectCard(0);
        if (deck.size() > 1 && Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) selectCard(1);
        if (deck.size() > 2 && Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) selectCard(2);
        if (deck.size() > 3 && Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) selectCard(3);
    }

    private void selectCard(int index) {
        List<String> deck = gameState.getPlayerDeck();
        if (index >= deck.size()) return;
        
        String key = deck.get(index);
        UnitData unit = UnitFactory.getInstance().getUnit(key);
        
        if (gameState.getPlayerElixir() >= unit.getCost()) {
            gameState.setSelectedCard(key);
        }
    }

    private void spawnUnit(String key, float x, float y) {
        UnitData unitData = UnitFactory.getInstance().getUnit(key);
        
        if (gameState.getPlayerElixir() >= unitData.getCost()) {
            Entity entity = new Entity(key, unitData, Team.PLAYER, x, y);
            gameState.addEntity(entity);
            gameState.decreasePlayerElixir(unitData.getCost());
        }
    }

    private void checkGameOver() {
        boolean playerKingAlive = false;
        boolean enemyKingAlive = false;

        for (Entity e : gameState.getEntities()) {
            if (e.getTeam() == Team.PLAYER && e.isKing()) playerKingAlive = true;
            if (e.getTeam() == Team.ENEMY && e.isKing()) enemyKingAlive = true;
        }

        if (!playerKingAlive) {
            game.setScreen(new GameOverScreen(game, false));
        } else if (!enemyKingAlive) {
            game.setScreen(new GameOverScreen(game, true));
        } else if (gameState.getGameTime() <= 0) {
            game.setScreen(new GameOverScreen(game, false)); // Draw treated as loss
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.549f, 0.757f, 0.322f, 1); // Green ground
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderManager.getCamera().update();
        
        // Draw game elements
        drawBackground();
        drawEntities();
        drawProjectiles();
        drawParticles();
        drawUI();
    }

    private void drawBackground() {
        ShapeRenderer sr = renderManager.getShapeRenderer();
        sr.setProjectionMatrix(renderManager.getCamera().combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        
        // River
        sr.setColor(0.365f, 0.612f, 0.925f, 1);
        sr.rect(GAME_WIDTH / 2 - 30, 0, 60, GAME_HEIGHT);
        
        // Bridges
        sr.setColor(0.843f, 0.8f, 0.784f, 1);
        sr.rect(GAME_WIDTH / 2 - 35, 80, 70, 40);
        sr.rect(GAME_WIDTH / 2 - 35, GAME_HEIGHT - 120, 70, 40);
        sr.rect(GAME_WIDTH / 2 - 35, GAME_HEIGHT / 2 - 20, 70, 40);
        
        sr.end();
    }

    private void drawEntities() {
        ShapeRenderer sr = renderManager.getShapeRenderer();
        SpriteBatch batch = renderManager.getBatch();
        BitmapFont font = renderManager.getFont();
        
        sr.setProjectionMatrix(renderManager.getCamera().combined);
        batch.setProjectionMatrix(renderManager.getCamera().combined);
        
        for (Entity e : gameState.getEntities()) {
            Vector2 pos = e.getPosition();
            float size = e.getData().getSize();
            
            // Draw shadow
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0, 0, 0, 0.3f);
            sr.ellipse(pos.x - size, pos.y + size / 2 - size * 0.3f, size * 2, size * 0.6f);
            sr.end();
            
            // Draw entity body
            sr.begin(ShapeRenderer.ShapeType.Filled);
            Color color = (e.getTeam() == Team.PLAYER) ? 
                new Color(0.29f, 0.537f, 0.863f, 1) : new Color(0.855f, 0.267f, 0.325f, 1);
            sr.setColor(color);
            sr.circle(pos.x, pos.y, size);
            sr.end();
            
            // Draw HP bar
            sr.begin(ShapeRenderer.ShapeType.Filled);
            float hpPct = e.getHp() / e.getMaxHp();
            float barWidth = 24;
            sr.setColor(0.067f, 0.067f, 0.067f, 1);
            sr.rect(pos.x - barWidth / 2, pos.y - size - 10, barWidth, 4);
            sr.setColor((e.getTeam() == Team.PLAYER) ? Color.GREEN : Color.RED);
            sr.rect(pos.x - barWidth / 2, pos.y - size - 10, barWidth * hpPct, 4);
            sr.end();
            
            // Draw icon
            batch.begin();
            font.setColor(Color.WHITE);
            font.draw(batch, e.getData().getIcon(), pos.x - 8, pos.y + 8);
            batch.end();
        }
    }

    private void drawProjectiles() {
        ShapeRenderer sr = renderManager.getShapeRenderer();
        sr.setProjectionMatrix(renderManager.getCamera().combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        
        for (Projectile p : gameState.getProjectiles()) {
            sr.setColor(p.isSplash() ? Color.BLACK : Color.GOLD);
            sr.circle(p.getPosition().x, p.getPosition().y, p.isSplash() ? 5 : 3);
        }
        
        sr.end();
    }

    private void drawParticles() {
        ShapeRenderer sr = renderManager.getShapeRenderer();
        sr.setProjectionMatrix(renderManager.getCamera().combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        
        for (Particle p : gameState.getParticles()) {
            Color c = p.getColor();
            sr.setColor(c.r, c.g, c.b, p.getLife() * 2);
            sr.rect(p.getPosition().x, p.getPosition().y, 4, 4);
        }
        
        sr.end();
    }

    private void drawUI() {
        SpriteBatch batch = renderManager.getBatch();
        BitmapFont font = renderManager.getFont();
        
        batch.setProjectionMatrix(renderManager.getCamera().combined);
        batch.begin();
        
        // Timer
        int minutes = gameState.getGameTime() / 60;
        int seconds = gameState.getGameTime() % 60;
        String timeStr = String.format("%02d:%02d", minutes, seconds);
        font.setColor(Color.WHITE);
        font.draw(batch, timeStr, GAME_WIDTH / 2 - 20, GAME_HEIGHT - 10);
        
        // Elixir
        String elixirStr = String.format("Elixir: %.0f/10", gameState.getPlayerElixir());
        font.draw(batch, elixirStr, 10, GAME_HEIGHT - 10);
        
        // Deck cards
        List<String> deck = gameState.getPlayerDeck();
        for (int i = 0; i < deck.size(); i++) {
            UnitData unit = UnitFactory.getInstance().getUnit(deck.get(i));
            float x = 300 + (i * 90);
            float y = 30;
            
            boolean canAfford = gameState.getPlayerElixir() >= unit.getCost();
            font.setColor(canAfford ? Color.WHITE : Color.GRAY);
            font.draw(batch, (i + 1) + ". " + unit.getIcon() + " " + unit.getCost(), x, y);
        }
        
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        renderManager.getCamera().viewportWidth = GAME_WIDTH;
        renderManager.getCamera().viewportHeight = GAME_HEIGHT;
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

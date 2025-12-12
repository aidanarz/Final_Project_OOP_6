package com.NetRoyale.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.NetRoyale.NetRoyale;
import com.NetRoyale.managers.GameStateManager;
import com.NetRoyale.managers.RenderManager;
import com.NetRoyale.managers.TowerAnimationManager;
import com.NetRoyale.managers.UnitAnimationManager;
import com.NetRoyale.models.*;
import com.NetRoyale.patterns.singleton.GameManager;
import com.NetRoyale.patterns.singleton.LevelManager;
import com.NetRoyale.patterns.facade.GameFacade;
import com.NetRoyale.patterns.command.CommandHistory;
import com.NetRoyale.patterns.factory.UnitFactory;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

/**
 * Game Screen V3 - Using all Design Patterns
 * - Singleton: GameManager, GameFacade, LevelManager
 * - State: WalkingState, AttackingState
 * - Object Pool: EntityPool, ProjectilePool
 * - Factory: EntityFactory
 * - Command: SpawnCommand, CommandHistory
 * - Facade: GameFacade
 * - Strategy: LevelStrategy for difficulty
 */
public class GameScreenV3 implements Screen {
    private static final float GAME_WIDTH = 850f;
    private static final float GAME_HEIGHT = 480f;
    private static final float SPAWN_ZONE_X = GAME_WIDTH * 0.4f;
    
    private NetRoyale game;
    private RenderManager renderManager;
    private TowerAnimationManager towerAnimationManager;
    private UnitAnimationManager unitAnimationManager;
    
    // Facade Pattern - Single point of access
    private GameFacade gameFacade;
    private GameManager gameManager;
    private CommandHistory commandHistory;
    private LevelManager levelManager;
    
    private List<CardUI> deckCards;
    private CardUI draggedCard;
    private Vector2 dragStart;

    public GameScreenV3(NetRoyale game) {
        this.game = game;
        this.renderManager = RenderManager.getInstance();
        this.towerAnimationManager = TowerAnimationManager.getInstance();
        this.unitAnimationManager = UnitAnimationManager.getInstance();
        this.dragStart = new Vector2();
        
        // Initialize using Facade
        gameFacade = new GameFacade();
        gameManager = gameFacade.getGameManager();
        commandHistory = gameFacade.getCommandHistory();
        levelManager = LevelManager.getInstance();
        
        initGame();
    }

    private void initGame() {
        // Get player deck from GameStateManager
        List<String> playerDeck = GameStateManager.getInstance().getPlayerDeck();
        
        // Initialize game through Facade (spawns towers automatically)
        gameFacade.initializeGame(GAME_WIDTH, GAME_HEIGHT);
        
        // Create deck UI cards
        deckCards = new ArrayList<>();
        float cardY = 20;
        float startX = (GAME_WIDTH - (4 * CardUI.CARD_WIDTH + 3 * 15)) / 2;
        for (int i = 0; i < playerDeck.size(); i++) {
            String key = playerDeck.get(i);
            UnitData unit = UnitFactory.getInstance().getUnit(key);
            float x = startX + i * (CardUI.CARD_WIDTH + 15);
            deckCards.add(new CardUI(key, unit, x, cardY));
        }
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        // Check game over
        if (gameFacade.checkWinCondition()) {
            String message = gameFacade.getWinnerMessage();
            gameOver(message.startsWith("VICTORY"), message);
            return;
        }
        
        // Update game through Facade
        gameFacade.update(delta);
        
        // Update card availability
        updateCardAvailability();
        
        // Handle input
        handleInput();
    }

    private void updateCardAvailability() {
        for (CardUI card : deckCards) {
            float cost = card.getUnit().getCost();
            card.setDisabled(!gameManager.canAfford(cost, true));
        }
    }

    private void handleInput() {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        
        // Check for UNDO command (Z key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z) && commandHistory.canUndo()) {
            commandHistory.undo();
        }
        
        // Check for REDO command (Y key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y) && commandHistory.canRedo()) {
            commandHistory.redo();
        }
        
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
        draggedCard = new CardUI(card.getKey(), unit, x, y);
        draggedCard.startDrag(x, y);
        dragStart.set(card.getBounds().x, card.getBounds().y);
    }

    private void handleCardDrop(float x, float y) {
        // Check if in spawn zone
        if (x < SPAWN_ZONE_X && y > CardUI.CARD_HEIGHT + 30 && y < GAME_HEIGHT - 30) {
            // Spawn using Command Pattern through Facade
            UnitData unitData = UnitFactory.getInstance().getUnit(draggedCard.getKey());
            gameFacade.spawnUnit(draggedCard.getKey(), unitData, x, y, Team.PLAYER);
        }
        
        draggedCard = null;
    }

    private void gameOver(boolean victory, String message) {
        game.setScreen(new GameOverScreenV2(game, victory, message));
    }

    private void draw() {
        Gdx.gl.glClearColor(0.549f, 0.757f, 0.322f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderManager.getCamera().update();
        SpriteBatch batch = renderManager.getBatch();
        ShapeRenderer sr = renderManager.getShapeRenderer();
        BitmapFont font = renderManager.getFont();
        
        batch.setProjectionMatrix(renderManager.getCamera().combined);
        sr.setProjectionMatrix(renderManager.getCamera().combined);
        
        drawBackground(sr);
        drawSpawnZone(sr);
        drawEntities(sr, batch, font);
        drawProjectiles(sr);
        drawParticles(sr);
        drawUI(sr, batch, font);
        
        if (draggedCard != null) {
            draggedCard.render(sr, batch, font);
        }
    }

    private void drawBackground(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.365f, 0.612f, 0.925f, 1);
        sr.rect(GAME_WIDTH / 2 - 30, 0, 60, GAME_HEIGHT);
        sr.setColor(0.843f, 0.8f, 0.784f, 1);
        sr.rect(GAME_WIDTH / 2 - 35, 80, 70, 40);
        sr.rect(GAME_WIDTH / 2 - 35, GAME_HEIGHT - 120, 70, 40);
        sr.rect(GAME_WIDTH / 2 - 35, GAME_HEIGHT / 2 - 20, 70, 40);
        sr.end();
    }

    private void drawSpawnZone(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(1, 1, 1, 0.2f);
        Gdx.gl.glLineWidth(2);
        for (int i = 0; i < 24; i++) {
            sr.line(SPAWN_ZONE_X, i * 20, SPAWN_ZONE_X, i * 20 + 10);
        }
        Gdx.gl.glLineWidth(1);
        sr.end();
    }

    private void drawEntities(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        Array<Entity> entities = gameFacade.getAllEntities();
        entities.sort((a, b) -> Float.compare(a.getPosition().y, b.getPosition().y));
        
        for (Entity e : entities) {
            Vector2 pos = e.getPosition();
            float size = e.getData().getSize();
            boolean isPlayer = e.getTeam() == Team.PLAYER;
            boolean isTower = e instanceof TowerEntity;
            
            // Shadow
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0, 0, 0, 0.3f);
            sr.ellipse(pos.x - size, pos.y + size / 2 - size * 0.3f, size * 2, size * 0.6f);
            sr.end();
            
            // Draw with sprite animation
            if (isTower) {
                drawTowerWithAnimation(batch, (TowerEntity) e, pos, size);
            } else {
                drawUnitWithAnimation(batch, e, pos, size, isPlayer);
            }
            
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
    
    private void drawTowerWithAnimation(SpriteBatch batch, TowerEntity tower, Vector2 pos, float size) {
        TextureRegion currentFrame = towerAnimationManager.getCurrentFrame(tower.getStateTime());
        
        if (currentFrame != null) {
            batch.begin();
            
            // Tower size menyesuaikan shape tower (kotak size*2 x size*2 + bendera 15px)
            // Total height tower shape = size*2 + 15, kita buat sprite sedikit lebih besar
            float spriteWidth = size * 2.2f;   // Sedikit lebih lebar dari shape
            float spriteHeight = size * 2.5f;  // Tinggi proporsional
            
            // POSISI TETAP - anchor di tengah bawah
            float drawX = pos.x - spriteWidth / 2;
            float drawY = pos.y - size;
            
            // Simple draw - static, tidak animasi
            batch.draw(currentFrame, 
                      drawX,           // X position
                      drawY,           // Y position
                      spriteWidth,     // Width
                      spriteHeight);   // Height
            
            batch.end();
        }
    }

    private void drawUnitWithAnimation(SpriteBatch batch, Entity entity, Vector2 pos, float size, boolean isPlayer) {
        // Get animation frame based on current state
        TextureRegion currentFrame = unitAnimationManager.getCurrentFrame(
            entity.getKey(), 
            entity.getAnimationState(), 
            entity.getStateTime()
        );
        
        if (currentFrame != null) {
            batch.begin();
            
            // Ukuran sprite MENYESUAIKAN SHAPE:
            // - Circle shape: diameter = size * 2
            // - Rectangle shape (giant/golem): width = size, height = size * 1.5
            String unitKey = entity.getKey();
            float spriteWidth, spriteHeight;
            
            if (unitKey.equals("giant") || unitKey.equals("golem")) {
                // Shape rectangle: size x size*1.5
                spriteWidth = size * 2.2f;   // Sedikit lebih lebar untuk visual
                spriteHeight = size * 3f;    // 1.5x tinggi + extra untuk kepala
            } else {
                // Shape circle: diameter size*2
                spriteWidth = size * 2.4f;   // Diameter + sedikit extra
                spriteHeight = size * 2.4f;  // Square untuk sprite 64x64
            }
            
            // POSISI TETAP - anchor di tengah bawah seperti run.png
            float drawX = pos.x - spriteWidth / 2;
            float drawY = pos.y - size;  // Bottom di ground
            
            // Flip sprite based on team
            boolean flipX = (entity.getTeam() == Team.ENEMY);
            
            // Draw simple - POSISI TETAP, frame berganti untuk animasi walk
            if (flipX) {
                // Flip horizontal
                batch.draw(currentFrame, 
                          drawX + spriteWidth,  // X shift untuk flip
                          drawY,                // Y tetap
                          -spriteWidth,         // Width negatif = flip
                          spriteHeight);        // Height normal
            } else {
                // Normal draw
                batch.draw(currentFrame, 
                          drawX,                // X position tetap
                          drawY,                // Y position tetap
                          spriteWidth,          // Width
                          spriteHeight);        // Height
            }
            
            batch.end();
        } else {
            // Fallback to shape if animation not found
            drawUnitShapeFallback(entity, pos, size, isPlayer);
        }
    }
    
    private void drawUnitShapeFallback(Entity e, Vector2 pos, float size, boolean isPlayer) {
        ShapeRenderer sr = renderManager.getShapeRenderer();
        String type = e.getKey();
        Color teamColor = isPlayer ? 
            new Color(0.29f, 0.537f, 0.863f, 1) : new Color(0.855f, 0.267f, 0.325f, 1);
        
        sr.begin(ShapeRenderer.ShapeType.Filled);
        
        if (type.equals("king") || type.equals("princess")) {
            sr.setColor(0.443f, 0.502f, 0.588f, 1);
            sr.rect(pos.x - size, pos.y - size, size * 2, size * 2);
            sr.setColor(teamColor);
            sr.rect(pos.x - size + 4, pos.y + size, size * 2 - 8, 15);
        } else if (type.equals("giant") || type.equals("golem")) {
            sr.setColor(type.equals("golem") ? 
                new Color(0.627f, 0.682f, 0.753f, 1) : teamColor);
            sr.rect(pos.x - size / 2, pos.y - size, size, size * 1.5f);
        } else {
            sr.setColor(teamColor);
            sr.circle(pos.x, pos.y, size);
        }
        
        sr.end();
    }

    private void drawProjectiles(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (Projectile p : gameFacade.getProjectiles()) {
            sr.setColor(p.isSplash() ? Color.BLACK : Color.GOLD);
            sr.circle(p.getPosition().x, p.getPosition().y, p.isSplash() ? 5 : 3);
        }
        sr.end();
    }

    private void drawParticles(ShapeRenderer sr) {
        // Particle system not yet implemented in Facade
        sr.begin(ShapeRenderer.ShapeType.Filled);
        // TODO: Add particle pool to facade if needed
        sr.end();
    }

    private void drawUI(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        // Top HUD
        batch.begin();
        font.getData().setScale(1.2f);
        
        font.setColor(0.29f, 0.537f, 0.863f, 1);
        font.draw(batch, "PLAYER", 20, GAME_HEIGHT - 10);
        
        // Timer in center
        font.getData().setScale(1.5f);
        int minutes = gameManager.getGameTime() / 60;
        int seconds = gameManager.getGameTime() % 60;
        String timeStr = String.format("%02d:%02d", minutes, seconds);
        font.setColor(Color.WHITE);
        font.draw(batch, timeStr, GAME_WIDTH / 2 - 35, GAME_HEIGHT - 15);
        
        // Level info top-left
        font.getData().setScale(1f);
        font.setColor(1f, 1f, 1f, 1);
        String levelInfo = levelManager.getLevelInfo();
        font.draw(batch, levelInfo, 20, GAME_HEIGHT - 35);
        
        font.getData().setScale(0.7f);
        font.setColor(0.957f, 0.263f, 0.212f, 1);
        String diffInfo = "Dmg: " + (int)(levelManager.getCurrentStrategy().getDamageMultiplier() * 100) + "%";
        font.draw(batch, diffInfo, 20, GAME_HEIGHT - 55);
        
        font.getData().setScale(1.2f);
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
        
        float fillPct = gameManager.getPlayerElixir() / gameManager.getMaxElixir();
        sr.setColor(0.843f, 0.439f, 0.678f, 1);
        sr.rect(barX, barY, barWidth * fillPct, barHeight);
        sr.end();
        
        batch.begin();
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
        String elixirStr = String.format("%.0f", gameManager.getPlayerElixir());
        font.draw(batch, elixirStr, GAME_WIDTH / 2 - 10, barY + 30);
        
        // Undo/Redo hints
        font.getData().setScale(0.6f);
        font.setColor(0.7f, 0.7f, 0.7f, 1);
        font.draw(batch, "Z: Undo | Y: Redo", 10, 20);
        font.draw(batch, "History: " + commandHistory.getHistorySize(), 10, 40);
        
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
    public void pause() { gameManager.pauseGame(); }

    @Override
    public void resume() { gameManager.resumeGame(); }

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}

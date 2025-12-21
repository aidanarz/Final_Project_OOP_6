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
import com.NetRoyale.managers.TileManager;
import com.NetRoyale.managers.TowerAnimationManager;
import com.NetRoyale.managers.UnitAnimationManager;
import com.NetRoyale.models.*;
import com.NetRoyale.patterns.singleton.GameManager;
import com.NetRoyale.patterns.singleton.LevelManager;
import com.NetRoyale.patterns.facade.GameFacade;
import com.NetRoyale.patterns.command.CommandHistory;
import com.NetRoyale.patterns.factory.UnitFactory;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.List;

/**
 * Game Screen - Using all Design Patterns
 * - Singleton: GameManager, GameFacade, LevelManager
 * - State: WalkingState, AttackingState
 * - Object Pool: EntityPool, ProjectilePool
 * - Factory: EntityFactory
 * - Command: SpawnCommand, CommandHistory
 * - Facade: GameFacade
 * - Strategy: LevelStrategy for difficulty
 */
public class GameScreen implements Screen {
    private static final float GAME_WIDTH = 850f;
    private static final float GAME_HEIGHT = 480f;
    private static final float SPAWN_ZONE_X = GAME_WIDTH * 0.4f;
    
    private NetRoyale game;
    private RenderManager renderManager;
    private TileManager tileManager;
    private TowerAnimationManager towerAnimationManager;
    private UnitAnimationManager unitAnimationManager;
    private Texture bridgeTexture;
    
    // Facade Pattern - Single point of access
    private GameFacade gameFacade;
    private GameManager gameManager;
    private CommandHistory commandHistory;
    private LevelManager levelManager;
    
    private List<CardUI> deckCards;
    private CardUI draggedCard;
    private Vector2 dragStart;

    public GameScreen(NetRoyale game) {
        this.game = game;
        this.renderManager = RenderManager.getInstance();
        this.tileManager = TileManager.getInstance();
        this.towerAnimationManager = TowerAnimationManager.getInstance();
        this.unitAnimationManager = UnitAnimationManager.getInstance();
        this.bridgeTexture = new Texture(Gdx.files.internal("1 Tiles/bridge.png"));
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
        game.setScreen(new GameOverScreen(game, victory, message));
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
        drawHorizontalPaths(batch);  // Draw tile paths for horizontal roads
        drawBridges(batch);  // Draw bridge textures on top of tiles
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
        // Background sederhana - hanya river dan bridge
        sr.begin(ShapeRenderer.ShapeType.Filled);
        
        // River (biru)
        sr.setColor(0.365f, 0.612f, 0.925f, 1);
        sr.rect(GAME_WIDTH / 2 - 30, 0, 60, GAME_HEIGHT);
        
        sr.end();
    }
    
    private void drawBridges(SpriteBatch batch) {
        // Draw bridge textures instead of rectangles
        batch.begin();
        
        // Bridge bawah (lower bridge) - diperbesar
        batch.draw(bridgeTexture, GAME_WIDTH / 2 - 50, 70, 100, 60);
        
        // Bridge atas (upper bridge) - diperbesar
        batch.draw(bridgeTexture, GAME_WIDTH / 2 - 50, GAME_HEIGHT - 130, 100, 60);
        
        batch.end();
    }
    
    private void drawHorizontalPaths(SpriteBatch batch) {
        // Load FieldsTile_31 untuk archer-bridge path, FieldsTile_32 untuk king-archer path
        if (tileManager.getTileTexture("path1") != null && tileManager.getTileTexture("path2") != null) {
            batch.begin();
            com.badlogic.gdx.graphics.Texture tile31 = tileManager.getTileTexture("path1");
            com.badlogic.gdx.graphics.Texture tile32 = tileManager.getTileTexture("path2");
            
            // PLAYER SIDE PATHS (menggunakan tile32 untuk king-archer)
            // Path vertikal: Player king tower ke archer tower bawah
            for (float y = 100; y < 240; y += 32) {
                batch.draw(tile32, 60 - 15, y, 30, 32);
            }
            
            // Path horizontal: Player king tower area ke archer tower bawah
            for (float x = 60; x < 150; x += 32) {
                batch.draw(tile32, x, 85, 32, 30);
            }
            
            // Path vertikal: Player king tower ke archer tower atas
            for (float y = 240; y < 380; y += 32) {
                batch.draw(tile32, 60 - 15, y, 30, 32);
            }
            
            // Path horizontal: Player king tower area ke archer tower atas
            for (float x = 60; x < 150; x += 32) {
                batch.draw(tile32, x, 365, 32, 30);
            }
            
            // Path horizontal bawah: Player archer tower ke bridge (tile31)
            for (float x = 150; x < GAME_WIDTH / 2 - 35; x += 32) {
                batch.draw(tile31, x, 85, 32, 30);
            }
            
            // Path horizontal atas: Player archer tower ke bridge (tile31)
            for (float x = 150; x < GAME_WIDTH / 2 - 35; x += 32) {
                batch.draw(tile31, x, 365, 32, 30);
            }
            
            // ENEMY SIDE PATHS
            // Path horizontal bawah: Bridge ke Enemy archer tower (tile31)
            for (float x = GAME_WIDTH / 2 + 35; x < 700; x += 32) {
                batch.draw(tile31, x, 85, 32, 30);
            }
            
            // Path horizontal atas: Bridge ke Enemy archer tower (tile31)
            for (float x = GAME_WIDTH / 2 + 35; x < 700; x += 32) {
                batch.draw(tile31, x, 365, 32, 30);
            }
            
            // Path horizontal: Enemy archer tower bawah ke king tower area (tile32)
            for (float x = 700; x < 790; x += 32) {
                batch.draw(tile32, x, 85, 32, 30);
            }
            
            // Path horizontal: Enemy archer tower atas ke king tower area (tile32)
            for (float x = 700; x < 790; x += 32) {
                batch.draw(tile32, x, 365, 32, 30);
            }
            
            // Path vertikal: Enemy king tower ke archer tower bawah (tile32)
            for (float y = 100; y < 240; y += 32) {
                batch.draw(tile32, GAME_WIDTH - 60 - 15, y, 30, 32);
            }
            
            // Path vertikal: Enemy king tower ke archer tower atas (tile32)
            for (float y = 240; y < 380; y += 32) {
                batch.draw(tile32, GAME_WIDTH - 60 - 15, y, 30, 32);
            }
            
            batch.end();
        }
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
        
        // First pass: Draw shadows FIRST (behind everything)
        for (Entity e : entities) {
            Vector2 pos = e.getPosition();
            float size = e.getData().getSize();
            boolean isTower = e instanceof TowerEntity;
            
            // Shadow - different for tower vs units
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0, 0, 0, 0.3f);
            if (isTower) {
                // Tower shadow: rectangular at base
                sr.rect(pos.x - size * 0.8f, pos.y - size + 2, size * 1.6f, size * 0.4f);
            } else {
                // Unit shadow: elliptical
                sr.ellipse(pos.x - size, pos.y + size / 2 - size * 0.3f, size * 2, size * 0.6f);
            }
            sr.end();
        }
        
        // Second pass: Draw all sprites (on top of shadows)
        batch.begin();
        for (Entity e : entities) {
            Vector2 pos = e.getPosition();
            float size = e.getData().getSize();
            boolean isPlayer = e.getTeam() == Team.PLAYER;
            boolean isTower = e instanceof TowerEntity;
            
            // Draw sprite animation
            if (isTower) {
                drawTowerWithAnimation(batch, (TowerEntity) e, pos, size);
            } else {
                drawUnitWithAnimation(batch, e, pos, size, isPlayer);
            }
        }
        batch.end();
        
        // Third pass: Draw HP bars (on top of everything)
        for (Entity e : entities) {
            Vector2 pos = e.getPosition();
            float size = e.getData().getSize();
            boolean isPlayer = e.getTeam() == Team.PLAYER;
            
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
        // Tentukan tipe tower berdasarkan isKing
        String towerType = tower.isKing() ? "king" : "princess";
        TextureRegion currentFrame = towerAnimationManager.getCurrentFrame(towerType, tower.getStateTime());
        
        if (currentFrame != null) {
            // Tower sprite frame: 60x130 pixels (aspect ratio 1:2.17)
            // Semua tower menggunakan ukuran yang sama (archer tower size)
            
            // Perbesar 1.5x (1 + 1/2) untuk semua tower
            float baseWidth = size * 2.8f * 1.5f;
            float spriteWidth = baseWidth * 1.0f;
            
            // Maintain aspect ratio but limit height
            float aspectRatio = 130f / 60f; // 2.17
            float maxHeight = size * 3.2f * 1.5f;
            float spriteHeight = Math.min(spriteWidth * aspectRatio, maxHeight);
            
            // Anchor at bottom center of tower base
            float drawX = pos.x - spriteWidth / 2;
            float drawY = pos.y - size + 5; // Slightly above base
            
            // Draw tower sprite with proper proportions
            batch.draw(currentFrame, 
                      drawX,           // X position (center aligned)
                      drawY,           // Y position (bottom aligned)
                      spriteWidth,     // Width
                      spriteHeight);   // Height (proportional)
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
            // Ukuran sprite yang lebih besar dan jelas
            // Base pada ukuran collision shape
            String unitKey = entity.getKey();
            float spriteWidth, spriteHeight;
            
            // Ukuran dasar berdasarkan collision shape size (diperbesar)
            float baseSize = size * 2.8f; // Diperbesar dari 1.8f ke 2.8f
            
            if (unitKey.equals("giant") || unitKey.equals("golem")) {
                // Unit besar: lebih besar dan imposing
                spriteWidth = baseSize * 1.6f;   // Diperbesar dari 1.4f
                spriteHeight = baseSize * 1.8f;  // Diperbesar dari 1.6f
            } else if (unitKey.equals("goblin")) {
                // Goblin: tetap kecil tapi lebih jelas
                spriteWidth = baseSize * 1.0f;   // Diperbesar dari 0.8f
                spriteHeight = baseSize * 1.0f;  
            } else {
                // Unit normal: ukuran yang lebih besar dan jelas
                spriteWidth = baseSize * 1.2f;   // Diperbesar dari 1.0f     
                spriteHeight = baseSize * 1.2f;  
            }
            
            // Posisi anchor di bottom center, align dengan shadow
            float drawX = pos.x - spriteWidth / 2;
            float drawY = pos.y - size + 2; // Sedikit di atas ground untuk visual yang lebih baik
            
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
    public void dispose() {
        if (bridgeTexture != null) {
            bridgeTexture.dispose();
        }
    }
}

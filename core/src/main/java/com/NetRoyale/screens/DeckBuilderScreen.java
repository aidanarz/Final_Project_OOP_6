package com.NetRoyale.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.NetRoyale.NetRoyale;
import com.NetRoyale.patterns.factory.UnitFactory;
import com.NetRoyale.models.UnitData;
import com.NetRoyale.models.CardUI;
import com.NetRoyale.managers.GameStateManager;
import com.NetRoyale.managers.RenderManager;
import com.NetRoyale.patterns.singleton.LevelManager;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deck Builder Screen with Level Display
 * Shows current level and difficulty using Strategy Pattern
 */
public class DeckBuilderScreen implements Screen {
    private static final float GAME_WIDTH = 850f;
    private static final float GAME_HEIGHT = 480f;

    private NetRoyale game;
    private List<String> selectedDeck;
    private Map<String, CardUI> collectionCards;
    private List<Rectangle> deckSlots;
    private Rectangle startButton;

    private RenderManager renderManager;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private LevelManager levelManager;

    public DeckBuilderScreen(NetRoyale game) {
        this.game = game;
        this.selectedDeck = new ArrayList<>();
        this.collectionCards = new LinkedHashMap<>();
        this.deckSlots = new ArrayList<>();

        this.renderManager = RenderManager.getInstance();
        this.batch = renderManager.getBatch();
        this.shapeRenderer = renderManager.getShapeRenderer();
        this.font = renderManager.getFont();
        this.levelManager = LevelManager.getInstance();

        createUI();
    }

    private void createUI() {
        // Create collection cards (10 units in 2 rows of 5) - centered
        UnitFactory factory = UnitFactory.getInstance();
        Map<String, UnitData> allUnits = factory.getAllUnits();

        float cardWidth = CardUI.CARD_WIDTH;
        float cardHeight = CardUI.CARD_HEIGHT;
        float gapX = 20;
        float gapY = 25;

        // Calculate centered starting position for 5 columns
        float totalCollectionWidth = 5 * cardWidth + 4 * gapX;
        float startX = (GAME_WIDTH - totalCollectionWidth) / 2;
        float startY = 300; // Lower to avoid level info

        int col = 0;
        int row = 0;

        // Create cards in order: knight, archer, giant, goblin, skeleton, wizard, golem, bomber, musketeer, valkyrie
        String[] order = {"knight", "archer", "giant", "goblin", "skeleton",
                          "wizard", "golem", "bomber", "musketeer", "valkyrie"};

        for (String key : order) {
            UnitData unit = allUnits.get(key);
            if (unit != null) {
                float x = startX + col * (cardWidth + gapX);
                float y = startY - row * (cardHeight + gapY);
                collectionCards.put(key, new CardUI(key, unit, x, y));

                col++;
                if (col >= 5) {
                    col = 0;
                    row++;
                }
            }
        }

        // Create deck slots (4 slots at bottom) - centered
        float deckY = 60;
        float totalDeckWidth = 4 * cardWidth + 3 * gapX;
        float deckStartX = (GAME_WIDTH - totalDeckWidth) / 2;
        for (int i = 0; i < 4; i++) {
            float x = deckStartX + i * (cardWidth + gapX);
            deckSlots.add(new Rectangle(x, deckY, cardWidth, cardHeight));
        }

        // Start button - centered
        startButton = new Rectangle(GAME_WIDTH / 2 - 100, 10, 200, 40);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.176f, 0.208f, 0.282f, 1); // #2d3748
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();
        draw();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

            renderManager.getCamera().unproject(touchPos);

            float mouseX = touchPos.x;
            float mouseY = touchPos.y;

            if (startButton.contains(mouseX, mouseY) && selectedDeck.size() == 4) {
                startGame();
                return;
            }

            for (int i = 0; i < selectedDeck.size(); i++) {
                if (deckSlots.get(i).contains(mouseX, mouseY)) {
                    selectedDeck.remove(i);
                    return;
                }
            }

            for (Map.Entry<String, CardUI> entry : collectionCards.entrySet()) {
                if (entry.getValue().contains(mouseX, mouseY)) {
                    addToDeck(entry.getKey());
                    return;
                }
            }
        }
    }

    private void addToDeck(String key) {
        // Don't add if already in deck
        if (selectedDeck.contains(key)) return;

        // Don't add if deck is full
        if (selectedDeck.size() >= 4) return;

        // Add to deck
        selectedDeck.add(key);
    }

    private void draw() {
        batch.setProjectionMatrix(renderManager.getCamera().combined);
        shapeRenderer.setProjectionMatrix(renderManager.getCamera().combined);

        batch.begin();

        // Title - centered
        font.getData().setScale(2f);
        font.setColor(1, 0.843f, 0, 1); // Amber/Gold
        String title = "DECK BUILDING";
        float titleWidth = font.getData().getGlyph(title.charAt(0)).width * title.length() * 2f;
        font.draw(batch, title, (GAME_WIDTH - titleWidth / 2) / 2, GAME_HEIGHT - 20);

        font.getData().setScale(0.8f);
        font.setColor(0.7f, 0.7f, 0.7f, 1);
        String subtitle = "Pilih 4 Pasukan untuk dibawa ke arena";
        font.draw(batch, subtitle, GAME_WIDTH / 2 - 140, GAME_HEIGHT - 45);

        // Level Info Box - Top right corner
        font.getData().setScale(1.3f);
        font.setColor(1, 1, 1, 1);
        String levelInfo = levelManager.getLevelInfo();
        font.draw(batch, levelInfo, GAME_WIDTH - 200, GAME_HEIGHT - 15);

        font.getData().setScale(0.8f);
        font.setColor(0.957f, 0.263f, 0.212f, 1); // Red for difficulty
        String diffInfo = levelManager.getDifficultyInfo();
        font.draw(batch, diffInfo, GAME_WIDTH - 200, GAME_HEIGHT - 40);

        // Progress indicator
        font.getData().setScale(0.7f);
        font.setColor(1f, 0.843f, 0f, 1); // Gold
        String progress = "Progress: " + levelManager.getCurrentLevel() + "/" + levelManager.getMaxLevel();
        font.draw(batch, progress, GAME_WIDTH - 200, GAME_HEIGHT - 60);

        // Collection label - aligned with cards
        font.getData().setScale(1f);
        font.setColor(0.8f, 0.8f, 0.8f, 1);
        if (!collectionCards.isEmpty()) {
            CardUI firstCard = collectionCards.values().iterator().next();
            font.draw(batch, "Collection (10)", firstCard.getBounds().x, 335);
        }

        // Deck label - aligned with slots
        if (!deckSlots.isEmpty()) {
            font.draw(batch, "Battle Deck (" + selectedDeck.size() + "/4)", deckSlots.get(0).x, 185);
        }

        batch.end();

        // Draw collection cards
        for (Map.Entry<String, CardUI> entry : collectionCards.entrySet()) {
            CardUI card = entry.getValue();
            card.setSelected(selectedDeck.contains(entry.getKey()));
            card.setDisabled(selectedDeck.contains(entry.getKey()));
            card.render(shapeRenderer, batch, font);
        }

        // Draw deck slots area background - with border
        if (!deckSlots.isEmpty()) {
            float bgPadding = 15;
            float bgX = deckSlots.get(0).x - bgPadding;
            float bgY = deckSlots.get(0).y - 10;
            float bgWidth = 4 * CardUI.CARD_WIDTH + 3 * 20 + bgPadding * 2;
            float bgHeight = CardUI.CARD_HEIGHT + 20;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.3f);
            shapeRenderer.rect(bgX, bgY, bgWidth, bgHeight);
            shapeRenderer.end();

            // Border
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.4f, 0.4f, 0.4f, 1);
            Gdx.gl.glLineWidth(2);
            shapeRenderer.rect(bgX, bgY, bgWidth, bgHeight);
            Gdx.gl.glLineWidth(1);
            shapeRenderer.end();
        }

        // Draw deck slots
        for (int i = 0; i < 4; i++) {
            Rectangle slot = deckSlots.get(i);

            if (i < selectedDeck.size()) {
                // Draw card in slot
                String key = selectedDeck.get(i);
                UnitData unit = UnitFactory.getInstance().getUnit(key);
                CardUI card = new CardUI(key, unit, slot.x, slot.y);
                card.setSelected(true);
                card.render(shapeRenderer, batch, font);
            } else {
                // Draw empty slot
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
                shapeRenderer.rect(slot.x, slot.y, slot.width, slot.height);
                shapeRenderer.end();

                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(0.4f, 0.4f, 0.4f, 1);
                for (int d = 0; d < 5; d++) {
                    shapeRenderer.line(slot.x + d, slot.y, slot.x + slot.width, slot.y + slot.height - d);
                    shapeRenderer.line(slot.x, slot.y + d, slot.x + slot.width - d, slot.y + slot.height);
                }
                shapeRenderer.end();

                batch.begin();
                font.getData().setScale(0.6f);
                font.setColor(0.4f, 0.4f, 0.4f, 1);
                font.draw(batch, "Slot " + (i + 1), slot.x + 15, slot.y + slot.height / 2);
                batch.end();
            }
        }

        // Draw start button
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        boolean canStart = selectedDeck.size() == 4;
        if (canStart) {
            shapeRenderer.setColor(0.549f, 0.757f, 0.322f, 1); // Green
        } else {
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1); // Gray
        }
        shapeRenderer.rect(startButton.x, startButton.y, startButton.width, startButton.height);
        shapeRenderer.end();

        batch.begin();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        font.draw(batch, "BATTLE!", startButton.x + 55, startButton.y + 28);
        font.getData().setScale(1f);
        batch.end();
    }

    private void startGame() {
        GameStateManager.getInstance().setPlayerDeck(selectedDeck);
        game.setScreen(new GameScreen(game));
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

package com.NetRoyale.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.NetRoyale.NetRoyale;
import com.NetRoyale.patterns.factory.UnitFactory;
import com.NetRoyale.models.UnitData;
import com.NetRoyale.managers.GameStateManager;
import com.NetRoyale.managers.RenderManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deck Builder Screen - Simple rendering without Scene2D
 */
public class DeckBuilderScreen implements Screen {
    private NetRoyale game;
    private List<String> selectedDeck;
    private List<CardButton> cardButtons;
    private List<DeckSlot> deckSlots;
    private Rectangle startButton;
    
    private RenderManager renderManager;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    public DeckBuilderScreen(NetRoyale game) {
        this.game = game;
        this.selectedDeck = new ArrayList<>();
        this.cardButtons = new ArrayList<>();
        this.deckSlots = new ArrayList<>();
        
        this.renderManager = RenderManager.getInstance();
        this.batch = renderManager.getBatch();
        this.shapeRenderer = renderManager.getShapeRenderer();
        this.font = renderManager.getFont();
        
        this.startButton = new Rectangle(325, 50, 200, 50);
        
        createUI();
    }

    private void createUI() {
        // Create card buttons for all units
        UnitFactory factory = UnitFactory.getInstance();
        Map<String, UnitData> allUnits = factory.getAllUnits();
        
        float startX = 100;
        float startY = 350;
        float cardWidth = 70;
        float cardHeight = 95;
        float padding = 15;
        
        int col = 0;
        int row = 0;
        
        for (Map.Entry<String, UnitData> entry : allUnits.entrySet()) {
            float x = startX + col * (cardWidth + padding);
            float y = startY - row * (cardHeight + padding);
            
            cardButtons.add(new CardButton(entry.getKey(), entry.getValue(), x, y, cardWidth, cardHeight));
            
            col++;
            if (col >= 5) {
                col = 0;
                row++;
            }
        }
        
        // Create deck slots
        float deckY = 130;
        for (int i = 0; i < 4; i++) {
            float x = 200 + i * (cardWidth + padding);
            deckSlots.add(new DeckSlot(i, x, deckY, cardWidth, cardHeight));
        }
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.176f, 0.208f, 0.282f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        handleInput();
        draw();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
            
            // Check card buttons
            for (CardButton card : cardButtons) {
                if (card.bounds.contains(mouseX, mouseY)) {
                    addToDeck(card.key);
                    return;
                }
            }
            
            // Check deck slots (for removal)
            for (int i = 0; i < selectedDeck.size(); i++) {
                if (deckSlots.get(i).bounds.contains(mouseX, mouseY)) {
                    removeFromDeck(i);
                    return;
                }
            }
            
            // Check start button
            if (startButton.contains(mouseX, mouseY) && selectedDeck.size() == 4) {
                startGame();
            }
        }
    }

    private void draw() {
        batch.begin();
        
        // Title
        font.getData().setScale(2f);
        font.setColor(Color.GOLD);
        font.draw(batch, "DECK BUILDING", 300, 450);
        
        font.getData().setScale(1f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Pilih 4 Pasukan untuk dibawa ke arena", 240, 420);
        
        // Collection label
        font.setColor(Color.WHITE);
        font.draw(batch, "Collection (10 units)", 100, 380);
        
        // Deck label
        font.draw(batch, "Battle Deck (" + selectedDeck.size() + "/4)", 200, 160);
        
        batch.end();
        
        // Draw cards
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (CardButton card : cardButtons) {
            drawCard(card, selectedDeck.contains(card.key));
        }
        shapeRenderer.end();
        
        // Draw deck slots
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 4; i++) {
            drawDeckSlot(deckSlots.get(i), i < selectedDeck.size() ? selectedDeck.get(i) : null);
        }
        shapeRenderer.end();
        
        // Draw start button
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        boolean canStart = selectedDeck.size() == 4;
        shapeRenderer.setColor(canStart ? new Color(0.549f, 0.757f, 0.322f, 1) : Color.GRAY);
        shapeRenderer.rect(startButton.x, startButton.y, startButton.width, startButton.height);
        shapeRenderer.end();
        
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        font.draw(batch, "BATTLE!", startButton.x + 50, startButton.y + 35);
        font.getData().setScale(1f);
        
        // Draw card info text
        for (CardButton card : cardButtons) {
            font.setColor(selectedDeck.contains(card.key) ? Color.GREEN : Color.WHITE);
            font.getData().setScale(0.8f);
            font.draw(batch, card.unit.getIcon(), card.bounds.x + 22, card.bounds.y + 70);
            font.getData().setScale(0.6f);
            font.draw(batch, card.unit.getName(), card.bounds.x + 5, card.bounds.y + 45);
            font.draw(batch, card.unit.getCost() + " elixir", card.bounds.x + 10, card.bounds.y + 25);
            font.getData().setScale(1f);
        }
        
        // Draw deck slot info
        for (int i = 0; i < selectedDeck.size(); i++) {
            String key = selectedDeck.get(i);
            UnitData unit = UnitFactory.getInstance().getUnit(key);
            DeckSlot slot = deckSlots.get(i);
            font.setColor(Color.YELLOW);
            font.getData().setScale(0.8f);
            font.draw(batch, unit.getIcon(), slot.bounds.x + 22, slot.bounds.y + 70);
            font.getData().setScale(0.6f);
            font.draw(batch, unit.getName(), slot.bounds.x + 5, slot.bounds.y + 25);
            font.getData().setScale(1f);
        }
        
        batch.end();
    }

    private void drawCard(CardButton card, boolean selected) {
        if (selected) {
            shapeRenderer.setColor(Color.GREEN);
        } else {
            shapeRenderer.setColor(Color.WHITE);
        }
        shapeRenderer.rect(card.bounds.x, card.bounds.y, card.bounds.width, card.bounds.height);
        
        // Border
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(selected ? Color.GOLD : Color.DARK_GRAY);
        shapeRenderer.rect(card.bounds.x, card.bounds.y, card.bounds.width, card.bounds.height);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    private void drawDeckSlot(DeckSlot slot, String key) {
        if (key != null) {
            shapeRenderer.setColor(Color.YELLOW);
        } else {
            shapeRenderer.setColor(Color.DARK_GRAY);
        }
        shapeRenderer.rect(slot.bounds.x, slot.bounds.y, slot.bounds.width, slot.bounds.height);
        
        // Border
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(slot.bounds.x, slot.bounds.y, slot.bounds.width, slot.bounds.height);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    private void addToDeck(String key) {
        if (selectedDeck.contains(key)) return;
        if (selectedDeck.size() >= 4) return;
        selectedDeck.add(key);
    }

    private void removeFromDeck(int index) {
        if (index < selectedDeck.size()) {
            selectedDeck.remove(index);
        }
    }

    private void startGame() {
        GameStateManager.getInstance().setPlayerDeck(selectedDeck);
        game.setScreen(new GameScreen(game));
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
    
    // Helper classes
    private static class CardButton {
        String key;
        UnitData unit;
        Rectangle bounds;
        
        CardButton(String key, UnitData unit, float x, float y, float width, float height) {
            this.key = key;
            this.unit = unit;
            this.bounds = new Rectangle(x, y, width, height);
        }
    }
    
    private static class DeckSlot {
        int index;
        Rectangle bounds;
        
        DeckSlot(int index, float x, float y, float width, float height) {
            this.index = index;
            this.bounds = new Rectangle(x, y, width, height);
        }
    }
}

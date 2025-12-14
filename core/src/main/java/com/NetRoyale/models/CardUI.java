package com.NetRoyale.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

// Card UI
public class CardUI {
    private UnitData unit;
    private String key;
    private Rectangle bounds;
    private boolean selected;
    private boolean disabled;
    private boolean dragging;
    private Vector2 dragOffset;
    
    public static final float CARD_WIDTH = 70f;
    public static final float CARD_HEIGHT = 95f;

    public CardUI(String key, UnitData unit, float x, float y) {
        this.key = key;
        this.unit = unit;
        this.bounds = new Rectangle(x, y, CARD_WIDTH, CARD_HEIGHT);
        this.selected = false;
        this.disabled = false;
        this.dragging = false;
        this.dragOffset = new Vector2();
    }

    public void render(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        // Card background
        sr.begin(ShapeRenderer.ShapeType.Filled);
        if (disabled) {
            sr.setColor(0.4f, 0.4f, 0.4f, 1);
        } else if (selected) {
            sr.setColor(1, 0.95f, 0.8f, 1);
        } else {
            sr.setColor(1, 1, 1, 1);
        }
        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        sr.end();
        
        // Border
        sr.begin(ShapeRenderer.ShapeType.Line);
        if (selected) {
            sr.setColor(Color.GOLD);
            Gdx.gl.glLineWidth(3);
        } else {
            sr.setColor(0.6f, 0.6f, 0.6f, 1);
            Gdx.gl.glLineWidth(1);
        }
        sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        Gdx.gl.glLineWidth(1);
        sr.end();
        
        // Elixir badge (drop shape in top-left)
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.588f, 0.478f, 0.863f, 1); // Purple
        float badgeSize = 20;
        sr.circle(bounds.x + badgeSize / 2 + 2, bounds.y + bounds.height - badgeSize / 2 - 2, badgeSize / 2);
        sr.end();
        
        // Draw content
        batch.begin();
        
        // Elixir cost
        font.setColor(Color.WHITE);
        font.getData().setScale(0.7f);
        String costStr = String.valueOf(unit.getCost());
        font.draw(batch, costStr, bounds.x + 8, bounds.y + bounds.height - 5);
        
        // Icon
        font.getData().setScale(2f);
        font.setColor(disabled ? Color.GRAY : Color.BLACK);
        font.draw(batch, unit.getIcon(), bounds.x + 20, bounds.y + 60);
        
        // Name
        font.getData().setScale(0.5f);
        font.setColor(Color.DARK_GRAY);
        font.draw(batch, unit.getName(), bounds.x + 5, bounds.y + 15);
        
        // Type
        font.getData().setScale(0.4f);
        font.setColor(Color.GRAY);
        font.draw(batch, unit.getType(), bounds.x + 5, bounds.y + 5);
        
        font.getData().setScale(1f);
        batch.end();
    }

    public void startDrag(float x, float y) {
        dragging = true;
        dragOffset.set(x - bounds.x, y - bounds.y);
    }

    public void updateDrag(float x, float y) {
        if (dragging) {
            bounds.x = x - dragOffset.x;
            bounds.y = y - dragOffset.y;
        }
    }

    public void stopDrag() {
        dragging = false;
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    public void setPosition(float x, float y) {
        bounds.x = x;
        bounds.y = y;
    }

    // Getters and setters
    public String getKey() { return key; }
    public UnitData getUnit() { return unit; }
    public Rectangle getBounds() { return bounds; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }
    public boolean isDragging() { return dragging; }
}

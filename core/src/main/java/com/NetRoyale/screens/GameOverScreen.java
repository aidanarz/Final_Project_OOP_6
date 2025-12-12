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
import com.NetRoyale.managers.RenderManager;

/**
 * Game Over Screen
 */
public class GameOverScreen implements Screen {
    private NetRoyale game;
    private boolean victory;
    
    private RenderManager renderManager;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont largeFont;
    
    private Rectangle playAgainButton;

    public GameOverScreen(NetRoyale game, boolean victory) {
        this.game = game;
        this.victory = victory;
        
        this.renderManager = RenderManager.getInstance();
        this.batch = renderManager.getBatch();
        this.shapeRenderer = renderManager.getShapeRenderer();
        this.font = renderManager.getFont();
        this.largeFont = renderManager.getLargeFont();
        
        this.playAgainButton = new Rectangle(325, 150, 200, 50);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0.85f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        handleInput();
        draw();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
            
            if (playAgainButton.contains(mouseX, mouseY)) {
                game.setScreen(new DeckBuilderScreen(game));
            }
        }
    }

    private void draw() {
        // Draw button
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.549f, 0.757f, 0.322f, 1));
        shapeRenderer.rect(playAgainButton.x, playAgainButton.y, playAgainButton.width, playAgainButton.height);
        shapeRenderer.end();
        
        batch.begin();
        
        // Title
        largeFont.setColor(victory ? Color.GREEN : Color.RED);
        String titleText = victory ? "VICTORY!" : "DEFEAT";
        largeFont.draw(batch, titleText, 320, 350);
        
        // Description
        font.getData().setScale(1.5f);
        font.setColor(Color.LIGHT_GRAY);
        String descText = victory ? "Enemy King Destroyed!" : "Your King has fallen!";
        font.draw(batch, descText, 280, 280);
        font.getData().setScale(1f);
        
        // Button text
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        font.draw(batch, "MAIN LAGI", playAgainButton.x + 40, playAgainButton.y + 35);
        font.getData().setScale(1f);
        
        batch.end();
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
}

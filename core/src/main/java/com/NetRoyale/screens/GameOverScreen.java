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
import com.NetRoyale.patterns.singleton.LevelManager;
import com.badlogic.gdx.math.Vector3;

/**
 * Game Over Screen - With Level Progression
 * Integrates Strategy Pattern for level system
 */
public class GameOverScreen implements Screen {
    private static final float GAME_WIDTH = 850f;
    private static final float GAME_HEIGHT = 480f;

    private NetRoyale game;
    private boolean victory;
    private String message;

    private RenderManager renderManager;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Rectangle playAgainButton;
    private LevelManager levelManager;
    private boolean isGameCompleted;

    public GameOverScreen(NetRoyale game, boolean victory, String message) {
        this.game = game;
        this.victory = victory;
        this.message = message;

        this.renderManager = RenderManager.getInstance();
        this.batch = renderManager.getBatch();
        this.shapeRenderer = renderManager.getShapeRenderer();
        this.font = renderManager.getFont();

        this.levelManager = LevelManager.getInstance();

        // Handle level progression
        if (victory) {
            levelManager.onLevelComplete();
            isGameCompleted = levelManager.hasCompletedAllLevels();
        } else {
            levelManager.onLevelFailed();
            isGameCompleted = false;
        }

        this.playAgainButton = new Rectangle(GAME_WIDTH / 2 - 100, GAME_HEIGHT / 2 - 80, 200, 50);
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
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

            renderManager.getCamera().unproject(touchPos);

            float mouseX = touchPos.x;
            float mouseY = touchPos.y;

            if (playAgainButton.contains(mouseX, mouseY)) {
                game.setScreen(new DeckBuilderScreen(game));
            }
        }
    }

    private void draw() {
        batch.setProjectionMatrix(renderManager.getCamera().combined);
        shapeRenderer.setProjectionMatrix(renderManager.getCamera().combined);

        batch.begin();

        // Check if game completed (all levels beaten)
        if (isGameCompleted) {
            // Final victory screen
            font.getData().setScale(2.5f);
            font.setColor(1f, 0.843f, 0f, 1); // Gold
            font.draw(batch, "🏆 GAME COMPLETE! 🏆", GAME_WIDTH / 2 - 230, GAME_HEIGHT / 2 + 130);

            font.getData().setScale(2f);
            font.setColor(0.282f, 0.725f, 0.314f, 1);
            font.draw(batch, "KAMU MENANG!", GAME_WIDTH / 2 - 140, GAME_HEIGHT / 2 + 70);

            font.getData().setScale(1.2f);
            font.setColor(1f, 1f, 1f, 1);
            font.draw(batch, "Selamat! Semua 5 level berhasil diselesaikan!", GAME_WIDTH / 2 - 230, GAME_HEIGHT / 2 + 20);

            font.getData().setScale(0.9f);
            font.setColor(1f, 0.843f, 0f, 1);
            font.draw(batch, "Kamu adalah Master of Kingdom Clash!", GAME_WIDTH / 2 - 170, GAME_HEIGHT / 2 - 10);
        } else {
            // Normal victory/defeat screen
            font.getData().setScale(3f);
            if (victory) {
                font.setColor(0.282f, 0.725f, 0.314f, 1); // Green
                font.draw(batch, "⭐ VICTORY! ⭐", GAME_WIDTH / 2 - 160, GAME_HEIGHT / 2 + 130);
            } else {
                font.setColor(0.957f, 0.263f, 0.212f, 1); // Red
                font.draw(batch, "💀 DEFEAT 💀", GAME_WIDTH / 2 - 140, GAME_HEIGHT / 2 + 130);
            }

            // Message
            font.getData().setScale(1.2f);
            font.setColor(1f, 1f, 1f, 1);
            font.draw(batch, message, GAME_WIDTH / 2 - message.length() * 5, GAME_HEIGHT / 2 + 70);

            // Level info box
            font.getData().setScale(1f);
            if (victory && !levelManager.isMaxLevel()) {
                font.setColor(1f, 0.843f, 0f, 1); // Gold
                font.draw(batch, "Level Berikutnya:", GAME_WIDTH / 2 - 80, GAME_HEIGHT / 2 + 30);

                font.getData().setScale(1.2f);
                font.setColor(1f, 1f, 1f, 1);
                String levelInfo = levelManager.getLevelInfo();
                font.draw(batch, levelInfo, GAME_WIDTH / 2 - levelInfo.length() * 4, GAME_HEIGHT / 2);

                font.getData().setScale(0.8f);
                font.setColor(0.957f, 0.263f, 0.212f, 1);
                String diff = levelManager.getDifficultyInfo();
                font.draw(batch, diff, GAME_WIDTH / 2 - diff.length() * 3, GAME_HEIGHT / 2 - 25);
            } else if (!victory) {
                font.setColor(1f, 1f, 1f, 1);
                font.draw(batch, "Coba Lagi:", GAME_WIDTH / 2 - 50, GAME_HEIGHT / 2 + 30);

                font.getData().setScale(1.2f);
                String levelInfo = levelManager.getLevelInfo();
                font.draw(batch, levelInfo, GAME_WIDTH / 2 - levelInfo.length() * 4, GAME_HEIGHT / 2);

                font.getData().setScale(0.8f);
                font.setColor(0.957f, 0.263f, 0.212f, 1);
                String diff = levelManager.getDifficultyInfo();
                font.draw(batch, diff, GAME_WIDTH / 2 - diff.length() * 3, GAME_HEIGHT / 2 - 25);
            }
        }

        font.getData().setScale(1f);
        batch.end();

        // Play again button
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.549f, 0.757f, 0.322f, 1); // Green
        shapeRenderer.rect(playAgainButton.x, playAgainButton.y, playAgainButton.width, playAgainButton.height);
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        String buttonText = isGameCompleted ? "MAIN LAGI" : (victory && !levelManager.isMaxLevel() ? "LANJUT" : "RETRY");
        font.draw(batch, buttonText, playAgainButton.x + (buttonText.equals("LANJUT") ? 50 : (buttonText.equals("RETRY") ? 50 : 30)), playAgainButton.y + 33);
        font.getData().setScale(1f);
        batch.end();
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

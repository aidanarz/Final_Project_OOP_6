package com.NetRoyale.managers;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;

// Singleton untuk manage rendering
public class RenderManager {
    private static RenderManager instance;
    
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont largeFont;
    private OrthographicCamera camera;

    private RenderManager() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.2f);
        largeFont = new BitmapFont();
        largeFont.getData().setScale(2.5f);
        
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 850, 480);
    }

    public static RenderManager getInstance() {
        if (instance == null) {
            instance = new RenderManager();
        }
        return instance;
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        largeFont.dispose();
    }

    public SpriteBatch getBatch() { return batch; }
    public ShapeRenderer getShapeRenderer() { return shapeRenderer; }
    public BitmapFont getFont() { return font; }
    public BitmapFont getLargeFont() { return largeFont; }
    public OrthographicCamera getCamera() { return camera; }
}

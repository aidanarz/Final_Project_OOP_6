package com.NetRoyale.managers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Gdx;

// Manager untuk animasi tower
public class TowerAnimationManager {
    private static TowerAnimationManager instance;
    
    private Texture towerSpriteSheet;
    private Animation<TextureRegion> towerAnimation;
    private TextureRegion[] frames;
    
    private TowerAnimationManager() {
        loadTowerAnimation();
    }
    
    public static TowerAnimationManager getInstance() {
        if (instance == null) {
            instance = new TowerAnimationManager();
        }
        return instance;
    }
    
    private void loadTowerAnimation() {
        try {
            // Load the sprite sheet
            towerSpriteSheet = new Texture(Gdx.files.internal("Archertower/7.png"));
            
            System.out.println("Tower sprite sheet loaded: " + towerSpriteSheet.getWidth() + "x" + towerSpriteSheet.getHeight());
            
            // Split menggunakan TextureRegion.split seperti contoh run.png
            // Asumsikan ada 7 frame horizontal
            int frameWidth = towerSpriteSheet.getWidth() / 7;
            int frameHeight = towerSpriteSheet.getHeight();
            
            TextureRegion[][] frames2D = TextureRegion.split(towerSpriteSheet, frameWidth, frameHeight);
            
            // Ambil 7 frames dari row pertama
            frames = new TextureRegion[7];
            for (int i = 0; i < 7; i++) {
                frames[i] = frames2D[0][i];
            }
            
            System.out.println("Frames created: " + frames.length + " (" + frameWidth + "x" + frameHeight + " each)");
            
            // Create animation - lebih lambat untuk bendera berkibar
            // 1f/6f = 6 FPS untuk animasi yang smooth dan tidak terlalu cepat
            towerAnimation = new Animation<>(1f/6f, frames);
            towerAnimation.setPlayMode(Animation.PlayMode.LOOP);
            
        } catch (Exception e) {
            System.err.println("Failed to load tower animation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Tower static, return frame pertama
    public TextureRegion getCurrentFrame(float stateTime) {
        if (frames != null && frames.length > 0) {
            // Return first frame only - tower tidak animasi
            return frames[0];
        }
        return null;
    }
    
    public TextureRegion getFrame(int index) {
        if (frames != null && index >= 0 && index < frames.length) {
            return frames[index];
        }
        return null;
    }
    
    public int getFrameWidth() {
        if (frames != null && frames.length > 0) {
            return frames[0].getRegionWidth();
        }
        return 0;
    }
    
    public int getFrameHeight() {
        if (frames != null && frames.length > 0) {
            return frames[0].getRegionHeight();
        }
        return 0;
    }
    
    public void dispose() {
        if (towerSpriteSheet != null) {
            towerSpriteSheet.dispose();
        }
    }
}

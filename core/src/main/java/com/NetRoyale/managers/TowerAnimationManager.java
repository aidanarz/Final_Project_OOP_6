package com.NetRoyale.managers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.Gdx;

// Manager untuk animasi tower
public class TowerAnimationManager {
    private static TowerAnimationManager instance;
    
    private Map<String, Texture> towerTextures;
    private Map<String, Animation<TextureRegion>> towerAnimations;
    private Map<String, TextureRegion[]> towerFrames;
    
    private TowerAnimationManager() {
        towerTextures = new HashMap<>();
        towerAnimations = new HashMap<>();
        towerFrames = new HashMap<>();
        loadTowerAnimations();
    }
    
    public static TowerAnimationManager getInstance() {
        if (instance == null) {
            instance = new TowerAnimationManager();
        }
        return instance;
    }
    
    private void loadTowerAnimations() {
        // Load King Tower (7.png)
        loadTowerAnimation("king", "Archertower/7.png", 6);
        
        // Load Archer Tower (4.png)
        loadTowerAnimation("princess", "Archertower/4.png", 6);
    }
    
    private void loadTowerAnimation(String towerType, String path, int frameCount) {
        try {
            // Load the sprite sheet
            Texture spriteSheet = new Texture(Gdx.files.internal(path));
            towerTextures.put(towerType, spriteSheet);
            
            System.out.println(towerType + " tower sprite loaded: " + spriteSheet.getWidth() + "x" + spriteSheet.getHeight());
            
            // Split menggunakan TextureRegion.split
            int frameWidth = spriteSheet.getWidth() / frameCount;
            int frameHeight = spriteSheet.getHeight();
            
            TextureRegion[][] frames2D = TextureRegion.split(spriteSheet, frameWidth, frameHeight);
            
            // Ambil frames dari row pertama
            TextureRegion[] frames = new TextureRegion[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = frames2D[0][i];
            }
            towerFrames.put(towerType, frames);
            
            System.out.println(towerType + " tower frames: " + frames.length + " (" + frameWidth + "x" + frameHeight + " each)");
            
            // Create animation
            Animation<TextureRegion> animation = new Animation<>(1f/6f, frames);
            animation.setPlayMode(Animation.PlayMode.LOOP);
            towerAnimations.put(towerType, animation);
            
        } catch (Exception e) {
            System.err.println("Failed to load " + towerType + " tower animation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get animated frame based on stateTime and tower type
     * Untuk animasi bendera berkibar pada tower
     */
    public TextureRegion getCurrentFrame(String towerType, float stateTime) {
        Animation<TextureRegion> animation = towerAnimations.get(towerType);
        if (animation != null) {
            return animation.getKeyFrame(stateTime, true);
        }
        return null;
    }
    
    /**
     * Get specific frame (for static display)
     */
    public TextureRegion getFrame(String towerType, int index) {
        TextureRegion[] frames = towerFrames.get(towerType);
        if (frames != null && index >= 0 && index < frames.length) {
            return frames[index];
        }
        return null;
    }
    
    /**
     * Get frame width
     */
    public int getFrameWidth(String towerType) {
        TextureRegion[] frames = towerFrames.get(towerType);
        if (frames != null && frames.length > 0) {
            return frames[0].getRegionWidth();
        }
        return 0;
    }
    
    /**
     * Get frame height
     */
    public int getFrameHeight(String towerType) {
        TextureRegion[] frames = towerFrames.get(towerType);
        if (frames != null && frames.length > 0) {
            return frames[0].getRegionHeight();
        }
        return 0;
    }
    
    public void dispose() {
        for (Texture texture : towerTextures.values()) {
            texture.dispose();
        }
        towerTextures.clear();
        towerAnimations.clear();
        towerFrames.clear();
    }
}

package com.NetRoyale.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.NetRoyale.models.AnimationState;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit Animation Manager (Singleton Pattern)
 * 
 * Mengikuti referensi LibGDX: https://libgdx.com/wiki/graphics/2d/2d-animation
 * 
 * Key concepts dari dokumentasi:
 * 1. Animation adalah state machine sederhana
 * 2. Frame ditentukan oleh elapsed time sejak animasi dimulai
 * 3. Gunakan TextureRegion.split() untuk memotong sprite sheet
 * 4. getKeyFrame(stateTime, looping) untuk mendapatkan frame saat ini
 */
public class UnitAnimationManager {
    private static UnitAnimationManager instance;
    
    // Map: unitKey -> Map(AnimationState -> Animation)
    private Map<String, Map<AnimationState, Animation<TextureRegion>>> animations;
    
    // Store textures for proper disposal
    private Map<String, Texture> textures;
    
    private UnitAnimationManager() {
        animations = new HashMap<>();
        textures = new HashMap<>();
        loadAllAnimations();
    }
    
    public static UnitAnimationManager getInstance() {
        if (instance == null) {
            instance = new UnitAnimationManager();
        }
        return instance;
    }
    
    private void loadAllAnimations() {
        // ===== KNIGHT ===== (6 frames walk, 5 frames attack)
        loadAnimationGrid("knight", AnimationState.WALK, "knight/knight_walk.png", 
                        6, 1, 0.1f, PlayMode.LOOP);
        loadAnimationGrid("knight", AnimationState.ATTACK, "knight/knight_attack.png", 
                        5, 1, 0.1f, PlayMode.LOOP);
        
        // ===== ARCHER ===== (5 frames walk, 5 frames attack)
        loadAnimationGrid("archer", AnimationState.WALK, "archers/archer_walk.png", 
                        5, 1, 0.1f, PlayMode.LOOP);
        loadAnimationGrid("archer", AnimationState.ATTACK, "archers/archer_attack.png", 
                        5, 1, 0.08f, PlayMode.LOOP);
        
        // ===== GIANT ===== (6 frames walk, 6 frames attack)
        loadAnimationGrid("giant", AnimationState.WALK, "giant/giant_walk.png", 
                        6, 1, 0.15f, PlayMode.LOOP);
        loadAnimationGrid("giant", AnimationState.ATTACK, "giant/giant_attack.png", 
                        6, 1, 0.12f, PlayMode.LOOP);
        
        // ===== WIZARD ===== (6 frames walk, 6 frames attack)
        loadAnimationGrid("wizard", AnimationState.WALK, "wizard/wizard_walk.png", 
                            6, 1, 0.12f, PlayMode.LOOP);
        loadAnimationGrid("wizard", AnimationState.ATTACK, "wizard/wizard_attack.png", 
                            6, 1, 0.08f, PlayMode.LOOP);

        // ===== GOBLIN ===== (6 frames walk, 6 frames attack)
        loadAnimationGrid("goblin", AnimationState.WALK, "goblin/goblin_walk.png", 
                            6, 1, 0.1f, PlayMode.LOOP);
        loadAnimationGrid("goblin", AnimationState.ATTACK, "goblin/goblin_attack.png", 
                            6, 1, 0.08f, PlayMode.LOOP);
        
        // ===== SKELETON ===== (6 frames walk, 6 frames attack)
        loadAnimationGrid("skeleton", AnimationState.WALK, "skeleton/skeleton_walk.png", 
                        6, 1, 0.08f, PlayMode.LOOP);
        loadAnimationGrid("skeleton", AnimationState.ATTACK, "skeleton/skeleton_attack.png", 
                        6, 1, 0.08f, PlayMode.LOOP);
        
        // ===== VALKYRIE ===== (6 frames walk, 6 frames attack)
        loadAnimationGrid("valkyrie", AnimationState.WALK, "valk/valk_walk.png", 
                        6, 1, 0.10f, PlayMode.LOOP);
        loadAnimationGrid("valkyrie", AnimationState.ATTACK, "valk/valk_attack.png", 
                        6, 1, 0.1f, PlayMode.LOOP);
        
        // ===== BOMBER ===== (6 frames walk, 6 frames attack)
        loadAnimationGrid("bomber", AnimationState.WALK, "bomber/bomber_walk.png", 
                        6, 1, 0.1f, PlayMode.LOOP);
        loadAnimationGrid("bomber", AnimationState.ATTACK, "bomber/bomber_attack.png", 
                        6, 1, 0.08f, PlayMode.LOOP);
        
        // ===== GOLEM ===== (6 frames walk, 6 frames attack)
        loadAnimationGrid("golem", AnimationState.WALK, "golem/golem_walk.png", 
                            6, 1, 0.15f, PlayMode.LOOP);
        loadAnimationGrid("golem", AnimationState.ATTACK, "golem/golem_attack.png", 
                            6, 1, 0.12f, PlayMode.LOOP);
        
        // ===== MUSKETEER ===== (6 frames walk, 2 frames attack)
        loadAnimationGrid("musketeer", AnimationState.WALK, "musketer/musketer_walk.png", 
                         6, 1, 0.08f, PlayMode.LOOP);
        loadAnimationGrid("musketeer", AnimationState.ATTACK, "musketer/musketer_attack.png", 
                         2, 1, 0.1f, PlayMode.LOOP);
        
        System.out.println("Unit animations loaded: " + animations.size() + " units");
    }
    
    /**
     * Load animation from grid sprite sheet (multiple rows and columns)
     * Sesuai referensi LibGDX: TextureRegion.split(texture, width/COLS, height/ROWS)
     * 
     * @param unitKey Unit identifier
     * @param state Animation state
     * @param path Path to sprite sheet
     * @param cols Number of columns in sprite sheet (FRAME_COLS)
     * @param rows Number of rows in sprite sheet (FRAME_ROWS)
     * @param frameDuration Time per frame in seconds (1/FPS)
     * @param playMode Animation play mode (LOOP, NORMAL, etc)
     */
    private void loadAnimationGrid(String unitKey, AnimationState state, String path,
                                   int cols, int rows, float frameDuration, PlayMode playMode) {
        try {
            // Load the sprite sheet as a Texture
            Texture spriteSheet = new Texture(Gdx.files.internal(path));
            textures.put(path, spriteSheet);
            
            // Calculate frame dimensions: width/FRAME_COLS, height/FRAME_ROWS
            int frameWidth = spriteSheet.getWidth() / cols;
            int frameHeight = spriteSheet.getHeight() / rows;
            
            System.out.println("Loading grid: " + path + " (" + spriteSheet.getWidth() + "x" + spriteSheet.getHeight() + 
                             ") -> " + cols + " cols x " + rows + " rows = " + frameWidth + "x" + frameHeight + " per frame");
            
            // Use the split utility method to create a 2D array of TextureRegions
            // Sesuai dokumentasi: TextureRegion.split(texture, frameWidth, frameHeight)
            TextureRegion[][] tmp = TextureRegion.split(spriteSheet, frameWidth, frameHeight);
            
            // Place the regions into a 1D array in the correct order
            // Starting from top left, going across first (row by row)
            int totalFrames = cols * rows;
            TextureRegion[] walkFrames = new TextureRegion[totalFrames];
            int index = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (i < tmp.length && j < tmp[i].length) {
                        walkFrames[index++] = tmp[i][j];
                    }
                }
            }
            
            // Trim array jika tidak semua slot terisi
            TextureRegion[] finalFrames = new TextureRegion[index];
            System.arraycopy(walkFrames, 0, finalFrames, 0, index);
            
            // Initialize the Animation with the frame interval and array of frames
            // Sesuai dokumentasi: new Animation<TextureRegion>(frameDuration, walkFrames)
            Animation<TextureRegion> animation = new Animation<TextureRegion>(frameDuration, finalFrames);
            animation.setPlayMode(playMode);
            
            // Store animation
            animations.putIfAbsent(unitKey, new HashMap<>());
            animations.get(unitKey).put(state, animation);
            
            System.out.println("  -> Loaded: " + unitKey + " " + state + " (" + index + " frames)");
            
        } catch (Exception e) {
            System.err.println("Failed to load grid animation: " + path + " - " + e.getMessage());
        }
    }
    
    /**
     * Get current frame of animation for the current stateTime
     * 
     * Sesuai referensi LibGDX:
     * 1. stateTime += Gdx.graphics.getDeltaTime(); (dilakukan di Entity/render)
     * 2. TextureRegion currentFrame = walkAnimation.getKeyFrame(stateTime, true);
     * 
     * @param unitKey Unit identifier
     * @param state Current animation state
     * @param stateTime Elapsed animation time (accumulated delta)
     * @return Current frame TextureRegion
     */
    public TextureRegion getCurrentFrame(String unitKey, AnimationState state, float stateTime) {
        Map<AnimationState, Animation<TextureRegion>> unitAnims = animations.get(unitKey);
        
        if (unitAnims == null) {
            return null;
        }
        
        Animation<TextureRegion> animation = unitAnims.get(state);
        
        // Handle IDLE - return first frame of WALK (static pose, no animation)
        if (state == AnimationState.IDLE) {
            Animation<TextureRegion> walkAnim = unitAnims.get(AnimationState.WALK);
            if (walkAnim != null) {
                // Return frame at time 0 = first frame, not looping
                return walkAnim.getKeyFrame(0, false);
            }
        }
        
        // Handle missing DEATH animation - use last frame of ATTACK as fallback
        if (state == AnimationState.DEATH && animation == null) {
            Animation<TextureRegion> attackAnim = unitAnims.get(AnimationState.ATTACK);
            if (attackAnim != null) {
                // Return last frame (looks like fallen/defeated)
                float duration = attackAnim.getAnimationDuration();
                return attackAnim.getKeyFrame(duration, false);
            }
        }
        
        // Normal animation playback
        if (animation != null) {
            // Sesuai dokumentasi: getKeyFrame(stateTime, looping)
            // looping = true untuk LOOP mode, false untuk NORMAL (play once)
            boolean looping = (animation.getPlayMode() == PlayMode.LOOP || 
                              animation.getPlayMode() == PlayMode.LOOP_PINGPONG ||
                              animation.getPlayMode() == PlayMode.LOOP_RANDOM ||
                              animation.getPlayMode() == PlayMode.LOOP_REVERSED);
            return animation.getKeyFrame(stateTime, looping);
        }
        
        // Final fallback to WALK animation
        Animation<TextureRegion> walkAnim = unitAnims.get(AnimationState.WALK);
        if (walkAnim != null) {
            return walkAnim.getKeyFrame(stateTime, true);
        }
        
        return null;
    }
    
    /**
     * Check if animation has finished (useful for non-looping animations like DEATH)
     * Sesuai dokumentasi: animation.isAnimationFinished(stateTime)
     */
    public boolean isAnimationFinished(String unitKey, AnimationState state, float stateTime) {
        Map<AnimationState, Animation<TextureRegion>> unitAnims = animations.get(unitKey);
        if (unitAnims != null) {
            Animation<TextureRegion> animation = unitAnims.get(state);
            if (animation != null) {
                return animation.isAnimationFinished(stateTime);
            }
        }
        return true;
    }
    
    /**
     * Check if unit has animation for specific state
     */
    public boolean hasAnimation(String unitKey, AnimationState state) {
        Map<AnimationState, Animation<TextureRegion>> unitAnims = animations.get(unitKey);
        return unitAnims != null && unitAnims.containsKey(state);
    }
    
    /**
     * Dispose all textures - harus dipanggil saat game selesai
     * Sesuai referensi LibGDX: "In dispose(), dispose of the Texture"
     */
    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
        animations.clear();
    }
}

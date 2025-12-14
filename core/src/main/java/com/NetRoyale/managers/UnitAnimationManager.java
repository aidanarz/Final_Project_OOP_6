package com.NetRoyale.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.NetRoyale.models.AnimationState;

import java.util.HashMap;
import java.util.Map;

// Manager untuk animasi unit
public class UnitAnimationManager {
    private static UnitAnimationManager instance;
    
    // Map: unitKey -> Map(AnimationState -> Animation)
    private Map<String, Map<AnimationState, Animation<TextureRegion>>> animations;
    
    private UnitAnimationManager() {
        animations = new HashMap<>();
        loadAllAnimations();
    }
    
    public static UnitAnimationManager getInstance() {
        if (instance == null) {
            instance = new UnitAnimationManager();
        }
        return instance;
    }
    
    private void loadAllAnimations() {
        // Knight animations - kecepatan sedang
        loadUnitAnimation("knight", AnimationState.WALK, "knight/knight_walk.png", 8, 64, 64, 0.1f);
        loadUnitAnimation("knight", AnimationState.ATTACK, "knight/knight_attack.png", 8, 64, 64, 0.12f);
        
        // Archer animations - kecepatan sedang
        loadUnitAnimation("archer", AnimationState.WALK, "archers/archer_walk.png", 8, 64, 64, 0.1f);
        loadUnitAnimation("archer", AnimationState.ATTACK, "archers/archer_attack.png", 8, 64, 64, 0.12f);
        loadUnitAnimation("archer", AnimationState.DEATH, "archers/archer_death.png", 8, 64, 64, 0.1f);
        
        // Giant animations - lambat karena besar
        loadUnitAnimation("giant", AnimationState.WALK, "giant/giant_walk.png", 8, 64, 64, 0.15f);
        loadUnitAnimation("giant", AnimationState.ATTACK, "giant/giant_attack.png", 8, 64, 64, 0.18f);
        
        // Wizard animations - fly/hover smooth
        loadUnitAnimation("wizard", AnimationState.WALK, "wizard/S_Fly.png", 8, 64, 64, 0.1f);
        loadUnitAnimation("wizard", AnimationState.ATTACK, "wizard/S_Attack.png", 8, 64, 64, 0.12f);
        loadUnitAnimation("wizard", AnimationState.DEATH, "wizard/S_Death.png", 8, 64, 64, 0.1f);
        loadUnitAnimation("wizard", AnimationState.SPECIAL, "wizard/S_Special.png", 8, 64, 64, 0.1f);
        
        // Goblin animations - cepat/agile
        loadUnitAnimation("goblin", AnimationState.WALK, "goblin/S_Walk.png", 8, 64, 64, 0.08f);
        loadUnitAnimation("goblin", AnimationState.ATTACK, "goblin/S_Attack.png", 8, 64, 64, 0.1f);
        loadUnitAnimation("goblin", AnimationState.DEATH, "goblin/S_Death.png", 8, 64, 64, 0.1f);
        
        // Skeleton animations - cepat
        loadUnitAnimation("skeleton", AnimationState.WALK, "skeleton/skeleton_walk.png", 8, 64, 64, 0.08f);
        loadUnitAnimation("skeleton", AnimationState.ATTACK, "skeleton/skeleton_attack.png", 8, 64, 64, 0.1f);
        loadUnitAnimation("skeleton", AnimationState.DEATH, "skeleton/skeleton_death.png", 8, 64, 64, 0.1f);
        
        // Valkyrie animations - sedang, warrior
        loadUnitAnimation("valkyrie", AnimationState.WALK, "valk/valk_walk.png", 8, 64, 64, 0.1f);
        loadUnitAnimation("valkyrie", AnimationState.ATTACK, "valk/valk_attack.png", 8, 64, 64, 0.12f);
        loadUnitAnimation("valkyrie", AnimationState.DEATH, "valk/valk_death.png", 8, 64, 64, 0.1f);
        
        // Bomber animations - sedang
        loadUnitAnimation("bomber", AnimationState.WALK, "bomber/bomber_walk.png", 8, 64, 64, 0.1f);
        loadUnitAnimation("bomber", AnimationState.ATTACK, "bomber/bomber_attack.png", 8, 64, 64, 0.12f);
        loadUnitAnimation("bomber", AnimationState.DEATH, "bomber/bomber_death.png", 8, 64, 64, 0.1f);
        
        // Golem animations - SANGAT lambat karena besar
        loadUnitAnimation("golem", AnimationState.WALK, "golem/golem_walk.png", 8, 64, 64, 0.18f);
        loadUnitAnimation("golem", AnimationState.ATTACK, "golem/golem_attack.png", 8, 64, 64, 0.2f);
        loadUnitAnimation("golem", AnimationState.DEATH, "golem/golem_death.png", 8, 64, 64, 0.15f);
        loadUnitAnimation("golem", AnimationState.SPECIAL, "golem/golem_special.png", 8, 64, 64, 0.15f);
        
        // Prince animations - cepat, charge
        loadUnitAnimation("prince", AnimationState.WALK, "prince/S_Run.png", 8, 64, 64, 0.09f);
        loadUnitAnimation("prince", AnimationState.ATTACK, "prince/S_Attack.png", 8, 64, 64, 0.11f);
        loadUnitAnimation("prince", AnimationState.DEATH, "prince/S_Death.png", 8, 64, 64, 0.1f);
        loadUnitAnimation("prince", AnimationState.SPECIAL, "prince/S_Special.png", 8, 64, 64, 0.1f);
        
        // Musketeer animations - sedang
        loadUnitAnimation("musketeer", AnimationState.WALK, "musketer/musketer_walk.png", 8, 64, 64, 0.1f);
        loadUnitAnimation("musketeer", AnimationState.ATTACK, "musketer/musketer_attack.png", 8, 64, 64, 0.12f);
        
        System.out.println("Unit animations loaded: " + animations.size() + " units");
    }
    
    private void loadUnitAnimation(String unitKey, AnimationState state, String path, 
                                   int frameCount, int frameWidth, int frameHeight, float frameDuration) {
        try {
            Texture texture = new Texture(Gdx.files.internal(path));
            
            // Split sprite sheet
            TextureRegion[][] frames2D = TextureRegion.split(texture, frameWidth, frameHeight);
            
            // Extract frames (assuming horizontal strip)
            TextureRegion[] frames = new TextureRegion[frameCount];
            for (int i = 0; i < frameCount && i < frames2D[0].length; i++) {
                frames[i] = frames2D[0][i];
            }
            
            // Create animation
            Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
            animation.setPlayMode(Animation.PlayMode.LOOP);
            
            // Store in nested map
            animations.putIfAbsent(unitKey, new HashMap<>());
            animations.get(unitKey).put(state, animation);
            
            System.out.println("Loaded: " + unitKey + " " + state + " (" + frameCount + " frames)");
            
        } catch (Exception e) {
            System.err.println("Failed to load animation: " + path + " - " + e.getMessage());
        }
    }
    
    // Get frame saat ini
    public TextureRegion getCurrentFrame(String unitKey, AnimationState state, float stateTime) {
        Map<AnimationState, Animation<TextureRegion>> unitAnims = animations.get(unitKey);
        
        if (unitAnims != null) {
            Animation<TextureRegion> animation = unitAnims.get(state);
            if (animation != null) {
                return animation.getKeyFrame(stateTime, true);
            }
            
            // Fallback to WALK if current state not available
            animation = unitAnims.get(AnimationState.WALK);
            if (animation != null) {
                return animation.getKeyFrame(stateTime, true);
            }
        }
        
        return null;
    }
    
    public boolean hasAnimation(String unitKey, AnimationState state) {
        Map<AnimationState, Animation<TextureRegion>> unitAnims = animations.get(unitKey);
        return unitAnims != null && unitAnims.containsKey(state);
    }
    
    public void dispose() {
        animations.clear();
    }
}

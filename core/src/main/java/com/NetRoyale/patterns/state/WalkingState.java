package com.NetRoyale.patterns.state;

import com.NetRoyale.models.AnimationState;
import com.NetRoyale.models.Entity;
import java.util.List;

// State saat unit berjalan
public class WalkingState implements UnitState {
    private static WalkingState instance;
    
    private WalkingState() {}
    
    public static WalkingState getInstance() {
        if (instance == null) {
            instance = new WalkingState();
        }
        return instance;
    }
    
    @Override
    public void execute(Entity entity, float delta, List<Entity> enemies) {
        // Set animation to WALK
        entity.setAnimationState(AnimationState.WALK);
        
        // Find target if not exist or dead or out of range
        if (entity.getTarget() == null || entity.getTarget().isDead() || !entity.isInRange(entity.getTarget())) {
            entity.setTarget(entity.findNearestTarget(enemies));
        }
        
        // If have target and in range, switch to attack state
        if (entity.getTarget() != null && entity.isInRange(entity.getTarget())) {
            entity.setState(AttackingState.getInstance());
            return;
        }
        
        // Move towards target or default direction
        if (entity.getData().getSpeed() > 0) {
            entity.moveTowardsTarget(delta);
        }
    }
    
    @Override
    public String getStateName() {
        return "WALKING";
    }
}

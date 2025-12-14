package com.NetRoyale.patterns.state;

import com.NetRoyale.models.AnimationState;
import com.NetRoyale.models.Entity;
import java.util.List;

// State saat unit menyerang
public class AttackingState implements UnitState {
    private static AttackingState instance;
    
    private AttackingState() {}
    
    public static AttackingState getInstance() {
        if (instance == null) {
            instance = new AttackingState();
        }
        return instance;
    }
    
    @Override
    public void execute(Entity entity, float delta, List<Entity> enemies) {
        // Set animation to ATTACK
        entity.setAnimationState(AnimationState.ATTACK);
        
        // Check if target still valid
        if (entity.getTarget() == null || entity.getTarget().isDead()) {
            entity.setState(WalkingState.getInstance());
            return;
        }
        
        // If target out of range, go back to walking
        if (!entity.isInRange(entity.getTarget())) {
            entity.setState(WalkingState.getInstance());
            return;
        }
        
        // Attack if cooldown ready
        if (entity.getAttackTimer() <= 0) {
            entity.performAttack();
            entity.resetAttackTimer();
        } else {
            entity.decrementAttackTimer(delta);
        }
    }
    
    @Override
    public String getStateName() {
        return "ATTACKING";
    }
}

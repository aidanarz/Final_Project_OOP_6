package com.NetRoyale.models;

import com.badlogic.gdx.math.Vector2;
import com.NetRoyale.patterns.state.UnitState;
import com.NetRoyale.patterns.state.WalkingState;
import java.util.List;

// Base class untuk entities
public class Entity {
    protected String key;
    protected Team team;
    protected Vector2 position;
    protected UnitData data;
    
    protected float hp;
    protected float maxHp;
    protected float attackTimer;
    protected Entity target;
    protected boolean isDead;
    protected float animOffset;
    protected UnitState currentState;
    
    // Animation state tracking
    protected float stateTime;
    protected AnimationState animationState;

    public Entity(String key, UnitData data, Team team, float x, float y) {
        this.key = key;
        this.data = data;
        this.team = team;
        this.position = new Vector2(x, y);
        this.hp = data.getHp();
        this.maxHp = data.getHp();
        this.attackTimer = 0;
        this.isDead = false;
        this.animOffset = (float) Math.random() * 10;
        this.currentState = WalkingState.getInstance();
        this.stateTime = 0f;
        this.animationState = AnimationState.WALK;
    }
    
    // Reset entity untuk pool
    public void reset(float x, float y, UnitData newData) {
        this.data = newData;
        this.position.set(x, y);
        this.hp = newData.getHp();
        this.maxHp = newData.getHp();
        this.attackTimer = 0;
        this.isDead = false;
        this.target = null;
        this.currentState = WalkingState.getInstance();
        this.stateTime = 0f;
        this.animationState = AnimationState.WALK;
    }

    // State Pattern - Delegate behavior to current state
    public void update(float delta, List<Entity> enemies) {
        if (isDead) return;
        stateTime += delta; // Update animation timer
        currentState.execute(this, delta, enemies);
    }

    // Methods for State Pattern
    public Entity findNearestTarget(List<Entity> enemies) {
        Entity closest = null;
        float minDist = Float.MAX_VALUE;
        
        for (Entity e : enemies) {
            float dist = position.dst(e.getPosition());
            if (dist < 300 && dist < minDist) {
                minDist = dist;
                closest = e;
            }
        }
        
        // Default to King
        if (closest == null) {
            for (Entity e : enemies) {
                if (e.isKing()) {
                    closest = e;
                    break;
                }
            }
        }
        return closest;
    }

    public boolean isInRange(Entity targetEntity) {
        float dist = position.dst(targetEntity.getPosition());
        return dist <= (data.getRange() + targetEntity.getData().getSize());
    }

    public void performAttack() {
        // Override in subclasses for different attack types
        if (target != null && !target.isDead()) {
            target.takeDamage(data.getDamage());
        }
    }

    public void moveTowardsTarget(float delta) {
        float dir = (team == Team.PLAYER) ? 1 : -1;
        float vx = data.getSpeed() * dir;
        float vy = 0;

        if (target != null) {
            float angle = (float) Math.atan2(target.getPosition().y - position.y, 
                                            target.getPosition().x - position.x);
            vx = (float) Math.cos(angle) * data.getSpeed();
            vy = (float) Math.sin(angle) * data.getSpeed();
        }

        position.x += vx * delta;
        position.y += vy * delta;
    }
    
    public void resetAttackTimer() {
        attackTimer = data.getCooldown();
    }
    
    public void decrementAttackTimer(float delta) {
        attackTimer -= delta;
    }

    public void takeDamage(float damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            isDead = true;
        }
    }

    // Getters and Setters
    public String getKey() { return key; }
    public Team getTeam() { return team; }
    public Vector2 getPosition() { return position; }
    public UnitData getData() { return data; }
    public float getHp() { return hp; }
    public float getMaxHp() { return maxHp; }
    public boolean isDead() { return isDead; }
    public float getAnimOffset() { return animOffset; }
    public boolean isKing() { return key.equals("king"); }
    public Entity getTarget() { return target; }
    public void setTarget(Entity target) { this.target = target; }
    public float getAttackTimer() { return attackTimer; }
    public UnitState getState() { return currentState; }
    public void setState(UnitState state) { this.currentState = state; }
    
    // Animation state
    public float getStateTime() { return stateTime; }
    public AnimationState getAnimationState() { return animationState; }
    public void setAnimationState(AnimationState state) { 
        if (this.animationState != state) {
            this.animationState = state;
            this.stateTime = 0f; // Reset animation when state changes
        }
    }
}

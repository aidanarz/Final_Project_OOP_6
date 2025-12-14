package com.NetRoyale.models;

import com.badlogic.gdx.math.Vector2;

// Projectile untuk serangan jarak jauh
public class Projectile {
    private Vector2 position;
    private Vector2 targetPosition;
    private Entity target;
    private float damage;
    private Team team;
    private float speed;
    private boolean active;
    private boolean isSplash;

    // Constructor untuk pooling
    public Projectile() {
        this.position = new Vector2();
        this.targetPosition = new Vector2();
        this.speed = 250f;
        this.active = false;
    }

    // Init projectile
    public void init(float x, float y, Entity target, float damage, Team team, boolean splash) {
        this.position.set(x, y);
        this.target = target;
        this.targetPosition.set(target.getPosition());
        this.damage = damage;
        this.team = team;
        this.active = true;
        this.isSplash = splash;
    }
    
    // Legacy constructor
    public Projectile(float x, float y, Entity target, float damage, Team team, boolean splash) {
        this();
        init(x, y, target, damage, team, splash);
    }
    
    /**
     * Reset projectile for pool reuse
     */
    public void reset() {
        this.position.set(0, 0);
        this.targetPosition.set(0, 0);
        this.target = null;
        this.damage = 0;
        this.team = null;
        this.active = false;
        this.isSplash = false;
    }

    public void update(float delta) {
        if (!active) return;

        // Update target position if target still alive
        if (!target.isDead()) {
            targetPosition.set(target.getPosition());
        }

        float dx = targetPosition.x - position.x;
        float dy = targetPosition.y - position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < 10) {
            active = false;
            if (target != null && !target.isDead()) {
                target.takeDamage(damage);
            }
        } else {
            float angle = (float) Math.atan2(dy, dx);
            position.x += Math.cos(angle) * speed * delta;
            position.y += Math.sin(angle) * speed * delta;
        }
    }

    // Getters
    public Vector2 getPosition() { return position; }
    public boolean isActive() { return active; }
    public boolean isSplash() { return isSplash; }
    public Team getTeam() { return team; }
}

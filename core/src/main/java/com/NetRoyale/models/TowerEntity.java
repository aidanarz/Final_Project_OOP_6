package com.NetRoyale.models;

import com.NetRoyale.patterns.pool.ProjectilePool;

// Tower dengan animasi sprite
public class TowerEntity extends Entity {
    private ProjectilePool projectilePool;
    private boolean isKing;
    private float stateTime;

    public TowerEntity(String key, UnitData data, Team team, float x, float y, boolean isKing, ProjectilePool projectilePool) {
        super(key, data, team, x, y);
        this.projectilePool = projectilePool;
        this.isKing = isKing;
        this.stateTime = 0;
    }
    
    @Override
    public void update(float delta, java.util.List<Entity> enemies) {
        super.update(delta, enemies);
        stateTime += delta;
    }

    @Override
    public void performAttack() {
        // Tower tembak projectile dari pool
        if (target != null && !target.isDead() && projectilePool != null) {
            Projectile projectile = projectilePool.obtain();
            projectile.init(position.x, position.y, target, data.getDamage(), team, false);
        }
    }

    @Override
    public void moveTowardsTarget(float delta) {
        // Tower tidak bergerak
    }

    @Override
    public boolean isKing() {
        return isKing;
    }
    
    public float getStateTime() {
        return stateTime;
    }
}

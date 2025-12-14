package com.NetRoyale.models;

import com.NetRoyale.patterns.pool.ProjectilePool;

// Entity untuk unit jarak jauh
public class RangedEntity extends Entity {
    private ProjectilePool projectilePool;
    private boolean isSplash;

    public RangedEntity(String key, UnitData data, Team team, float x, float y, ProjectilePool projectilePool, boolean isSplash) {
        super(key, data, team, x, y);
        this.projectilePool = projectilePool;
        this.isSplash = isSplash;
    }

    @Override
    public void performAttack() {
        // Ambil projectile dari pool
        if (target != null && !target.isDead() && projectilePool != null) {
            Projectile projectile = projectilePool.obtain();
            projectile.init(position.x, position.y, target, data.getDamage(), team, isSplash);
        }
    }
}

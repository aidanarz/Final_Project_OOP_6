package com.NetRoyale.patterns.pool;

import com.NetRoyale.models.Projectile;
import com.badlogic.gdx.utils.Array;

// Pool untuk reuse projectile
public class ProjectilePool {
    private Array<Projectile> activeProjectiles;
    private Array<Projectile> pool;
    private static final int INITIAL_POOL_SIZE = 50;
    
    public ProjectilePool() {
        activeProjectiles = new Array<>();
        pool = new Array<>();
        
        // Pre-populate pool
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            pool.add(new Projectile());
        }
    }
    
    // Ambil projectile dari pool
    public Projectile obtain() {
        Projectile projectile;
        if (pool.size > 0) {
            projectile = pool.pop();
        } else {
            projectile = new Projectile();
        }
        activeProjectiles.add(projectile);
        return projectile;
    }
    
    // Kembalikan projectile ke pool
    public void free(Projectile projectile) {
        activeProjectiles.removeValue(projectile, true);
        projectile.reset();
        pool.add(projectile);
    }
    
    // Cleanup projectile inactive
    public void cleanupInactive() {
        for (int i = activeProjectiles.size - 1; i >= 0; i--) {
            Projectile p = activeProjectiles.get(i);
            if (!p.isActive()) {
                free(p);
            }
        }
    }
    
    public void clear() {
        for (Projectile p : activeProjectiles) {
            pool.add(p);
        }
        activeProjectiles.clear();
    }
    
    public Array<Projectile> getActiveProjectiles() { return activeProjectiles; }
    public int getPoolSize() { return pool.size; }
    public int getActiveCount() { return activeProjectiles.size; }
}

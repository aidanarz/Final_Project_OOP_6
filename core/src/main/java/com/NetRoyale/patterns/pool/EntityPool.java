package com.NetRoyale.patterns.pool;

import com.NetRoyale.models.Entity;
import com.NetRoyale.models.Team;
import com.NetRoyale.models.UnitData;
import com.NetRoyale.patterns.factory.EntityFactory;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;

// Pool untuk reuse entity
public class EntityPool {
    private Array<Entity> activeEntities;
    private Map<String, Array<Entity>> poolByType;
    private ProjectilePool projectilePool;
    private static final int INITIAL_POOL_SIZE_PER_TYPE = 5;
    
    public EntityPool(ProjectilePool projectilePool) {
        this.activeEntities = new Array<>();
        this.poolByType = new HashMap<>();
        this.projectilePool = projectilePool;
    }
    
    private String getPoolKey(String unitKey, Team team) {
        return unitKey + "_" + team.name();
    }
    
    private void initializePool(String poolKey, String unitKey, UnitData data, Team team) {
        Array<Entity> pool = new Array<>();
        
        // Pre-populate pool with entities
        for (int i = 0; i < INITIAL_POOL_SIZE_PER_TYPE; i++) {
            Entity entity = EntityFactory.createEntity(unitKey, data, team, 0, 0, projectilePool);
            entity.takeDamage(entity.getMaxHp()); // Mark as dead initially
            pool.add(entity);
        }
        
        poolByType.put(poolKey, pool);
    }
    
    // Ambil entity dari pool
    public Entity obtain(String unitKey, UnitData data, Team team, float x, float y) {
        String poolKey = getPoolKey(unitKey, team);
        
        // Initialize pool if not exists
        if (!poolByType.containsKey(poolKey)) {
            initializePool(poolKey, unitKey, data, team);
        }
        
        Array<Entity> pool = poolByType.get(poolKey);
        Entity entity;
        
        // Try to reuse from pool
        if (pool.size > 0) {
            entity = pool.pop();
            entity.reset(x, y, data);
        } else {
            // Create new entity if pool is empty
            entity = EntityFactory.createEntity(unitKey, data, team, x, y, projectilePool);
        }
        
        activeEntities.add(entity);
        return entity;
    }
    
    /**
     * Return entity to pool when dead
     */
    public void free(Entity entity) {
        if (!activeEntities.removeValue(entity, true)) {
            return; // Entity not in active list
        }
        
        String poolKey = getPoolKey(entity.getKey(), entity.getTeam());
        Array<Entity> pool = poolByType.get(poolKey);
        
        if (pool == null) {
            pool = new Array<>();
            poolByType.put(poolKey, pool);
        }
        
        // Reset entity state before returning to pool
        entity.takeDamage(entity.getMaxHp()); // Ensure it's marked as dead
        pool.add(entity);
    }
    
    // Cleanup entity yang mati
    public void cleanupDeadEntities() {
        for (int i = activeEntities.size - 1; i >= 0; i--) {
            Entity entity = activeEntities.get(i);
            if (entity.isDead()) {
                free(entity);
            }
        }
    }
    
    public void clear() {
        for (Entity entity : activeEntities) {
            String poolKey = getPoolKey(entity.getKey(), entity.getTeam());
            Array<Entity> pool = poolByType.get(poolKey);
            if (pool != null) {
                pool.add(entity);
            }
        }
        activeEntities.clear();
    }
    
    // Reset semua pool
    public void reset() {
        activeEntities.clear();
        poolByType.clear();
    }
    
    public Array<Entity> getActiveEntities() { return activeEntities; }
    
    public int getActiveCount() { return activeEntities.size; }
    
    public int getPoolSize(String unitKey, Team team) {
        String poolKey = getPoolKey(unitKey, team);
        Array<Entity> pool = poolByType.get(poolKey);
        return pool != null ? pool.size : 0;
    }
    
    public int getTotalPoolSize() {
        int total = 0;
        for (Array<Entity> pool : poolByType.values()) {
            total += pool.size;
        }
        return total;
    }
}

package com.NetRoyale.patterns.command;

import com.NetRoyale.models.Entity;
import com.NetRoyale.models.UnitData;
import com.NetRoyale.models.Team;
import com.NetRoyale.patterns.singleton.GameManager;
import com.NetRoyale.patterns.pool.EntityPool;
import com.badlogic.gdx.utils.Array;

/**
 * Command Pattern - SpawnCommand
 * 
 * Purpose: Encapsulate spawn unit action dengan undo capability
 * - Execute: Spawn unit dari EntityPool dan kurangi elixir
 * - Undo: Remove unit dan refund elixir, kembalikan ke pool
 * - Terintegrasi dengan Object Pool Pattern untuk reuse entities
 */
public class SpawnCommand implements Command {
    private String unitKey;
    private UnitData unitData;
    private float x;
    private float y;
    private Team team;
    private Entity spawnedEntity;
    private Array<Entity> entityList;
    private EntityPool entityPool;
    
    public SpawnCommand(String unitKey, UnitData unitData, float x, float y, Team team, 
                       Array<Entity> entityList, EntityPool entityPool) {
        this.unitKey = unitKey;
        this.unitData = unitData;
        this.x = x;
        this.y = y;
        this.team = team;
        this.entityList = entityList;
        this.entityPool = entityPool;
    }
    
    @Override
    public void execute() {
        GameManager gm = GameManager.getInstance();
        
        // Check elixir
        boolean isPlayer = (team == Team.PLAYER);
        if (!gm.canAfford(unitData.getCost(), isPlayer)) {
            return;
        }
        
        // Spend elixir
        gm.spendElixir(unitData.getCost(), isPlayer);
        
        // Obtain entity from pool (Object Pool Pattern)
        spawnedEntity = entityPool.obtain(unitKey, unitData, team, x, y);
        entityList.add(spawnedEntity);
    }
    
    @Override
    public void undo() {
        if (spawnedEntity != null && entityList.contains(spawnedEntity, true)) {
            // Remove from game
            entityList.removeValue(spawnedEntity, true);
            
            // Refund elixir
            boolean isPlayer = (team == Team.PLAYER);
            GameManager.getInstance().refundElixir(unitData.getCost(), isPlayer);
            
            // Return entity to pool
            entityPool.free(spawnedEntity);
            
            spawnedEntity = null;
        }
    }
    
    public Entity getSpawnedEntity() {
        return spawnedEntity;
    }
}

package com.NetRoyale.patterns;

import com.NetRoyale.models.Entity;
import java.util.List;

/**
 * Concrete Strategy - Random AI spawning
 * Integrates with Level Strategy Pattern for difficulty scaling
 * 
 * NOTE: This class is now DEPRECATED.
 * Enemy spawning logic has been moved to GameFacade.spawnEnemyUnit()
 * for better integration with Command Pattern and Object Pool Pattern.
 * 
 * Kept for reference only - not used in GameScreenV3.
 */
public class RandomAIStrategy implements AIStrategy {

    public RandomAIStrategy() {
    }

    @Override
    public void execute(float elixir, List<Entity> playerEntities, List<Entity> enemyEntities) {
        // DEPRECATED: This method is no longer used.
        // Enemy spawning is now handled by GameFacade.spawnEnemyUnit()
        // which integrates with Command Pattern and Object Pool Pattern.
        
        // Original implementation kept for reference:
        /*
        if (elixir < 3) return;

        UnitFactory factory = UnitFactory.getInstance();
        String[] allKeys = factory.getAllUnits().keySet().toArray(new String[0]);
        String pick = allKeys[random.nextInt(allKeys.length)];
        UnitData unitData = factory.getUnit(pick);
        
        LevelStrategy levelStrategy = LevelManager.getInstance().getCurrentStrategy();
        unitData = levelStrategy.applyDifficulty(unitData);

        if (elixir >= unitData.getCost()) {
            float[] lanes = {100f, GAME_HEIGHT / 2, GAME_HEIGHT - 100f};
            float y = lanes[random.nextInt(3)] + (random.nextFloat() * 40 - 20);
            float x = GAME_WIDTH - 120 - (random.nextFloat() * 50);

            // Would spawn here, but now handled by GameFacade
        }
        */
    }
}

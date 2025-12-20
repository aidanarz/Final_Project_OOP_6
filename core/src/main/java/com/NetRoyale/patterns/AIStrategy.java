package com.NetRoyale.patterns;

import com.NetRoyale.models.Entity;
import java.util.List;

/**
 * Strategy Pattern - Different AI behaviors
 */
public interface AIStrategy {
    void execute(float elixir, List<Entity> playerEntities, List<Entity> enemyEntities);
}

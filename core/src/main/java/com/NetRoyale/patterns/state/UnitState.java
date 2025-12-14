package com.NetRoyale.patterns.state;

import com.NetRoyale.models.Entity;
import java.util.List;

// State interface untuk behavior unit
public interface UnitState {
    void execute(Entity entity, float delta, List<Entity> enemies);
    String getStateName();
}

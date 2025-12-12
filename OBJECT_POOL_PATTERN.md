# Object Pool Pattern Implementation

## Overview
Project ini mengimplementasikan **Object Pool Pattern** untuk dua jenis object utama:
1. **Projectile** (Peluru/Proyektil)
2. **Entity** (Troops/Cards/Units)

Pattern ini digunakan untuk mengurangi memory allocation dan garbage collection overhead dengan cara **reuse** objects yang sudah tidak digunakan.

---

## 1. ProjectilePool (Projectile Object Pool)

### Lokasi
`com.NetRoyale.patterns.pool.ProjectilePool`

### Purpose
Mengelola pool untuk peluru/proyektil yang ditembakkan oleh ranged units.

### Cara Kerja
1. **Pre-populate Pool**: Saat initialization, pool dibuat dengan 50 projectile
2. **Obtain**: Saat ranged unit menembak, ambil projectile dari pool (atau buat baru jika pool kosong)
3. **Free**: Saat projectile mengenai target atau keluar arena, kembalikan ke pool
4. **Cleanup**: Secara periodik cleanup projectile yang inactive

### Integrasi
- Digunakan oleh: `RangedEntity`, `TowerEntity`
- Dikelola oleh: `GameFacade`

```java
// Example usage di RangedEntity
Projectile proj = projectilePool.obtain();
proj.initialize(position.x, position.y, target, data.getDamage(), isSplash);
```

---

## 2. EntityPool (Entity/Troops Object Pool)

### Lokasi
`com.NetRoyale.patterns.pool.EntityPool`

### Purpose
Mengelola pool untuk semua troops/cards/units yang di-spawn dalam game.

### Cara Kerja
1. **Pool per Type**: Setiap unit type (knight, archer, dll) dan team memiliki pool sendiri
2. **Pre-populate**: Saat pertama kali unit type di-spawn, buat pool dengan 5 entities
3. **Obtain**: Saat spawn unit via SpawnCommand, ambil dari pool (atau create new jika kosong)
4. **Free**: Saat entity mati, kembalikan ke pool untuk reuse
5. **Reset**: Entity di-reset state-nya (HP, position, target, dll) sebelum reuse
6. **Cleanup**: Secara periodik cleanup entities yang mati

### Pool Key Format
```
{unitKey}_{teamName}
```
Contoh: `knight_PLAYER`, `archer_ENEMY`

### Integrasi dengan Design Patterns Lain

#### Command Pattern (SpawnCommand)
```java
// SpawnCommand.execute()
spawnedEntity = entityPool.obtain(unitKey, unitData, team, x, y);
entityList.add(spawnedEntity);

// SpawnCommand.undo()
entityList.removeValue(spawnedEntity, true);
entityPool.free(spawnedEntity); // Return to pool
```

#### Facade Pattern (GameFacade)
```java
public GameFacade() {
    this.projectilePool = new ProjectilePool();
    this.entityPool = new EntityPool(projectilePool); // EntityPool needs ProjectilePool
}

public void update(float delta) {
    // Cleanup inactive projectiles
    projectilePool.cleanupInactive();
    
    // Cleanup dead entities and return to pool
    entityPool.cleanupDeadEntities();
}
```

#### Factory Pattern (EntityFactory)
EntityPool menggunakan EntityFactory untuk create entities:
```java
entity = EntityFactory.createEntity(unitKey, data, team, x, y, projectilePool);
```

---

## Entity Reset Flow

Saat entity dikembalikan ke pool dan di-obtain lagi:

### Entity.reset(x, y, newData)
```java
public void reset(float x, float y, UnitData newData) {
    this.data = newData;
    this.position.set(x, y);
    this.hp = newData.getHp();        // Full HP
    this.maxHp = newData.getHp();
    this.attackTimer = 0;
    this.isDead = false;              // Revive
    this.target = null;               // Clear target
    this.currentState = WalkingState.getInstance(); // Reset state
}
```

### Lifecycle
1. **First Spawn**: Entity created via Factory
2. **Death**: `isDead = true`, HP = 0
3. **Cleanup**: Removed from active list via `cleanupDeadEntities()`
4. **Free**: Returned to pool via `entityPool.free(entity)`
5. **Obtain**: Retrieved from pool via `entityPool.obtain()`
6. **Reset**: State reset to default values
7. **Re-spawn**: Back in game with full HP and fresh state

---

## Benefits

### Memory Efficiency
- Mengurangi allocations dengan reuse objects
- Menurunkan garbage collection overhead
- Pre-populated pool untuk predictable performance

### Performance
- Fast obtain/free operations
- No hiccups dari GC pauses
- Cleanup dilakukan secara batch

### Code Organization
- Centralized pool management
- Clean separation of concerns
- Easy to debug dan monitor (getPoolSize, getActiveCount)

---

## Monitoring Pool Status

### EntityPool
```java
entityPool.getActiveCount()                    // Total active entities
entityPool.getPoolSize("knight", Team.PLAYER) // Pool size for specific type
entityPool.getTotalPoolSize()                 // Total entities in all pools
```

### ProjectilePool
```java
projectilePool.getActiveCount()  // Active projectiles
projectilePool.getPoolSize()     // Projectiles in pool
```

---

## Summary

| Object Type | Pool Class | Managed By | Cleanup Method |
|------------|-----------|-----------|----------------|
| Projectile | ProjectilePool | GameFacade | `cleanupInactive()` |
| Entity | EntityPool | GameFacade | `cleanupDeadEntities()` |

Kedua pool ini bekerja sama untuk memberikan efficient memory management untuk semua game objects yang spawned secara dynamic.

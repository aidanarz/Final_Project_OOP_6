# Level System with Strategy Pattern

## Overview
Kingdom Clash kini memiliki sistem level dengan 5 tingkat kesulitan yang berbeda. Sistem ini diimplementasikan menggunakan **Strategy Pattern** untuk memberikan flexibility dalam mengatur difficulty scaling.

---

## Design Pattern: Strategy Pattern

### Purpose
- Define keluarga algoritma (level strategies) yang dapat di-interchange
- Encapsulate setiap algoritma dalam class terpisah
- Memungkinkan algoritma berubah secara independent dari client yang menggunakannya

### Structure

```
LevelStrategy (Interface)
├── Level1Strategy (Easy - 1.0x damage)
├── Level2Strategy (Normal - 1.3x damage)
├── Level3Strategy (Hard - 1.6x damage)
├── Level4Strategy (Expert - 2.0x damage)
└── Level5Strategy (Master - 2.5x damage)
```

---

## Level Details

### Level 1: Beginner 🟢
- **Damage Multiplier**: 1.0x (Normal)
- **Description**: Musuh memiliki damage standard
- **Color**: Green

### Level 2: Normal 🟡
- **Damage Multiplier**: 1.3x
- **Description**: Musuh 30% lebih kuat
- **Color**: Yellow

### Level 3: Hard 🟠
- **Damage Multiplier**: 1.6x
- **Description**: Musuh 60% lebih kuat
- **Color**: Orange

### Level 4: Expert 🔴
- **Damage Multiplier**: 2.0x
- **Description**: Musuh 2x lebih kuat
- **Color**: Red

### Level 5: MASTER 🟣
- **Damage Multiplier**: 2.5x
- **Description**: Musuh 2.5x lebih kuat - Final Boss!
- **Color**: Purple

---

## Level Progression System

### Flow Chart
```
Start Game
    ↓
Level 1 🟢
    ↓
Win? → Yes → Level 2 🟡
    ↓ No
Retry Level 1
    ↓
Level 2 🟡
    ↓
Win? → Yes → Level 3 🟠
    ↓ No
Retry Level 2
    ↓
... (continue pattern)
    ↓
Level 5 🟣
    ↓
Win? → Yes → GAME COMPLETE! 🎉
    ↓ No
Retry Level 5
```

### Rules
1. **Win**: Advance to next level (if not at max)
2. **Lose**: Retry same level
3. **Complete All 5**: Display "KAMU MENANG!" message
4. **Progress Saved**: maxLevelReached tracks highest level unlocked

---

## Implementation Details

### 1. LevelStrategy Interface
```java
public interface LevelStrategy {
    int getLevelNumber();
    String getLevelName();
    float getDamageMultiplier();
    UnitData applyDifficulty(UnitData originalData);
    String getLevelColor();
}
```

### 2. LevelManager (Singleton)
```java
LevelManager levelManager = LevelManager.getInstance();

// Get current strategy
LevelStrategy strategy = levelManager.getCurrentStrategy();

// Apply difficulty to enemy units
UnitData modifiedData = strategy.applyDifficulty(originalData);

// Handle victory
levelManager.onLevelComplete(); // Advance to next level

// Handle defeat
levelManager.onLevelFailed(); // Stay on same level

// Check completion
boolean allDone = levelManager.hasCompletedAllLevels();
```

### 3. Integration with AI Strategy
```java
// In RandomAIStrategy.execute()
UnitData unitData = factory.getUnit(pick);

// Apply level difficulty (Strategy Pattern)
LevelStrategy levelStrategy = LevelManager.getInstance().getCurrentStrategy();
unitData = levelStrategy.applyDifficulty(unitData);

// Spawn enemy with modified stats
Entity enemy = new Entity(pick, unitData, Team.ENEMY, x, y);
```

### 4. Game Flow
```java
// DeckBuilderScreenV2 - Show current level
String levelInfo = levelManager.getLevelInfo();
font.draw(batch, levelInfo, x, y);

// GameScreenV3 - Display level during battle
font.draw(batch, levelManager.getLevelInfo(), x, y);

// GameOverScreenV2 - Handle progression
if (victory) {
    levelManager.onLevelComplete();
    if (levelManager.hasCompletedAllLevels()) {
        // Show "KAMU MENANG!" screen
    } else {
        // Show "Next Level" info
    }
} else {
    levelManager.onLevelFailed();
    // Show "Retry" info
}
```

---

## UI/UX Features

### Deck Builder Screen
- Displays current level at top center
- Shows emoji indicator (🟢🟡🟠🔴🟣)
- Displays "Enemy Damage: X%" info in red

### Game Screen
- Level info displayed at top center during battle
- Shows current difficulty level with emoji

### Game Over Screen

#### Victory (Not Final Level)
```
VICTORY!
[Result message]
Next: [Level emoji] Level X: [Name]
[LANJUT button]
```

#### Victory (Final Level)
```
GAME COMPLETE!
🎉 KAMU MENANG! 🎉
Semua 5 level berhasil diselesaikan!
[MAIN LAGI button]
```

#### Defeat
```
DEFEAT
[Result message]
Retry: [Level emoji] Level X: [Name]
[RETRY button]
```

---

## Example Scenario

### Player Journey:
1. Start → Level 1 🟢 (1.0x damage)
2. Win → Level 2 🟡 (1.3x damage)
3. **Lose** → Retry Level 2 🟡
4. Win → Level 3 🟠 (1.6x damage)
5. Win → Level 4 🔴 (2.0x damage)
6. **Lose** → Retry Level 4 🔴
7. Win → Level 5 🟣 (2.5x damage)
8. Win → **GAME COMPLETE!** 🎉

---

## Technical Benefits

### Strategy Pattern Advantages
✅ **Easy to add new levels**: Just create new LevelXStrategy class
✅ **Clean separation**: Each level has its own strategy
✅ **Runtime switching**: Change difficulty on-the-fly
✅ **Testable**: Each strategy can be tested independently
✅ **Maintainable**: Modify one level without affecting others

### Code Example - Adding Level 6
```java
public class Level6Strategy implements LevelStrategy {
    public int getLevelNumber() { return 6; }
    public String getLevelName() { return "Level 6: Nightmare"; }
    public float getDamageMultiplier() { return 3.0f; }
    // ... implement other methods
}

// In LevelManager
private static final int MAX_LEVEL = 6;

private LevelStrategy getLevelStrategy(int level) {
    // ... existing cases
    case 6: return new Level6Strategy();
}
```

---

## Summary

| Component | Pattern | Responsibility |
|-----------|---------|----------------|
| LevelStrategy | Strategy | Define level difficulty behavior |
| Level1-5Strategy | Concrete Strategy | Implement specific difficulty scaling |
| LevelManager | Singleton + Context | Manage level progression & current strategy |
| RandomAIStrategy | Integration | Apply difficulty to enemy units |
| GameOverScreenV2 | UI | Handle win/loss and level progression |
| DeckBuilderScreenV2 | UI | Display current level info |
| GameScreenV3 | UI | Show level during gameplay |

Sistem level ini memberikan progressive difficulty yang fair dan challenge yang meningkat seiring player advance melalui 5 level yang ada!

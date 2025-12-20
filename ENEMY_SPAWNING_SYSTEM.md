# Enemy Spawning System dengan Level Strategy Pattern

## Perbaikan yang Dilakukan

### 1. Perbaikan Package dan Import (4 Files)
Semua file di folder `patterns/command` dan `patterns/facade` telah diperbaiki:

- ✅ **Command.java** - Package: `com.NetRoyale.patterns.command`
- ✅ **CommandHistory.java** - Package: `com.NetRoyale.patterns.command`
- ✅ **SpawnCommand.java** - Package: `com.NetRoyale.patterns.command` + imports
- ✅ **GameFacade.java** - Package: `com.NetRoyale.patterns.facade` + imports

File AI Strategy juga diperbaiki:
- ✅ **AIStrategy.java** - Package: `com.NetRoyale.patterns`
- ✅ **RandomAIStrategy.java** - Package: `com.NetRoyale.patterns` + imports

### 2. Enemy Spawning System

#### Sistem Spawning Otomatis
GameFacade sekarang memiliki sistem spawning musuh otomatis yang:

1. **Timer-based Spawning**
   - Musuh akan spawn secara otomatis berdasarkan interval waktu
   - Interval spawn berbeda per level (lebih cepat di level tinggi)

2. **Level-based Difficulty** (Strategy Pattern)
   - Level 1: Spawn setiap 8 detik - Musuh damage 1.0x (Beginner) 🟢
   - Level 2: Spawn setiap 6.5 detik - Musuh damage 1.3x (Easy) 🔵
   - Level 3: Spawn setiap 5 detik - Musuh damage 1.6x (Normal) 🟡
   - Level 4: Spawn setiap 3.5 detik - Musuh damage 2.0x (Hard) 🟠
   - Level 5: Spawn setiap 2.5 detik - Musuh damage 2.5x (Master) 🟣

3. **Random Unit Selection**
   - AI memilih unit secara random dari semua unit yang tersedia
   - Unit yang dipilih akan mendapat buff damage sesuai level

4. **Smart Spawning**
   - Hanya spawn jika AI punya cukup elixir (minimal 3)
   - Spawn di 3 lane berbeda (atas, tengah, bawah)
   - Posisi spawn di sisi kanan (enemy side)

## Cara Kerja

### Flow Spawning Enemy:

```
Game Start → GameFacade.initializeGame()
    ↓
Every Frame → GameFacade.update(delta)
    ↓
updateAISpawning(delta)
    ↓
aiSpawnTimer >= spawnInterval? 
    ↓ YES
spawnEnemyUnit()
    ↓
1. Get random unit from UnitFactory
2. Apply LevelStrategy.applyDifficulty() (buff damage)
3. Check if AI has enough elixir
4. Execute SpawnCommand → Spawn enemy unit
5. AI elixir dikurangi otomatis
```

### Integration dengan Design Patterns:

- **Strategy Pattern**: `LevelStrategy` menentukan difficulty multiplier
- **Command Pattern**: `SpawnCommand` untuk spawn dengan undo capability
- **Singleton Pattern**: `LevelManager` untuk manage current level
- **Factory Pattern**: `UnitFactory` untuk create unit data
- **Object Pool Pattern**: `EntityPool` untuk reuse entities
- **Facade Pattern**: `GameFacade` sebagai interface tunggal

## Testing

Untuk test enemy spawning:

1. Jalankan game (GameScreenV3)
2. Tunggu beberapa detik
3. Musuh akan mulai spawn dari sisi kanan
4. Check level info di top-left untuk melihat damage multiplier
5. Semakin tinggi level, semakin cepat musuh spawn dan semakin kuat damage

## Notes

- Enemy spawning menggunakan AI elixir yang regenerate otomatis
- Spawning interval dapat diubah di method `getAISpawnInterval()`
- Damage multiplier dapat diubah di file `Level[1-5]Strategy.java`
- Enemy spawn menggunakan Command Pattern tapi tidak masuk commandHistory (karena AI action, bukan player action)

---

## Status Implementasi ✅

### File yang Sudah Diperbaiki:
1. ✅ `Command.java` (patterns/command) - Package fixed
2. ✅ `CommandHistory.java` (patterns/command) - Package fixed  
3. ✅ `SpawnCommand.java` (patterns/command) - Package & imports fixed
4. ✅ `GameFacade.java` (patterns/facade) - Package, imports & enemy spawning implemented
5. ✅ `AIStrategy.java` - Package fixed
6. ✅ `RandomAIStrategy.java` - Deprecated (logic moved to GameFacade)

### Enemy Spawning Features:
- ✅ Auto-spawn enemy units every X seconds (based on level)
- ✅ LevelStrategy integration (damage multiplier per level)
- ✅ Random unit selection
- ✅ Smart spawning (check elixir, spawn in lanes)
- ✅ Command Pattern integration
- ✅ Object Pool Pattern integration

### Compile Status:
- ✅ No compilation errors
- ✅ All imports correct
- ✅ Ready to run

**Game sekarang sudah siap! Musuh akan spawn otomatis dari level 1 sampai level 5 dengan kesulitan yang meningkat.**

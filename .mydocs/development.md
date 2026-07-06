# Combat Level Calculator — Development Guide

## Project Setup

### Prerequisites
- Java 17+ (the project targets Java 11 but builds fine with 17)
- Gradle 8.10+ (included via gradlew)
- RuneLite client source (API is available via Maven)

### Build & Run

```bash
# Clean build and run tests
.\gradlew clean test

# Run the plugin in the development client
.\gradlew run

# Note: For plugin hub submission, use `build=standard` in runelite-plugin.properties
# No shadowJar needed - the build.gradle is replaced during hub review
```

## Directory Structure

```
combat-level-calculator-master/
├── src/
│   ├── main/java/com/combatlevelcalc/  (Plugin source code)
│   └── test/java/com/combatlevelcalc/  (Unit tests)
├── build.gradle                          (Build config)
├── settings.gradle                       (Project name/group)
├── .mydocs/                              (Documentation)
│   ├── architecture.md                   (This file's sibling)
│   ├── plan.md                           (Original design plan)
│   └── ...
├── runelite-plugin.properties            (Plugin metadata)
└── ...
```

## Code Organization

### Main Package: `com.combatlevelcalc`

| File | Purpose | Key Methods |
|------|---------|-------------|
| `CombatLevelCalculatorPlugin.java` | Plugin lifecycle & toolbar | `startUp()`, `shutDown()`, `onGameStateChanged()` |
| `CombatLevelPanel.java` | UI (Swing) | `setStats()`, `refreshBuilds()`, `updateCombatLabel()` |
| `StatsModel.java` | Player stats data | Getters/setters for 7 combat stats |
| `BuildModel.java` | PvP build archetype | Store name, requirements, reachability, combat level |
| `CombatCalculator.java` | Combat formula | `computeCombatLevel(StatsModel)` |
| `BuildFactory.java` | Build definitions | `getBuilds()`, `evaluateBuilds()` |
| `CombatLevelCalculatorConfig.java` | Config interface | (Currently empty) |

## Testing

### Run Tests
```bash
.\gradlew test
```

### Test Coverage
- **CombatCalculatorUnitTest**: Verifies combat level formula
- **BuildFactoryUnitTest**: Validates build reachability logic

### Adding New Tests

Example: Testing a new stat combination

```java
@Test
public void testNewBuild() {
    StatsModel stats = new StatsModel(40, 40, 40, 40, 40, 40, 40);
    int combatLevel = CombatCalculator.computeCombatLevel(stats);
    assertEquals(expectedValue, combatLevel);
}
```

## Extending the Plugin

### 1. Add a New PvP Build

Edit `BuildFactory.getBuilds()`:

```java
builds.add(new BuildModel(
    "Hybrid",
    Map.of(
        "Attack", 60,
        "Strength", 60,
        "Ranged", 60,
        "Magic", 60
    ),
    Map.of(
        "Defence", 40,
        "Prayer", 1
    )
));
```

Then run `.\gradlew test` to ensure no regressions.

### 2. Add a Config Option

1. Add to `CombatLevelCalculatorConfig.java`:
```java
@ConfigItem(
    keyName = "hideUnattainable",
    name = "Hide Unattainable Builds",
    description = "Hide builds you cannot reach"
)
default boolean hideUnattainable() {
    return false;
}
```

2. Inject config in `CombatLevelCalculatorPlugin.java`:
```java
@Inject
private CombatLevelCalculatorConfig config;
```

3. Use config in `CombatLevelPanel.refreshBuilds()`:
```java
if (config.hideUnattainable() && !build.isReachable()) {
    continue;
}
```

### 3. Change the Combat Formula

Edit `CombatCalculator.computeCombatLevel()`. The current formula is RuneScape's standard:

```
base = 0.25 × (Defence + Hitpoints + ⌊Prayer/2⌋)
melee = 0.325 × (Attack + Strength)
range = 0.325 × ⌊Ranged × 1.5⌋
mage = 0.325 × ⌊Magic × 1.5⌋
combat = ⌊base + max(melee, range, mage)⌋
```

**Note**: RuneLite guidelines permit PvM/PvP balance changes; only combat *prediction* (next attack, target, timing) is forbidden for bosses.

### 4. Load Builds from External Source

Currently, builds are hardcoded in `BuildFactory`. To load from JSON/external file:

1. Add a `BuildLoader` utility class that parses JSON
2. Modify `BuildFactory.getBuilds()` to call the loader
3. Store build definitions in `src/main/resources/builds.json` or fetch from a file

Example structure:
```json
{
  "builds": [
    {
      "name": "1 Defence Pure",
      "minimumStats": { "Attack": 70, "Strength": 70, "Hitpoints": 52 },
      "maximumStats": { "Defence": 1, "Ranged": 1, "Prayer": 1, "Magic": 1 }
    }
  ]
}
```

## Common Issues

### Build Fails: "Cannot resolve symbol..."
- Run `.\gradlew clean` to clear Gradle cache
- Check that `.vscode/settings.json` points to the correct JDK (Java 17)
- Reload VS Code workspace: Command Palette → Developer: Reload Window

### Tests Don't Run
```bash
.\gradlew clean test --refresh-dependencies
```

### Plugin Doesn't Load in Client
- Ensure `CombatLevelCalculatorPlugin` class exists and is not renamed
- Check `runelite-plugin.properties` `plugins=` entry points to correct class
- Verify build.gradle `pluginMainClass` is set to `com.combatlevelcalc.CombatLevelCalculatorTest`

## Code Style

- **Java 11 compatible** (no switch expressions, no text blocks, etc.)
- **No reflection** (RuneLite policy)
- **Lombok** for reducing boilerplate (optional; currently not used but could be added)
- **Slf4j** for logging (via `@Slf4j` annotation)
- **Immutable objects** preferred (e.g., `BuildModel` fields are `final`)

## Submitting to RuneLite Hub

1. Update version in `runelite-plugin.properties`:
   ```
   version=1.0.0
   ```

2. Ensure tests pass:
   ```bash
   .\gradlew clean test
   ```

3. Build JAR:
   ```bash
   .\gradlew shadowJar
   ```

4. Check code follows [RuneLite guidelines](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features):
   - ✅ No reflection, JNI, external processes
   - ✅ No combat prediction/automation
   - ✅ Follows config naming conventions
   - ✅ Uses injected Gson, OkHttp, Client API

5. Fork and create a PR to [runelite/plugins](https://github.com/runelite/plugins)

## Performance Notes

- **Combat calculation**: O(1), runs instantly
- **Build evaluation**: O(n×m) where n = number of builds (~5), m = average stats per build (~7) → negligible
- **UI refresh**: Runs on EDT; smooth for < 100 builds
- **Stat fetching**: Light; uses `client.getRealSkillLevel()` (native API, no blocking)

## Future Improvements

1. **Externalize build definitions** to JSON/API
2. **Add config options** for filtering, sorting, export
3. **Cache build templates** in `BuildFactory`
4. **Responsive UI** improvements (collapsible sections, search)
5. **Stat snapshots**: Save/load/compare multiple stat configurations
6. **Exp calculations**: Estimate XP needed for each build
7. **Account type badges**: Show build compatibility at a glance

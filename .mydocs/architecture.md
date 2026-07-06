# Combat Level Calculator Plugin — Architecture & Design

## Overview
The Combat Level Calculator is a RuneLite plugin that allows players to preview their combat level based on custom stat inputs and discover which PvP account builds (e.g., Pure, Zerker, Tank) are attainable from their current stats.

## Architecture

### Package Structure
```
com.combatlevelcalc/
├── CombatLevelCalculatorPlugin.java    (Main plugin entry point)
├── CombatLevelCalculatorConfig.java    (Config interface, empty by design)
├── CombatLevelPanel.java               (UI component)
├── StatsModel.java                     (Data model for player stats)
├── BuildModel.java                     (Data model for PvP builds)
├── CombatCalculator.java               (Combat level formula)
└── BuildFactory.java                   (Build definitions and evaluation)
```

### Key Classes

#### 1. **CombatLevelCalculatorPlugin** (Plugin Lifecycle)
- **Role**: Manages plugin lifecycle (startup, shutdown), client toolbar integration
- **Responsibilities**:
  - Register/unregister the UI panel on the client toolbar
  - Fetch player stats from the RuneLite client when logged in
  - Respond to game state changes (login) to refresh stats
- **Dependencies**: RuneLite Client API, ClientToolbar, Config
- **Thread Safety**: Uses `clientThread` implicitly via Client API; stat fetching is synchronous

#### 2. **CombatLevelPanel** (UI Layer)
- **Role**: Displays and manages the user interface
- **Responsibilities**:
- Render stat input fields (Attack, Strength, Defence, Hitpoints, Ranged, Prayer, Magic)
- Live-update combat level as user edits inputs
- Display reachable and unattainable PvP build lists with modern dark-theme styling
- Provide "Fetch stats" button to load account stats from client
- Toggle visibility of unattainable builds
- **Design Pattern**: Extends `PluginPanel` from RuneLite
- **Event Handling**: DocumentListener for live stat input changes; ActionListener for fetch button
- **Visual Design**: Dark theme colors with accent bars; left accent stripe indicates build status; hover effects on cards

#### 3. **StatsModel** (Data Transfer Object)
- **Role**: Encapsulates player stats
- **Fields**: attack, strength, defence, hitpoints, ranged, prayer, magic (all int)
- **Note**: Simple POJO with getters/setters; could be simplified with Lombok in future

#### 4. **CombatCalculator** (Business Logic)
- **Role**: Computes combat level from stats
- **Formula** (RuneScape standard):
  ```
  base = 0.25 × (Defence + Hitpoints + ⌊Prayer/2⌋)
  melee = 0.325 × (Attack + Strength)
  range = 0.325 × ⌊Ranged × 1.5⌋
  mage = 0.325 × ⌊Magic × 1.5⌋
  combat_level = ⌊base + max(melee, range, mage)⌋
  ```
- **Note**: Utility class with private constructor (no instantiation)

#### 5. **BuildModel** (Data Model for Builds)
- **Role**: Represents a PvP account archetype
- **Fields**:
  - `name`: Build name (e.g., "1 Defence Pure", "Zerker")
  - `minimumStats`: Required stat levels (Map<String, Integer>)
  - `maximumStats`: Hard caps on stats (e.g., Defence ≤ 1 for a pure)
  - `reachable`: Whether player can achieve this build
  - `delta`: Required stat increases to reach minimum stats
  - `blockedStats`: Stats exceeding maximum caps (prevents reachability)
  - `estimatedCombatLevel`: Combat level of the target build

#### 6. **BuildFactory** (Build Definitions & Evaluation)
- **Role**: Defines and evaluates PvP builds
- **Current Builds**:
- **Obby Mauler**: Strength 60, Hitpoints 49, Magic 42, Ranged 42, Attack/Defence/Prayer ≤ 1
- **50 Attack Pure**: Strength 99, Hitpoints 90, Magic 99, Ranged 99, Attack 50, Defence/Prayer ≤ 1
- **60 Attack Pure**: Strength 99, Hitpoints 90, Magic 99, Ranged 99, Attack 60, Defence 1, Prayer 31
- **75 Attack Pure**: Strength 99, Hitpoints 90, Magic 99, Ranged 99, Attack 75, Defence 1, Prayer 52
- **Zerker**: Attack 60, Strength 99, Hitpoints 93, Ranged 99, Magic 99, Defence 45, Prayer 31
- **Zerker Brid**: Same as Zerker
- **Tank**: Attack 50, Strength 93, Hitpoints 94, Ranged 95, Magic 95, Defence 70, Prayer 77
- **Evaluation Logic**:
  - Compares current stats against build minimums → calculates deltas
  - Compares current stats against build maximums → identifies blockers
  - Build is "reachable" if all minimums are met and no maximums are exceeded
  - Estimated combat level is calculated for the target build stats

#### 7. **CombatLevelCalculatorConfig** (Configuration)
- **Role**: Config interface for the plugin
- **Current State**: Empty (no user-configurable settings; can be extended in future)
- **Config Group**: `"combatlevelcalc"`

## Data Flow

```
Player logs in
    ↓
CombatLevelCalculatorPlugin.onGameStateChanged()
    ↓
fetchCurrentStats() via Client API
    ↓
StatsModel populated
    ↓
CombatLevelPanel.setStats(StatsModel)
    ↓
Input fields updated
    ↓
User edits stat input → DocumentListener triggers updateCombatLabel()
    ↓
CombatCalculator.computeCombatLevel(StatsModel)
    ↓
BuildFactory.evaluateBuilds(currentStats, templates)
    ↓
UI displays combat level + reachable/unattainable builds
```

## Threading Model

- **Plugin startup/shutdown**: Main plugin thread (RuneLite lifecycle)
- **Stat fetching**: Uses `Client.getRealSkillLevel()` (safe, runs on client thread)
- **UI updates**: Swing event dispatch thread (implicit via DocumentListener/ActionListener)
- **No explicit async**: Current implementation is synchronous; stat fetching is lightweight

## Testing

Two unit test classes validate core logic:

1. **CombatCalculatorUnitTest**: Verifies combat level formula with known stat vectors
2. **BuildFactoryUnitTest**: Validates build reachability checks

Run tests with:
```bash
.\gradlew test
```

## Extension Points

### Adding New Builds
Edit `BuildFactory.getBuilds()`:
```java
builds.add(new BuildModel(
    "My Build",
    Map.of("Attack", 70, "Defence", 1),  // Minimums
    Map.of("Prayer", 1, "Magic", 1)      // Maximums
));
```

### Adding Configuration Options
1. Add config items to `CombatLevelCalculatorConfig` with `@ConfigItem` annotations
2. Update `CombatLevelPanel` to read/respect those settings
3. Example: "Show only reachable builds" toggle

### Caching/Async Improvements
- Consider caching build templates in `BuildFactory`
- Use `clientThread.invokeLater()` for stat fetching if needed for responsiveness

## RuneLite Standards Compliance

✅ Java 11 compatible  
✅ No reflection  
✅ Uses injected Gson/OkHttp (not applicable here)  
✅ No blocking on client thread  
✅ Proper plugin naming and config group  
✅ Logging with Slf4j  
✅ Clean shutdown with resource cleanup  

## Known Limitations / Future Work

1. **No external data source**: Build definitions are hardcoded; could be loaded from a file or API
2. **No user settings**: Could add config options (e.g., filter builds by attack type)
3. **No stat export**: Could save/load stat snapshots or share builds
4. **Build list growth**: As more builds are added, the UI list could become long; pagination or filtering would help
5. **No XP calculations**: Currently stat-level based; could estimate XP needed to reach goals

## Refactoring Notes

- **StatsModel**: Could reduce boilerplate with Lombok `@Getter @Setter`
- **CombatLevelPanel**: Stat input fields are repetitive; could be refactored to an array/enum-driven approach
- **CombatCalculator**: Could add inline comments explaining the RuneScape formula coefficients

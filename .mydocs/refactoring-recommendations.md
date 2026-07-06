# Combat Level Calculator — Refactoring Recommendations

## Executive Summary

The plugin is **well-structured and production-ready**. No critical issues were found. Below are **optional** refactorings that would improve code maintainability and reduce boilerplate, listed by priority.

---

## High Priority (Nice to Have)

### 1. Reduce Boilerplate in CombatLevelPanel (Stat Fields)

**Current Issue**: Seven stat input fields are declared and wired separately:
```java
private final JTextField attackField = new JTextField(3);
private final JTextField strengthField = new JTextField(3);
// ... 5 more times
```

**Recommendation**: Use an enum-based approach:

```java
enum Stat {
    ATTACK("Attack"),
    STRENGTH("Strength"),
    DEFENCE("Defence"),
    HITPOINTS("Hitpoints"),
    RANGED("Ranged"),
    PRAYER("Prayer"),
    MAGIC("Magic");

    private final String displayName;
    Stat(String displayName) { this.displayName = displayName; }
}

private final Map<Stat, JTextField> statFields = new EnumMap<>(Stat.class);

// In constructor:
for (Stat stat : Stat.values()) {
    JTextField field = new JTextField(3);
    statFields.put(stat, field);
    addLabeled(top, stat.displayName + ":", field);
    field.getDocument().addDocumentListener(dl);
}
```

**Benefits**:
- Eliminates 7 field declarations
- Easier to add new stats in the future
- Clearer intent (all stats handled uniformly)

**Effort**: ~30 minutes

---

## Medium Priority (Quality Improvements)

### 2. Add Javadoc to CombatCalculator

**Current Issue**: The combat formula coefficients (0.25, 0.325, 1.5) are not explained.

**Recommendation**: Add inline documentation:

```java
public static int computeCombatLevel(StatsModel s)
{
    // RuneScape combat level formula:
    // base = 0.25 × (Defence + Hitpoints + ⌊Prayer/2⌋)
    // highest_damage_skill = max(
    //   0.325 × (Attack + Strength),
    //   0.325 × ⌊Ranged × 1.5⌋,
    //   0.325 × ⌊Magic × 1.5⌋
    // )
    // combat_level = ⌊base + highest_damage_skill⌋
    
    int defence = s.getDefence();
    // ...
}
```

**Benefits**:
- Developers understand the formula intent
- Easier to verify against RuneScape wiki
- Non-obvious coefficients are documented

**Effort**: ~10 minutes

---

### 3. Add Validation to StatsModel

**Current Issue**: StatsModel accepts any integer value (negative, > 99, etc.) without validation.

**Recommendation**: Add bounds checking:

```java
public void setAttack(int attack) {
    if (attack < 0 || attack > 120) {
        log.warn("Attack level {} is out of bounds [0, 120]", attack);
        return; // or throw IllegalArgumentException
    }
    this.attack = attack;
}
```

Or use a setter that validates in the constructor:

```java
public StatsModel(int attack, int strength, ...) {
    this.attack = validateStat("Attack", attack);
    // ...
}

private static int validateStat(String name, int value) {
    if (value < 0 || value > 120) {
        throw new IllegalArgumentException(name + " must be 0-120, got " + value);
    }
    return value;
}
```

**Benefits**:
- Prevents invalid states (negative stats, impossible levels)
- Catches user input errors earlier
- Easier debugging

**Effort**: ~20 minutes

---

### 4. Use Lombok to Reduce StatsModel Boilerplate

**Current Issue**: StatsModel has 7 fields × 2 methods (getter/setter) = 14 lines of repetitive code.

**Recommendation**: Add Lombok dependency and use annotations:

In `build.gradle`:
```gradle
compileOnly 'org.projectlombok:lombok:1.18.30'
annotationProcessor 'org.projectlombok:lombok:1.18.30'
```

Then rewrite `StatsModel.java`:
```java
@Getter
@Setter
public class StatsModel {
    private int attack;
    private int strength;
    private int defence;
    private int hitpoints;
    private int ranged;
    private int prayer;
    private int magic;
}
```

**Benefits**:
- Reduces 14 lines to 7 fields
- Standard Java pattern (Lombok is widely used)
- Easier to add new stats

**Effort**: ~15 minutes

---

## Low Priority (Nice Polish)

### 5. Extract BuildFactory Build Definitions to Enum

**Current Issue**: Build definitions are scattered in `getBuilds()` method; adding new builds requires editing the method.

**Recommendation**: Use an enum:

```java
enum BuildTemplate {
    PURE_1_DEF("1 Defence Pure", 
        Map.of("Attack", 70, "Strength", 70, "Hitpoints", 52),
        Map.of("Defence", 1, "Ranged", 1, "Prayer", 1, "Magic", 1)),
    ZERKER("Zerker",
        Map.of("Attack", 60, "Strength", 60, "Hitpoints", 60),
        Map.of("Defence", 45, "Ranged", 1, "Prayer", 1, "Magic", 1));
    // ... more builds
    
    final String name;
    final Map<String, Integer> minStats;
    final Map<String, Integer> maxStats;
    
    BuildTemplate(String name, Map<String, Integer> min, Map<String, Integer> max) {
        this.name = name;
        this.minStats = min;
        this.maxStats = max;
    }
}

public static List<BuildModel> getBuilds() {
    return Arrays.stream(BuildTemplate.values())
        .map(t -> new BuildModel(t.name, t.minStats, t.maxStats))
        .collect(Collectors.toList());
}
```

**Benefits**:
- Cleaner method body
- Easier to add/remove builds
- Self-documenting code

**Effort**: ~30 minutes

---

### 6. Add Unit Test for StatsModel Validation (Once Added)

**Recommendation** (if validation is added):

```java
@Test(expected = IllegalArgumentException.class)
public void testNegativeAttackThrows() {
    new StatsModel(-1, 50, 40, 50, 50, 50, 50);
}

@Test(expected = IllegalArgumentException.class)
public void testStatsAbove120Throws() {
    new StatsModel(121, 50, 40, 50, 50, 50, 50);
}
```

**Effort**: ~15 minutes (after validation is added)

---

## Not Recommended (Already Good)

✅ **Config interface**: Correct as-is (extensible for future settings)  
✅ **Combat formula**: Correct and well-known; don't change unless RuneScape changes  
✅ **Test structure**: Tests are focused and clean  
✅ **Plugin lifecycle**: Proper use of RuneLite lifecycle hooks  
✅ **Threading**: Synchronous model is fine for lightweight operations  

---

## Refactoring Priority Roadmap

If you plan to improve the plugin over time, suggested order:

1. **Documentation** (Javadoc on CombatCalculator) — 10 min
2. **Stat field refactor** (Enum approach) — 30 min
3. **Lombok integration** (reduce boilerplate) — 15 min
4. **Validation** (StatsModel bounds) — 20 min
5. **Build enum** (cleaner definitions) — 30 min

Total effort: ~2 hours for all improvements.

---

## Decision

**For plugin hub submission**: Current code is production-ready. No refactoring required.

**For long-term maintainability**: High + Medium priority items (1–4) are recommended, especially if:
- You plan to add more stats or builds frequently
- You want cleaner onboarding for future contributors
- You want to prevent user input errors

**Note**: The build.gradle has been simplified (no shadowJar task) since `build=standard` is used.
 **Note**: The plugin now uses a PNG icon (`src/main/resources/icon.png`) with programmatic fallback.

Choose what makes sense for your timeline and goals.

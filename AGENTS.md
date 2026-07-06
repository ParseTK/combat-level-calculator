# RuneLite Plugin Development — Agent Knowledge Base

This document contains the authoritative rules, patterns, and standards for developing RuneLite plugins.
Cline should use this as a reference for ALL RuneLite plugin tasks.

---

## 1. LOGGING STANDARDS

- Use `log.debug()` for developer/diagnostic logging.
- Do NOT use `log.info()` for per-frame or per-event logging — RuneLite runs at INFO level in production, so high-frequency info logs will pollute user logs.
- `log.info()` is acceptable for one-time startup/shutdown messages or infrequent events.

## 2. THREADING & CONCURRENCY

- **Never use `Thread.sleep()`** — it blocks the client thread and will get the plugin rejected.
- **Never block on shutdown**: Do NOT call `executor.awaitTermination()` in `shutDown()`. Just use `executor.shutdownNow()`.
- **Never do blocking I/O on the client thread** (network or disk). Use the OkHttp thread pool for blocking network requests.
- If you need to call back into `client` from an async callback (e.g., OkHttp's `enqueue()`), wrap the client code in `clientThread.invoke()`.
- Explicitly cancel scheduled tasks (`ScheduledFuture`) in `shutDown()`, in addition to shutting down the executor.
- For batching async work, use `CompletableFuture.allOf()` — never `CountDownLatch`.
- If you must call `Process.waitFor()` (rare), always pass a reasonable timeout.

## 3. PERFORMANCE

- Don't scan the entire scene every tick/frame. Use events like `NpcSpawned`, `NpcDespawned`, `GameObjectSpawned`, `ItemSpawned` to track what you care about in your own collection.
- Keep overlay computations (run every frame) to a minimum — avoid heavy loops, string formatting, or allocations in `render()`.

## 4. API & DEPENDENCY INJECTION

### Injecting Dependencies
```java
@Inject
private Client client;

@Inject
private ClientToolbar clientToolbar;

@Inject
private CombatLevelCalculatorConfig config;

@Inject
private OkHttpClient httpClient;  // For HTTP requests

@Inject
private Gson gson;                // For JSON parsing

@Inject
private ToolbarButton toolbarButton;
```

### Config Provider Pattern
```java
@Provides
MyConfig provideConfig(ConfigManager configManager)
{
    return configManager.getConfig(MyConfig.class);
}
```

### Game Value Constants
- Use `net.runelite.api.gameval` package constants — `ItemID`, `InterfaceID`, `ObjectID`, `AnimationID`, `SpotAnimationID`, `VarbitID`, `VarPlayerID`, etc.
- **Never hardcode magic numbers** when gameval constants exist.

### Widget Lookups
```java
// CORRECT — use component ID from gamevals:
client.getWidget(InterfaceID.KourendFavour.FAVOUR_OVERLAY);

// WRONG — do NOT manually combine interface + component IDs
```

### Browser Links
- Use `net.runelite.client.util.LinkBrowser` to open URLs, NOT `java.awt.Desktop`.

## 5. HTTP & JSON (OkHttp + Gson)

- **Use OkHttp for ALL HTTP requests**. Inject with `@Inject OkHttpClient`.
- **NEVER use** `HttpURLConnection`, `java.net.http.HttpClient`, or Apache HttpClient.
- **Inject Gson** with `@Inject Gson`. Never create your own `Gson()` instance. You can use `.newBuilder()` to derive a customized instance.
- **Do NOT add transitive RuneLite dependencies** (Gson, Guice, OkHttp) to `build.gradle` — they are already provided by the client.
- **Never execute OkHttp calls synchronously on the client thread** — always use `enqueue()` which runs on the OkHttp thread pool.

### Correct HTTP Pattern
```java
@Inject
private OkHttpClient httpClient;

@Inject
private Gson gson;

private void fetchData()
{
    Request request = new Request.Builder()
        .url("https://api.example.com/data")
        .build();

    httpClient.newCall(request).enqueue(new Callback()
    {
        @Override
        public void onFailure(Call call, IOException e)
        {
            log.debug("Request failed", e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException
        {
            if (!response.isSuccessful())
            {
                response.close();
                return;
            }

            String body = response.body().string();
            MyData data = gson.fromJson(body, MyData.class);

            // If you need to call back into client, use clientThread.invoke():
            clientThread.invoke(() -> {
                // Access client here
            });
        }
    });
}
```

## 6. FILE I/O

- Only read/write files inside the `.runelite` directory (get path via `RuneLite.RUNELITE_DIR`).
- Create a subdirectory for your plugin: `.runelite/your-plugin-name/`.
- Use `JFileChooser` for user-initiated file operations (import/export).

## 7. CONFIG

### Config Group Naming
- Must be specific: e.g., `"deadman-prices"`, NOT `"deadman"`.
- For this plugin: `"combatlevelcalc"`.

### Config Migration
- **Never rename a config key or config group** without providing a migration script — renaming silently resets all users' saved settings.

### Third-Party Server Warning
If adding a `@ConfigItem` that toggles a feature sending data to a third-party server:
- Must be **disabled by default** (opt-in).
- Must have a `warning` field: `"This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers"`

## 8. PLUGIN SETUP & PACKAGING

### Template Cleanup
When starting from the example plugin template, rename EVERYTHING:
- Package path: `com.example` → `com.yourplugin`
- Class names: `ExamplePlugin` → `YourPlugin`
- Config interface: `ExampleConfig` → `YourConfig`
- Config group: `"example"` → `"yourplugin"`
- `build.gradle` group: update `group = 'com.yourplugin'`
- `settings.gradle` rootProject.name: update
- `runelite-plugin.properties`: update `plugins=` entry

### Build Configuration
```groovy
// build.gradle must target Java 11
tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.release.set(11)
}
```

### Plugin Properties File
```properties
displayName=Your Plugin Name
author=Your Name
description=What the plugin does
tags=tag1,tag2
version=1.0.0
plugins=com.yourplugin.YourPlugin
build=standard
```

### Prohibited Files
- Do NOT include `META-INF/services/net.runelite.client.plugins.Plugin` file (it's auto-generated).
- Do NOT commit build artifacts (no `.class` files, `out/`, `.tmp` directories).

### License
- Retain a permissive license (BSD-2 recommended for RuneLite plugins).

## 9. RESOURCES & ASSETS

- Optimize icon PNGs. Java loads images at full resolution in memory (`width × height × 4` bytes = 1KB for 16×16, but 75KB for 100×100).
- Ensure PNGs are actually PNGs — do NOT rename JPEGs or ICOs to `.png`.
- For simple icons, draw them programmatically with `BufferedImage` + `Graphics2D` to avoid external files.

## 10. CLEANUP & LIFECYCLE

```java
@Override
protected void shutDown() throws Exception
{
    // 1. Cancel scheduled tasks
    if (scheduledFuture != null)
    {
        scheduledFuture.cancel(false);
        scheduledFuture = null;
    }

    // 2. Shut down executor (non-blocking)
    if (executor != null)
    {
        executor.shutdownNow();
        executor = null;
    }

    // 3. Remove toolbar button
    if (navButton != null)
    {
        clientToolbar.removeNavigation(navButton);
        navButton = null;
    }

    // 4. Remove overlays
    overlayManager.remove(myOverlay);

    // 5. Remove event listeners/subscriptions
    eventBus.unregister(this);
}
```

- Remove unused config classes, fields, and imports.
- Do NOT mix code reformatting with feature changes in the same commit — it makes diffs unreadable.

## 11. TESTING RULES (CRITICAL)

You (Cline) CANNOT verify plugin behavior. Even with screen-capture or computer-use tools:
- **Do NOT automate game interaction** — it violates Jagex's third-party client guidelines and will get the user banned.
- Only the user can confirm a plugin works in-game.

After completing a task:
1. Offer to launch RuneLite: `./gradlew run`
2. Tell the user to follow https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts to log in
3. Tell the user WHAT to test — specific behavior, golden path, edge cases
4. Wait for user confirmation before considering the task done (a clean JVM start is not a passing test)

## 12. LANGUAGE & COMPILER RESTRICTIONS

### Forbidden (will get plugin rejected):
- ❌ Java reflection (`Class.forName()`, `Method.invoke()`, etc.)
- ❌ JNI or JNA
- ❌ Direct native memory access via `Unsafe` or LWJGL
- ❌ Executing external processes (`Process`, `ProcessBuilder`, `Runtime.exec()`)
- ❌ Downloading or dynamically loading code (classloading, URLClassLoader)
- ❌ Runtime code generation (bytecode manipulation, `javax.tools.JavaCompiler`)
- ❌ Java serialization/deserialization (`ObjectInputStream`, `ObjectOutputStream`)

### Allowed:
- ✅ Java 11 compatible syntax
- ✅ Lambda expressions (Java 8+)
- ✅ `var` keyword (Java 10+)
- ✅ Lombok annotations (`@Slf4j`, `@Getter`, `@Setter`, `@Inject`)
- ✅ Gson for JSON parsing
- ✅ OkHttp for HTTP

## 13. BOSS & COMBAT RESTRICTIONS

Applies to ALL bosses, Raids sub-bosses, Slayer bosses, Demi-bosses, and wave-based minigames:

- ❌ No next-attack prediction (timing or attack style)
- ❌ No projectile target/landing indicators
- ❌ No prayer switching indicators
- ❌ No attack counters
- ❌ No automatic "stand here" / "don't stand here" indicators (manual tile marking OK)
- ❌ No additional visual/audio indicators of boss mechanics (unless manually triggered external helper)
- ❌ No advance warning of future hazards (highlighting currently active hazards OK)
- ❌ No "flinch" timing helpers
- ❌ No combat prayer recommendations
- ❌ No NPC focus identification
- ❌ No content simulation (e.g., boss fight simulators)
- ❌ New high-end PvM boss plugins are not accepted as a blanket policy

## 14. PVP RESTRICTIONS

- ❌ No removing or deprioritising attack/cast options in PvP
- ❌ No opponent freeze duration indicators
- ❌ No PvP clan opponent identification
- ❌ No PvP loot drop previews
- ❌ No identifying an opponent's opponent
- ❌ No PvP target scouting information
- ❌ No player group summaries (attackable counts, prayer usage, etc.)
- ❌ No level-based PvP player indicators (highlighting attackable players or those within level range)
- ❌ No spell targeting simplification (removing menu options)

## 15. MENU & INTERFACE RESTRICTIONS

### Menu
- ❌ No adding new menu entries that send actions to the server
- ❌ No menu modifications for Construction
- ❌ No menu modifications for Blackjacking
- ❌ No conditional menu entry removal based on NPC type, friend status, etc.

### Interface
- ❌ No unhiding hidden interface components (special attack bar, minimap)
- ❌ No moving/resizing click zones for 3D components
- ❌ No moving/resizing combat, inventory, equipment, or spellbook click zones
- ❌ No resizing prayer book or spellbook components
- ❌ No removing inventory pane background or making it click-through
- ❌ No detached camera world interaction

## 16. INPUT & DATA PRIVACY

### Input
- ❌ No injecting mouse or keyboard events
- ❌ No autotyping (programmatic text insertion into chatbox input)
- ❌ No modifying outgoing chat messages after the user sends them

### Privacy
- ❌ No exposing player information over HTTP
- ❌ No crowdsourcing data about other players (locations, gear, names, etc.)
- ❌ No credential manager plugins that store account credentials

## 17. COMMON CODE PATTERNS

### Plugin Entry Point
```java
@Slf4j
@PluginDescriptor(
    name = "Plugin Name"
)
public class MyPlugin extends Plugin
{
    @Inject
    private Client client;

    @Override
    protected void startUp() throws Exception
    {
        log.debug("My Plugin started!");
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.debug("My Plugin stopped!");
    }
}
```

### Event Subscription
```java
@Subscribe
public void onGameStateChanged(GameStateChanged event)
{
    if (event.getGameState() == GameState.LOGGED_IN)
    {
        // Player just logged in
    }
}

@Subscribe
public void onGameTick(GameTick event)
{
    // Runs every game tick (~600ms)
}

@Subscribe
public void onNpcSpawned(NpcSpawned event)
{
    NPC npc = event.getNpc();
    // Track NPC
}

@Subscribe
public void onNpcDespawned(NpcDespawned event)
{
    // Remove from tracked collection
}
```

### Reading Player Stats
```java
StatsModel s = new StatsModel(
    client.getRealSkillLevel(Skill.ATTACK),
    client.getRealSkillLevel(Skill.STRENGTH),
    client.getRealSkillLevel(Skill.DEFENCE),
    client.getRealSkillLevel(Skill.HITPOINTS),
    client.getRealSkillLevel(Skill.RANGED),
    client.getRealSkillLevel(Skill.PRAYER),
    client.getRealSkillLevel(Skill.MAGIC)
);
```

### Toolbar Button Registration
```java
private NavigationButton navButton;
private MyPanel panel;

@Override
protected void startUp()
{
    panel = new MyPanel();

    // Draw a programmatic icon (no external file needed)
    BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = icon.createGraphics();
    // ... draw your icon ...
    g.dispose();

    navButton = NavigationButton.builder()
        .tooltip("My Plugin")
        .icon(icon)
        .panel(panel)
        .priority(100)  // optional: position in sidebar
        .build();

    clientToolbar.addNavigation(navButton);
}

@Override
protected void shutDown()
{
    if (navButton != null)
    {
        clientToolbar.removeNavigation(navButton);
        navButton = null;
    }
}
```

### Custom Panel (PluginPanel)
```java
public class MyPanel extends PluginPanel
{
    public MyPanel()
    {
        setLayout(new BorderLayout());
        // Add your Swing components here
    }
}
```

## 18. BUILD GRADLE TEMPLATE

```groovy
plugins {
    id 'java'
}

repositories {
    mavenLocal()
    maven {
        url = 'https://repo.runelite.net'
        content {
            includeGroupByRegex("net\\.runelite.*")
        }
    }
    mavenCentral()
}

def runeLiteVersion = 'latest.release'

dependencies {
    compileOnly group: 'net.runelite', name:'client', version: runeLiteVersion

    compileOnly 'org.projectlombok:lombok:1.18.30'
    annotationProcessor 'org.projectlombok:lombok:1.18.30'

    testImplementation 'junit:junit:4.12'
    testImplementation group: 'net.runelite', name:'client', version: runeLiteVersion
    testImplementation group: 'net.runelite', name:'jshell', version: runeLiteVersion
}

group = 'com.yourplugin'

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.release.set(11)
}
```

## 19. COMMAND CHEAT SHEET

```bash
# Build and run tests
./gradlew clean test

# Run dev client with plugin
./gradlew run

# Build JAR for hub submission
./gradlew shadowJar

# Clean build artifacts
./gradlew clean
```

---

## Summary Checklist for New Plugins

- [ ] Package renamed from `com.example` to something unique
- [ ] Config group renamed from `"example"`
- [ ] `build.gradle` group updated
- [ ] `settings.gradle` rootProject.name updated
- [ ] `runelite-plugin.properties` `plugins=` updated
- [ ] No `META-INF/services/...` file
- [ ] No reflection, JNI, external processes, etc.
- [ ] Java 11 target
- [ ] No blocking on client thread
- [ ] Proper shutdown cleanup (executor, scheduled tasks, overlays, toolbar buttons)
- [ ] Tests pass with `./gradlew test`
- [ ] Logging uses `log.debug()` (not `log.info()` for frequent events)
# Plugin Submission Guide

## Creating a New Plugin Repository

### 1. Generate Repository
- Use the RuneLite plugin template or `create_new_plugin.py` script from plugin-hub-tooling
- Name repository appropriately (e.g., `combat-level-calculator`)
- Set repository to **public**

### 2. Clone to IDE
- Click "Clone or download" on GitHub and copy the HTTPS link
- In IntelliJ: **Get from Version Control** → paste URL
- Or use command line: `git clone <repository-url>`

### 3. Rename Package
- Right-click package in sidebar → **Refactor > Rename**
- Change `com.example` to your plugin package (e.g., `com.combatlevelcalc`)
- Rename `ExamplePlugin`, `ExampleConfig`, `ExamplePluginTest` to your names
- Update `pluginMainClass` in `build.gradle`

### 4. Update Plugin Metadata
In `runelite-plugin.properties`:
```properties
displayName=Combat Level Calculator
author=YourName
description=Preview combat level and explore attainable PvP builds
tags=combat,stats,pvp,builds
plugins=com.combatlevelcalc.CombatLevelCalculatorPlugin
version=1.0.0
build=standard
```

### 5. Add Icon (Optional)
- Place `icon.png` at repository root (max 48x72 px)
- Or in `src/main/resources/icon.png` for programmatic loading

### 6. Write README
- Document features, usage, and any configuration options
- Include screenshots if helpful

## Submitting to Plugin Hub

### 1. Fork plugin-hub
- Fork `https://github.com/runelite/plugin-hub`

### 2. Create Branch
```bash
git remote add upstream https://github.com/runelite/plugin-hub.git
git fetch upstream
git checkout -B combat-level-calculator upstream/master
```

### 3. Create Plugin Manifest
Create `plugins/combat-level-calculator` with:
```
repository=https://github.com/yourusername/combat-level-calculator.git
commit=<40-character-commit-hash>
```

Get commit hash from GitHub → Commits → click latest commit → copy full hash.

### 4. Push and Create PR
```bash
git add plugins/combat-level-calculator
git commit -m "add combat-level-calculator"
git push -f -u origin combat-level-calculator
```
- Create PR via GitHub UI or `gh pr create -w`

## Updating a Plugin

```bash
# Update to latest upstream
git fetch upstream
git checkout -B combat-level-calculator upstream/master

# Update commit hash in plugins/combat-level-calculator
# ... edit file ...

git add plugins/combat-level-calculator
git commit -m "update combat-level-calculator"
git push -f -u origin combat-level-calculator
```

## Build Verification

- Check CI workflow: `.github/workflows/build.yml` → should show ✔️
- If ❌, click **Details** to see build log
- Check "RuneLite Plugin Hub Checks" for any "Changes are needed"

## Review Process

- Maintainers check for:
  - No malicious code
  - Complies with Jagex rules
  - Not a rejected/rolled-back feature
- If unclear whether plugin is allowed, it won't be merged

## Resources

- Use `Class.getResourceAsStream()` for loading resources (works in JAR)
- Avoid `Class.getResource()` (returns jar-URL in production)

## Third-Party Dependencies

- Add to `thirdParty` configuration in `package/verification-template/build.gradle`
- Run `./gradlew --write-verification-metadata sha256`
- Increases review time - avoid unless necessary

## Build Type

- `build=standard`: Your `build.gradle` is replaced during submission (faster review)
- `build=gradle`: Your build file is used as-is (for custom dependencies)

## Troubleshooting

### Outdated Client
- Set `runeLiteVersion` to `latest.release` in `build.gradle`
- **Gradle Tool Window** → Right-click project → **Refresh Gradle Dependencies**
- Or **Invalidate caches** in IDE
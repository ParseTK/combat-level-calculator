# Combat Level Calculator — User Guide

## Installation

1. Clone or download the plugin repository
2. Run `.\gradlew run` from the project root
3. The RuneLite development client will launch with the plugin loaded

## Features

### 1. View Your Current Combat Level
- Open the **Combat Level Calculator** panel from the RuneLite client toolbar (look for the dragon dagger icon)
- Your stats are automatically loaded when you log in
- The combat level is displayed at the top of the panel

### 2. Edit Stats Manually
- Type custom values into any stat input box (Attack, Strength, Defence, etc.)
- Combat level updates **live** as you type
- Useful for planning future stat increases

### 3. Fetch Current Account Stats
- Click the **"Fetch stats"** button to reload your current stats from your account
- Useful if you've leveled up or want to reset the inputs to your actual stats

### 4. Explore PvP Builds
The plugin shows a list of popular PvP account archetypes:

#### Reachable Builds (Green section)
- Builds you can currently achieve with your stats
- Shows the exact stat increases needed to reach each build
- Combat level is displayed for reference

#### Unattainable Builds (Gray/Hidden section)
- Builds you cannot currently reach (missing stats or stat limits exceeded)
- Shows what stats are blocking you from reaching each build
- Hidden by default; toggle **"Show unattainable builds"** to display them

### 5. Example Builds Included
 
 - **Obby Mauler** (Strength 60, Hitpoints 49, Magic 42, Ranged 42, Attack/Defence ≤ 1): Pure melee with obby maul
 - **50 Attack Pure** (Strength 99, Hitpoints 90, Magic 99, Ranged 99, Attack 50): High damage pure
 - **60 Attack Pure** (Strength 99, Hitpoints 90, Magic 99, Ranged 99, Attack 60, Prayer 31): Higher attack pure
 - **75 Attack Pure** (Strength 99, Hitpoints 90, Magic 99, Ranged 99, Attack 75, Prayer 52): Maxed attack pure
 - **Zerker** (Attack 60, Strength 99, Hitpoints 93, Ranged 99, Magic 99, Defence 45, Prayer 31): Hybrid with 45 defence
 - **Tank** (Attack 50, Strength 93, Hitpoints 94, Ranged 95, Magic 95, Defence 70, Prayer 77): High defence hybrid

## Tips

- **Planning ahead**: Use the unattainable builds list to see what your next realistic goal could be
- **Multi-build accounts**: A Zerker can train Attack/Strength to become a Ranged Main (as long as Defence stays within limits)
- **Combat level prediction**: The estimated combat level for each build helps you understand trade-offs

## Troubleshooting

**Stats not updating?**
- Ensure you're logged in to the RuneLite client
- Click "Fetch stats" to manually reload
- Check that the plugin panel is visible in the toolbar

**Incorrect combat level?**
- Double-check your stat inputs; combat level is calculated instantly
- The formula uses RuneScape's standard combat calculation

**Build seems impossible?**
- Some builds have strict stat caps (e.g., Defence ≤ 1 for a pure)
- If you've already trained a stat too high, that build path is blocked

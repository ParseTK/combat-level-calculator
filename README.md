# Combat Level Calculator

A RuneLite plugin that allows players to preview their combat level based on custom stat inputs and discover which PvP account builds (e.g., Pure, Zerker, Tank) are attainable from their current stats.

## Features

- **Live Combat Level Preview**: Shows computed combat level immediately when stat inputs change
- **Fetch Stats**: Populates inputs from your account stats with a single click
- **PvP Build Discovery**: Lists popular PvP account archetypes and shows which are reachable
- **Build Details**: Shows required stat increases to reach each build, or which stats are blocking you
- **Toggle Unattainable Builds**: Show or hide builds you cannot currently reach

## Usage

1. Open the **Combat Level Calculator** panel from the RuneLite client toolbar
2. Your stats are automatically loaded when you log in
3. Edit stat inputs to preview alternate combat levels
4. View reachable and unattainable PvP builds

## Building

```bash
# Run tests
.\gradlew test

# Run the plugin in the development client
.\gradlew run
```

## Testing

```bash
.\gradlew test
```

## License

BSD-2
**Combat Level Calculator — Plan & Design**

**Scope**: Provide an in-plugin tool that displays the user's current stats, allows editing stat input boxes to preview alternate combat levels, and lists potential PvP account builds that are attainable or unattainable from the current stats. Include a toggle to show/hide unattainable builds.

**Goals**:
- **Live Preview**: Show computed combat level immediately when inputs change.
- **Fetch Stats**: Populate inputs from the user's account on plugin tab open.
- **Build Generation**: Produce a list of example PvP builds (e.g., pure, zerker, tank) the account can reach and those it cannot yet reach.
- **Filtering**: Option to hide unattainable builds.
- **Standards**: Follow RuneLite plugin rules (no reflection, OkHttp + injected Gson, avoid blocking client thread, proper config group naming).

**Design — High level**:
- **UI**: A plugin panel under the plugin tab with:
  - input boxes for core stats (Attack, Strength, Defence, Hitpoints, Ranged, Prayer, Magic)
  - `Fetch stats` button that fills inputs from the client when available
  - computed combat level display (live)
  - two lists: `Reachable builds` and `Unattainable builds` with a `Show unattainable` toggle
  - per-build details: required stat changes to reach the build
- **Data model**:
  - `Stats` object (int values for each skill)
  - `Build` object (name, target stats, category, reachable(boolean), delta map)

**Algorithm**:
- Use RuneScape combat level formula to compute base combat level from stats.
- For build generation, define a curated list of target builds (e.g., 1-def pure, zerker, tank) and compute deltas from current stats. Mark reachable if all deltas are non-negative and within plausible experience constraints.

**Unattainable builds handling**:
- Compute required level increases per-skill for each build, show them under `Unattainable builds`.
- Provide `Show/Hide unattainable` toggle to keep UI clean.

**Performance & Threading**:
- Fetching must run off the client thread when calling blocking APIs. Use clientThread.invokeLater for client access and OkHttp/Gson for remote calls if needed.

**Testing**:
- Unit test combat level calculation with known vectors.
- Integration test for UI data binding (where feasible with existing test framework).

**Runelite Standards & Safety**:
- Follow rules in AGENTS.md: Java 11, no reflection, no external process execution, do not block client thread, config group naming, read/write only in `.runelite/` if files required.

**Deliverables**:
- `ExamplePlugin.java` updates + new UI panel class
- `StatsModel.java`, `BuildModel.java`
- Unit tests under `test/`
- Updated `README.md` with testing instructions
- `.mydocs/plan.md` (this file) for your approval

**Next steps (what I'll do after approval)**:
1. Scaffold UI panel and `StatsModel` + `BuildModel` classes.
2. Implement stat fetch + editable inputs + live combat calc.
3. Add curated builds list + reachability checks + show/hide toggle.
4. Write unit tests and run `./gradlew test`.

**Approval requested**: Please review this plan. If you approve, I will begin implementing step 1.

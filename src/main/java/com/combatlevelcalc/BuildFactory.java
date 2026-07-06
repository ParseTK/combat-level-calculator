package com.combatlevelcalc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BuildFactory
{
    private BuildFactory() {}

    public static List<BuildModel> getBuilds()
    {
        List<BuildModel> builds = new ArrayList<>();

        builds.add(new BuildModel(
            "Obby Mauler",
            Map.of(
                "Strength", 60,
                "Hitpoints", 49,
                "Magic", 42,
                "Ranged", 42
            ),
            Map.of(
                "Attack", 1,
                "Defence", 1,
                "Prayer", 1
            )));

        builds.add(new BuildModel(
            "50 Attack Pure",
            Map.of(
                "Strength", 99,
                "Hitpoints", 90,
                "Magic", 99,
                "Ranged", 99
            ),
            Map.of(
                "Attack", 50,
                "Defence", 1,
                "Prayer", 31
            )));

        builds.add(new BuildModel(
            "60 Attack Pure",
            Map.of(
                "Strength", 99,
                "Hitpoints", 90,
                "Magic", 99,
                "Ranged", 99
            ),
            Map.of(
                "Attack", 60,
                "Defence", 1,
                "Prayer", 52
            )));

        builds.add(new BuildModel(
            "75 Attack Pure",
            Map.of(
                "Strength", 99,
                "Hitpoints", 90,
                "Magic", 99,
                "Ranged", 99
            ),
            Map.of(
                "Attack", 75,
                "Defence", 1,
                "Prayer", 52
            )));

        builds.add(new BuildModel(
            "Zerker",
            Map.of(
                "Attack", 60,
                "Strength", 99,
                "Hitpoints", 93,
                "Ranged", 99,
                "Magic", 99
            ),
            Map.of(
                "Defence", 45,    
                "Prayer", 31
            )));

        builds.add(new BuildModel(
            "Zerker Brid",
            Map.of(
                "Attack", 60,
                "Strength", 99,
                "Hitpoints", 93,
                "Ranged", 99,
                "Magic", 99
            ),
            Map.of(
                "Defence", 45,    
                "Prayer", 45
            )));

        builds.add(new BuildModel(
            "Tank",
            Map.of(
                "Attack", 50,
                "Strength", 93,
                "Hitpoints", 94,
                "Ranged", 95,
                "Magic", 95
            ),
            Map.of(
                "Defence", 70,
                "Prayer", 77
            )));

        return builds;
    }

    public static List<BuildModel> evaluateBuilds(StatsModel current, List<BuildModel> templates)
    {
        List<BuildModel> results = new ArrayList<>();

        for (BuildModel template : templates)
        {
            BuildModel build = new BuildModel(template.getName(), template.getMinimumStats(), template.getMaximumStats());
            Map<String, Integer> delta = new HashMap<>();
            Map<String, Integer> blocked = new HashMap<>();

            // Compute deltas (what's needed to reach minimums)
            for (var entry : template.getMinimumStats().entrySet())
            {
                int currentValue = currentValueFor(entry.getKey(), current);
                int required = entry.getValue();
                int diff = Math.max(0, required - currentValue);
                if (diff > 0)
                {
                    delta.put(entry.getKey(), diff);
                }
            }

            // Compute blocked stats (exceeding maximum caps)
            for (var entry : template.getMaximumStats().entrySet())
            {
                int currentValue = currentValueFor(entry.getKey(), current);
                int limit = entry.getValue();
                if (currentValue > limit)
                {
                    blocked.put(entry.getKey(), currentValue - limit);
                }
            }

            // A build is reachable only if no max caps are exceeded
            boolean reachable = blocked.isEmpty();

            build.setDelta(delta);
            build.setBlockedStats(blocked);
            build.setReachable(reachable);
            build.setEstimatedCombatLevel(CombatCalculator.computeCombatLevel(buildToModel(build)));
            results.add(build);
        }

        return results;
    }

    private static StatsModel buildToModel(BuildModel build)
    {
        return new StatsModel(
            Math.max(build.getMinimumStats().getOrDefault("Attack", 1), build.getMaximumStats().getOrDefault("Attack", 1)),
            Math.max(build.getMinimumStats().getOrDefault("Strength", 1), build.getMaximumStats().getOrDefault("Strength", 1)),
            Math.max(build.getMinimumStats().getOrDefault("Defence", 1), build.getMaximumStats().getOrDefault("Defence", 1)),
            Math.max(build.getMinimumStats().getOrDefault("Hitpoints", 1), build.getMaximumStats().getOrDefault("Hitpoints", 1)),
            Math.max(build.getMinimumStats().getOrDefault("Ranged", 1), build.getMaximumStats().getOrDefault("Ranged", 1)),
            Math.max(build.getMinimumStats().getOrDefault("Prayer", 1), build.getMaximumStats().getOrDefault("Prayer", 1)),
            Math.max(build.getMinimumStats().getOrDefault("Magic", 1), build.getMaximumStats().getOrDefault("Magic", 1))
        );
    }

    private static int currentValueFor(String stat, StatsModel current)
    {
        switch (stat)
        {
            case "Attack":
                return current.getAttack();
            case "Strength":
                return current.getStrength();
            case "Defence":
                return current.getDefence();
            case "Hitpoints":
                return current.getHitpoints();
            case "Ranged":
                return current.getRanged();
            case "Prayer":
                return current.getPrayer();
            case "Magic":
                return current.getMagic();
            default:
                return 0;
        }
    }
}
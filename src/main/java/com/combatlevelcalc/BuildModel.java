package com.combatlevelcalc;

import java.util.Map;

public class BuildModel
{
    private final String name;
    private final Map<String, Integer> minimumStats;
    private final Map<String, Integer> maximumStats;
    private boolean reachable;
    private Map<String, Integer> delta;
    private Map<String, Integer> blockedStats;
    private int estimatedCombatLevel;

    public BuildModel(String name, Map<String, Integer> minimumStats, Map<String, Integer> maximumStats)
    {
        this.name = name;
        this.minimumStats = minimumStats;
        this.maximumStats = maximumStats;
    }

    public String getName() { 
        return name; 
    }

    public Map<String, Integer> getMinimumStats() { 
        return minimumStats; 
    }

    public Map<String, Integer> getMaximumStats() { 
        return maximumStats; 
    }

    public boolean isReachable() { 
        return reachable; 
    }

    public void setReachable(boolean reachable) { 
        this.reachable = reachable; 
    }

    public Map<String, Integer> getDelta() { 
        return delta; 
    }

    public void setDelta(Map<String, Integer> delta) { 
        this.delta = delta; 
    }

    public Map<String, Integer> getBlockedStats() { 
        return blockedStats; 
    }

    public void setBlockedStats(Map<String, Integer> blockedStats) { 
        this.blockedStats = blockedStats; 
    }

    public int getEstimatedCombatLevel() { 
        return estimatedCombatLevel; 
    }

    public void setEstimatedCombatLevel(int estimatedCombatLevel) { 
        this.estimatedCombatLevel = estimatedCombatLevel; 
    }
}

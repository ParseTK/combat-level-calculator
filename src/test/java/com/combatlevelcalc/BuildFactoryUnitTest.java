package com.combatlevelcalc;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class BuildFactoryUnitTest
{
    @Test
    public void testObbyMaulerReachableAtExactlyMinimums()
    {
        // If current stats meet minimums and don't exceed max caps, build should be reachable
        StatsModel current = new StatsModel(1, 60, 1, 49, 42, 1, 42);
        List<BuildModel> builds = BuildFactory.evaluateBuilds(current, BuildFactory.getBuilds());
        assertTrue(builds.stream().anyMatch(build -> build.getName().equals("Obby Mauler") && build.isReachable()));
    }

    @Test
    public void testZerkerReachableWhenBelowMinimums()
    {
        // If current stats are below minimums but haven't exceeded max caps, build is still reachable
        // Zerker needs Attack 60, Strength 99 - at 40/40/40/40 it's not there yet, but possible
        StatsModel current = new StatsModel(40, 40, 40, 40, 1, 1, 1);
        List<BuildModel> builds = BuildFactory.evaluateBuilds(current, BuildFactory.getBuilds());
        assertTrue(builds.stream().anyMatch(build -> build.getName().equals("Zerker") && build.isReachable()));
    }

    @Test
    public void testObbyMaulerUnreachableWhenExceedsMaxCap()
    {
        // If attack is 70, Obby Mauler should show unattainable (blocked by max cap Attack ≤ 1)
        StatsModel current = new StatsModel(70, 60, 1, 49, 42, 1, 42);
        List<BuildModel> builds = BuildFactory.evaluateBuilds(current, BuildFactory.getBuilds());
        assertTrue(builds.stream().anyMatch(build -> build.getName().equals("Obby Mauler") && !build.isReachable()));
    }

    @Test
    public void testZerkerReachableWithHigherStats()
    {
        // Zerker caps Defence at 45 - having Defence 44 should still be reachable
        // Stats: Attack, Strength, Defence, Hitpoints, Ranged, Prayer, Magic
        StatsModel current = new StatsModel(60, 99, 44, 93, 99, 31, 99);
        List<BuildModel> builds = BuildFactory.evaluateBuilds(current, BuildFactory.getBuilds());
        assertTrue(builds.stream().anyMatch(build -> build.getName().equals("Zerker") && build.isReachable()));
    }

    @Test
    public void testZerkerUnreachableWhenDefenceTooHigh()
    {
        // Zerker caps Defence at 45 - having Defence 50 should make it unattainable
        StatsModel current = new StatsModel(60, 99, 50, 93, 99, 99, 31);
        List<BuildModel> builds = BuildFactory.evaluateBuilds(current, BuildFactory.getBuilds());
        assertTrue(builds.stream().anyMatch(build -> build.getName().equals("Zerker") && !build.isReachable()));
    }
}
package com.combatlevelcalc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CombatCalculatorUnitTest
{
    @Test
    public void testComputeCombatLevelPure()
    {
        StatsModel stats = new StatsModel(70, 70, 1, 52, 1, 1, 1);
        assertEquals(58, CombatCalculator.computeCombatLevel(stats));
    }

    @Test
    public void testComputeCombatLevelZerker()
    {
        StatsModel stats = new StatsModel(60, 60, 45, 60, 1, 1, 1);
        assertEquals(65, CombatCalculator.computeCombatLevel(stats));
    }

    @Test
    public void testComputeCombatLevelMage()
    {
        StatsModel stats = new StatsModel(1, 1, 40, 60, 1, 52, 99);
        assertEquals(79, CombatCalculator.computeCombatLevel(stats));
    }
}

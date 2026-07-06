package com.combatlevelcalc;

public final class CombatCalculator
{
    private CombatCalculator() {}

    /**
     * Computes the combat level according to RuneScape's standard formula:
     * <pre>
     * base = 0.25 × (Defence + Hitpoints + ⌊Prayer/2⌋)
     * melee = 0.325 × (Attack + Strength)
     * range = 0.325 × ⌊Ranged × 1.5⌋
     * mage = 0.325 × ⌊Magic × 1.5⌋
     * combat_level = ⌊base + max(melee, range, mage)⌋
     * </pre>
     * @param s the player's combat stats
     * @return the computed combat level
     */
    
    public static int computeCombatLevel(StatsModel s)
    {
        int defence = s.getDefence();
        int hp = s.getHitpoints();
        int prayer = s.getPrayer();
        int attack = s.getAttack();
        int strength = s.getStrength();
        int ranged = s.getRanged();
        int magic = s.getMagic();

        double base = 0.25 * (defence + hp + Math.floor(prayer / 2.0));

        double melee = 0.325 * (attack + strength);
        double range = 0.325 * (Math.floor(ranged * 1.5));
        double mage = 0.325 * (Math.floor(magic * 1.5));

        double highest = Math.max(melee, Math.max(range, mage));

        return (int) Math.floor(base + highest);
    }
}

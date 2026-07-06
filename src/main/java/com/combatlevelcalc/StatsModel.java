package com.combatlevelcalc;

public class StatsModel
{
    private static final int MIN_STAT = 0;
    private static final int MAX_STAT = 99;

    private int attack;
    private int strength;
    private int defence;
    private int hitpoints;
    private int ranged;
    private int prayer;
    private int magic;

    public StatsModel() {}

    public StatsModel(int attack, int strength, int defence, int hitpoints, int ranged, int prayer, int magic)
    {
        this.attack = validateStat("Attack", attack);
        this.strength = validateStat("Strength", strength);
        this.defence = validateStat("Defence", defence);
        this.hitpoints = validateStat("Hitpoints", hitpoints);
        this.ranged = validateStat("Ranged", ranged);
        this.prayer = validateStat("Prayer", prayer);
        this.magic = validateStat("Magic", magic);
    }

    private static int validateStat(String name, int value)
    {
        if (value < MIN_STAT || value > MAX_STAT)
        {
            return Math.max(MIN_STAT, Math.min(MAX_STAT, value));
        }
        return value;
    }

    public int getAttack() { 
        return attack; 
    }
    
    public void setAttack(int attack) { 
        this.attack = validateStat("Attack", attack); 
    }

    public int getStrength() { 
        return strength; 
    }

    public void setStrength(int strength) { 
        this.strength = validateStat("Strength", strength); 
    }

    public int getDefence() { 
        return defence; 
    }

    public void setDefence(int defence) { 
        this.defence = validateStat("Defence", defence); 
    }

    public int getHitpoints() { 
        return hitpoints; 
    }

    public void setHitpoints(int hitpoints) { 
        this.hitpoints = validateStat("Hitpoints", hitpoints); 
    }

    public int getRanged() { 
        return ranged; 
    }

    public void setRanged(int ranged) { 
        this.ranged = validateStat("Ranged", ranged); 
    }

    public int getPrayer() { 
        return prayer; 
    }

    public void setPrayer(int prayer) { 
        this.prayer = validateStat("Prayer", prayer); 
    }

    public int getMagic() { 
        return magic; 
    }

    public void setMagic(int magic) { 
        this.magic = validateStat("Magic", magic); 
    }
}

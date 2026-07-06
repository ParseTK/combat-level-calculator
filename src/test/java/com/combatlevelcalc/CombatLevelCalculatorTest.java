package com.combatlevelcalc;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CombatLevelCalculatorTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CombatLevelCalculatorPlugin.class);
		RuneLite.main(args);
	}
}

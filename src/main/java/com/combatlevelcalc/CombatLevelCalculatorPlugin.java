package com.combatlevelcalc;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@PluginDescriptor(
	name = "Combat Level Calculator"
)
public class CombatLevelCalculatorPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private CombatLevelCalculatorConfig config;

	private NavigationButton navButton;
	private CombatLevelPanel panel;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Combat Level Calculator started!");

		panel = new CombatLevelPanel();
		panel.setFetchAction(e -> fetchCurrentStats());

		navButton = NavigationButton.builder()
			.tooltip("Combat Level Calculator")
			.icon(createIcon())
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			fetchCurrentStats();
		}
	}

	/**
	 * Load the icon from the resources folder
	 */
	private BufferedImage createIcon()
	{
		try (InputStream stream = getClass().getResourceAsStream("/icon.png"))
		{
			if (stream != null)
			{
				return ImageIO.read(stream);
			}
		}
		catch (IOException e)
		{
			log.debug("Failed to load icon", e);
		}

		// Fallback to programmatic icon if PNG not found
		BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = icon.createGraphics();
		try
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(190, 200, 220));
			g.fillRect(4, 2, 8, 12);
		}
		finally
		{
			g.dispose();
		}
		return icon;
	}

	private void fetchCurrentStats()
	{
		try
		{
			StatsModel s = new StatsModel(
				client.getRealSkillLevel(Skill.ATTACK),
				client.getRealSkillLevel(Skill.STRENGTH),
				client.getRealSkillLevel(Skill.DEFENCE),
				client.getRealSkillLevel(Skill.HITPOINTS),
				client.getRealSkillLevel(Skill.RANGED),
				client.getRealSkillLevel(Skill.PRAYER),
				client.getRealSkillLevel(Skill.MAGIC)
			);
			panel.setStats(s);
		}
		catch (Exception ex)
		{
			log.debug("Failed to fetch stats for panel", ex);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("Combat Level Calculator stopped!");
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
			navButton = null;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			fetchCurrentStats();
		}
	}

	@Provides
	CombatLevelCalculatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CombatLevelCalculatorConfig.class);
	}
}
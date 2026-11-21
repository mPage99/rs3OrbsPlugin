package com.rs3orbs;

import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "RS3 XP Orbs",
	description = "Show RS3-inspired skill progress orbs and XP drops",
	tags = {"xp", "experience", "skills", "overlay"}
)
public class Rs3OrbsPlugin extends Plugin
{
	private static final int INITIALIZATION_TICKS = 2;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Rs3OrbsOverlay overlay;

	private final Map<Skill, Integer> previousXp = new EnumMap<>(Skill.class);
	private int ticksUntilInitialization;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			initializeFromCurrentExperience();
		}
		else
		{
			beginInitialization();
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		clearTrackingState();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState gameState = event.getGameState();
		if (gameState == GameState.LOGGING_IN || gameState == GameState.HOPPING)
		{
			beginInitialization();
		}
		else if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST)
		{
			clearTrackingState();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (ticksUntilInitialization == 0 || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (--ticksUntilInitialization == 0)
		{
			for (Skill skill : Skill.values())
			{
				previousXp.putIfAbsent(skill, client.getSkillExperience(skill));
			}
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		Skill skill = event.getSkill();
		if (skill == null)
		{
			return;
		}

		int xp = event.getXp();
		Integer oldXp = previousXp.put(skill, xp);
		if (oldXp == null)
		{
			// The first observation for a skill after login is its baseline, not an XP drop.
			return;
		}

		int gainedXp = xp - oldXp;
		if (gainedXp > 0)
		{
			overlay.recordXpGain(skill, gainedXp, xp);
		}
	}

	private void beginInitialization()
	{
		clearTrackingState();
		ticksUntilInitialization = INITIALIZATION_TICKS;
	}

	private void initializeFromCurrentExperience()
	{
		clearTrackingState();
		for (Skill skill : Skill.values())
		{
			previousXp.put(skill, client.getSkillExperience(skill));
		}
	}

	private void clearTrackingState()
	{
		previousXp.clear();
		ticksUntilInitialization = 0;
		overlay.clearState();
	}
}

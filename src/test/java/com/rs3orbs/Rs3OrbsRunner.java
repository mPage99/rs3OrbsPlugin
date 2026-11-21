package com.rs3orbs;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class Rs3OrbsRunner
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(Rs3OrbsPlugin.class);
		RuneLite.main(args);
	}
}

package com.salvaginghelper;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SalvagingHelperPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SalvagingHelperPlugin.class);
		RuneLite.main(args);
	}
}
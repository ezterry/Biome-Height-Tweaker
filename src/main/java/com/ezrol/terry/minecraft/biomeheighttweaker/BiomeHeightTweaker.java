package com.ezrol.terry.minecraft.biomeheighttweaker;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.Level;

@Mod(
		modid = BiomeHeightTweaker.MODID,
		version = BiomeHeightTweaker.VERSION,
		guiFactory = "com.ezrol.terry.minecraft.biomeheighttweaker.GuiFactory",
		acceptableRemoteVersions = "*"
)
public class BiomeHeightTweaker {
	public static final String MODID = "biomeheighttweaker";
	public static final String VERSION = "${version}";
	public static boolean logging = true; // extra logging always keep false in
											// source control
	public static ConfigHandler config;
	public static boolean village;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new ConfigHandler(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		config.postInit();
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static void log(Level lvl, String message) {
		/**
		 * mod logging funcition, only when "logging" is set to true above
		 */
		if (lvl == Level.FATAL) {
			/* Major issue force enable logging for this and future messages */
			logging = true;
		}
		if (logging || lvl == Level.ERROR) {
			FMLLog.log(BiomeHeightTweaker.MODID, lvl, message);
		}
	}
    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event) {
		log(Level.INFO,"config change" + event.modID);
		if (event.modID.equals(MODID)) {
			config.configUpdated();
		}
	}
}

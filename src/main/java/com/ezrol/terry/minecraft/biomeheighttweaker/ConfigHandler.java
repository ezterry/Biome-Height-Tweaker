package com.ezrol.terry.minecraft.biomeheighttweaker;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

public class ConfigHandler {
	/**
	 * Class to load and maintain the configuration
	 */
	private File confFile;
	private Map<String, BiomeHeightData> biomeDefaults; // default values for
														// the biomes
	private Configuration cfg; // mod config
	// field accessors
	private static Field fieldBaseHeight;
	private static Field fieldHeightVariation;

	public ConfigHandler(File f) {
		/* just set the file to be used for the config */
		boolean deob;

		confFile = f;
		cfg = new Configuration(confFile);
		biomeDefaults = new HashMap<String, BiomeHeightData>();

		/*
		 * We need to grant ourselves access to two private methods of
		 * BiomeGenBase
		 */
		try {
			BiomeGenBase.class.getDeclaredField("baseHeight");
			deob = true;
		} catch (NoSuchFieldException e) {
			deob = false;
		}
		try {
			if (deob) {
				fieldBaseHeight = BiomeGenBase.class.getDeclaredField("baseHeight");
				fieldHeightVariation = BiomeGenBase.class.getDeclaredField("heightVariation");
			} else {
				fieldBaseHeight = BiomeGenBase.class.getDeclaredField("field_76748_D");
				fieldHeightVariation = BiomeGenBase.class.getDeclaredField("field_76749_E");
			}
			fieldBaseHeight.setAccessible(true);
			fieldHeightVariation.setAccessible(true);
		} catch (Exception e) {
			BiomeHeightTweaker.log(Level.FATAL, "Unable to find reflected class: ");
			BiomeHeightTweaker.log(Level.FATAL, e.toString());
			Field fields[] = BiomeGenBase.class.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				BiomeHeightTweaker.log(Level.INFO, fields[i].getName());
			}
			throw new RuntimeException(e);
		}
	}

	private void updateBiome(String biomeName, BiomeGenBase biome) {
		float cfgHeight;
		float cfgVariation;
		BiomeHeightData defaultData = biomeDefaults.get(biomeName);

		// Load Height
		cfgHeight = cfg.getFloat("baseheight", BiomeHeightTweaker.MODID + "." + biomeName, defaultData.getHeight(),
				Math.min(defaultData.getHeight(), (float) -2.5), Math.max(defaultData.getHeight(), (float) 4.5),
				"Biome Base Height", "biomeheighttweaker.config.biomeheight");

		// Set Comment on category
		cfg.getCategory(BiomeHeightTweaker.MODID + "." + biomeName).setComment(biome.getBiomeName());

		// Get Variation
		cfgVariation = cfg.getFloat("heightvariation", BiomeHeightTweaker.MODID + "." + biomeName,
				defaultData.getVariation(), Math.min(defaultData.getVariation(), (float) 0.0),
				Math.max(defaultData.getVariation(), (float) 7.0), "Biome Height Variation",
				"biomeheighttweaker.config.biomevar");

		// Update base class
		if (cfgHeight != biome.getBaseHeight()) {
			try {
				fieldBaseHeight.setFloat(biome, cfgHeight);
				if (biome.getBaseHeight() != cfgHeight) {
					BiomeHeightTweaker.log(Level.ERROR,
							"Biome seems to be ignoring height setting: " + defaultData.getName());
					BiomeHeightTweaker.log(Level.ERROR, "Returned: " + biome.getBaseHeight() + " not " + cfgHeight);
				}
			} catch (Exception e) {
				BiomeHeightTweaker.log(Level.ERROR,
						"unexpected error setting Base Height for: " + defaultData.getName());
			}
		}
		if (cfgVariation != biome.getHeightVariation()) {
			try {
				fieldHeightVariation.setFloat(biome, cfgVariation);
				if (biome.getHeightVariation() != cfgVariation) {
					BiomeHeightTweaker.log(Level.ERROR,
							"Biome seems to be ignoring variation setting: " + defaultData.getName());
					BiomeHeightTweaker.log(Level.ERROR,
							"Returned: " + biome.getHeightVariation() + " not " + cfgVariation);
				}
			} catch (Exception e) {
				BiomeHeightTweaker.log(Level.ERROR,
						"unexpected error setting Height Variation for: " + defaultData.getName());
			}
		}
		// debug check
		if (BiomeHeightTweaker.logging) {
			BiomeHeightTweaker.log(Level.INFO,
					defaultData.getName() + " set to h:" + biome.getBaseHeight() + " v: " + biome.getHeightVariation());
		}
	}

	public void postInit() {
		/*
		 * To be called in post init, loads the "default" value and initializes
		 * any values in the txt config
		 */

		// load actual config file
		cfg.load();

		// get the village generation information
		// this can only happen on init
		BiomeHeightTweaker.village = cfg.getBoolean("Enable Ice Plains Village", BiomeHeightTweaker.MODID, false,
				"Generated villages in Ice Plains biomes", "biomeheighttweaker.config.village");
		// "update" the configs to load the rest
		configUpdated();

	}

	public void configUpdated() {
		FMLControlledNamespacedRegistry<BiomeGenBase> biomeRegister = GameData.getBiomeRegistry();
		Iterator<BiomeGenBase> biomeitr = biomeRegister.iterator();
		BiomeGenBase biome;
		BiomeHeightData defaultData;
		String biomeName;

		// loop all registered biomes
		while (biomeitr.hasNext()) {
			biome = biomeitr.next();
			biomeName = biomeRegister.getNameForObjectBypass(biome).toString();

			BiomeHeightTweaker.log(Level.INFO, "Loading " + biomeName);
			if (!biomeDefaults.containsKey(biomeName)) {
				defaultData = new BiomeHeightData(biomeName, biome.getBaseHeight(), biome.getHeightVariation());
				biomeDefaults.put(biomeName, defaultData);
			} else {
				defaultData = biomeDefaults.get(biomeName);
			}
			BiomeHeightTweaker.log(Level.INFO, "default: " + defaultData);

			// get the values from the config and update if needed
			updateBiome(biomeName, biome);
		}
		// save cfg
		cfg.save();
	}

	public List<Object> getGuiPropList() {
		List<Object> lst = new ArrayList<Object>();
		Property village;
		Property title;
		ConfigCategory maincat = cfg.getCategory(BiomeHeightTweaker.MODID);
		FMLControlledNamespacedRegistry<BiomeGenBase> biomeRegister = GameData.getBiomeRegistry();
		Iterator<BiomeGenBase> biomeitr = biomeRegister.iterator();
		BiomeGenBase biome;
		String biomeName;
		Set<ConfigCategory> cfgBiomeCats;
		Iterator<ConfigCategory> cfgIter;
		ConfigCategory biomeCat = null;

		village = maincat.get("Enable Ice Plains Village");
		village.setRequiresMcRestart(true);

		lst.add(village);

		cfgBiomeCats = maincat.getChildren();
		while (biomeitr.hasNext()) {
			biome = biomeitr.next();
			biomeName = biomeRegister.getNameForObjectBypass(biome).toString();

			cfgIter = cfgBiomeCats.iterator();
			while (cfgIter.hasNext()) {
				biomeCat = cfgIter.next();
				if (biomeCat.getName().equals(biomeName)) {
					break;
				}
			}
			if (biomeCat == null || (!biomeCat.getName().equals(biomeName))) {
				BiomeHeightTweaker.log(Level.ERROR,
						"Biome " + biomeName + " found on gui configure, but not postinit -- skipping");
			} else {
				lst.add(biomeCat);
			}
		}
		return lst;
	}
}

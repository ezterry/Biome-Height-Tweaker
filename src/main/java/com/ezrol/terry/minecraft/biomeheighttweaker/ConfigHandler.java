package com.ezrol.terry.minecraft.biomeheighttweaker;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class ConfigHandler {
    // field accessors
    private static Field fieldBaseHeight;
    private static Field fieldHeightVariation;
    private static Field fieldBiomeName;
    // the biomes
    private final Configuration cfg; // mod config
    /**
     * Class to load and maintain the configuration
     */
    @SuppressWarnings("CanBeFinal")
    private Map<String, BiomeHeightData> biomeDefaults; // default values for
    private boolean firstLoad; // true on initial load from cfg

    public ConfigHandler(File f) {
        /* just set the file to be used for the config */
        boolean deob;
        File confFile;

        confFile = f;
        cfg = new Configuration(confFile);
        firstLoad = true;
        biomeDefaults = new HashMap<>();

		/*
		 * We need to grant ourselves access to two private methods of
		 * BiomeGenBase
		 */
        try {
            Biome.class.getDeclaredField("baseHeight");
            deob = true;
        } catch (NoSuchFieldException e) {
            deob = false;
        }
        try {
            if (deob) {
                fieldBaseHeight = Biome.class.getDeclaredField("baseHeight");
                fieldHeightVariation = Biome.class.getDeclaredField("heightVariation");
                fieldBiomeName = Biome.class.getDeclaredField("biomeName");
            } else {
                fieldBaseHeight = Biome.class.getDeclaredField("field_76748_D");
                fieldHeightVariation = Biome.class.getDeclaredField("field_76749_E");
                fieldBiomeName = Biome.class.getDeclaredField("field_76791_y");
            }
            fieldBaseHeight.setAccessible(true);
            fieldHeightVariation.setAccessible(true);
            fieldBiomeName.setAccessible(true);
        } catch (Exception e) {
            BiomeHeightTweaker.log(Level.FATAL, "Unable to find reflected class: ");
            BiomeHeightTweaker.log(Level.FATAL, e.toString());
            Field fields[] = Biome.class.getDeclaredFields();
            for (Field field : fields) {
                BiomeHeightTweaker.log(Level.INFO, field.getName());
            }
            throw new RuntimeException(e);
        }
    }

    private void updateBiome(String biomeName, Biome biome) {
        float cfgHeight;
        float cfgVariation;
        BiomeHeightData defaultData = biomeDefaults.get(biomeName);

        // Load Height
        cfgHeight = cfg.getFloat("baseheight", BiomeHeightTweaker.MODID + "." + biomeName, defaultData.getHeight(),
                Math.min(defaultData.getHeight(), (float) -2.5), Math.max(defaultData.getHeight(), (float) 4.5),
                "Biome Base Height", "biomeheighttweaker.config.biomeheight");

        // Set Comment on category
        String biomeComment;
        try {
            biomeComment = (String) fieldBiomeName.get(biome);
        } catch (IllegalAccessException e) {
            BiomeHeightTweaker.log(Level.WARN, "Unable to display biome name");
            biomeComment="UNKNOWN";
        }
        cfg.getCategory(BiomeHeightTweaker.MODID + "." + biomeName).setComment(biomeComment);

        // Get Variation
        cfgVariation = cfg.getFloat("heightvariation", BiomeHeightTweaker.MODID + "." + biomeName,
                defaultData.getVariation(), Math.min(defaultData.getVariation(), (float) 0.0),
                Math.max(defaultData.getVariation(), (float) 7.0), "Biome Height Variation",
                "biomeheighttweaker.config.biomevar");

        // Update base class
        if (!firstLoad || cfgHeight != biome.getBaseHeight()) {
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
        if (!firstLoad || cfgVariation != biome.getHeightVariation()) {
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
        // g get the cave configutation
        BiomeHeightTweaker.alt_caves = cfg.getBoolean("Enable Alternative Caves", BiomeHeightTweaker.MODID, false,
                "Large caves in DEFAULT terrain generation (and/or mods that use ChunkProviderOverworld)",
                "biomeheighttweaker.config.alt_caves");
        // "update" the configs to load the rest
        configUpdated();
        firstLoad = false;

    }

    public void configUpdated() {
        RegistryNamespaced<ResourceLocation, Biome> biomeRegister = Biome.REGISTRY;
        Iterator<Biome> biomeitr = biomeRegister.iterator();
        Biome biome;
        BiomeHeightData defaultData;
        String biomeName;

        // loop all registered biomes
        while (biomeitr.hasNext()) {
            biome = biomeitr.next();

            try {
                //noinspection ConstantConditions
                biomeName = biomeRegister.getNameForObject(biome).toString();
            } catch (NullPointerException e) {
                biomeName = "UNDEF";
            }

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
        List<Object> lst = new ArrayList<>();
        Property village;
        Property alt_caves;
        ConfigCategory maincat = cfg.getCategory(BiomeHeightTweaker.MODID);
        RegistryNamespaced<ResourceLocation, Biome> biomeRegister = Biome.REGISTRY;
        Iterator<Biome> biomeitr = biomeRegister.iterator();
        Biome biome;
        String biomeName;
        Set<ConfigCategory> cfgBiomeCats;
        Iterator<ConfigCategory> cfgIter;
        ConfigCategory biomeCat = null;

        village = maincat.get("Enable Ice Plains Village");
        village.setRequiresMcRestart(true);

        lst.add(village);

        alt_caves = maincat.get("Enable Alternative Caves");
        alt_caves.setRequiresMcRestart(true);

        lst.add(alt_caves);

        cfgBiomeCats = maincat.getChildren();
        while (biomeitr.hasNext()) {
            biome = biomeitr.next();

            try {
                //noinspection ConstantConditions
                biomeName = biomeRegister.getNameForObject(biome).toString();
            } catch (NullPointerException e) {
                biomeName = "UNDEF";
            }

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

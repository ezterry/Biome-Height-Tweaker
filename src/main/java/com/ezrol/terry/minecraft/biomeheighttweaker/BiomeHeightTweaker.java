package com.ezrol.terry.minecraft.biomeheighttweaker;

import net.minecraft.init.Biomes;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderOverworld;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Field;
import java.util.WeakHashMap;

@SuppressWarnings("WeakerAccess")
@Mod(modid = BiomeHeightTweaker.MODID, version = BiomeHeightTweaker.VERSION, guiFactory = "com.ezrol.terry.minecraft.biomeheighttweaker.GuiFactory", acceptableRemoteVersions = "*")
public class BiomeHeightTweaker {
    public static final String MODID = "biomeheighttweaker";
    public static final String VERSION = "${version}";
    public static boolean logging = false; // extra logging always keep false in
    // source control

    protected static ConfigHandler config;
    protected static boolean village = false;
    protected static boolean alt_caves = false;

    private WeakHashMap<ChunkProviderOverworld, Boolean> cavesOverride = null;
    private Field fieldCaveGenerator = null;

    /**
     * output to the "log"
     *
     * @param lvl     - level of the message
     * @param message - text to send
     */
    public static void log(Level lvl, String message) {
        if (lvl == Level.FATAL) {
            /* Major issue force enable logging for this and future messages */
            logging = true;
        }
        if (logging || lvl == Level.ERROR) {
            FMLLog.log(BiomeHeightTweaker.MODID, lvl, message);
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new ConfigHandler(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        config.postInit();
        MinecraftForge.EVENT_BUS.register(this);

        if (village) {
            MinecraftForge.TERRAIN_GEN_BUS.register(new SpruceVillages(Biomes.ICE_PLAINS));
            BiomeManager.addVillageBiome(Biomes.ICE_PLAINS, true);
        }
    }

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event) {
        String eventModId = event.getModID();
        log(Level.INFO, "config change" + eventModId);
        if (eventModId.equals(MODID)) {
            config.configUpdated();
        }
    }

    /**
     * Change the cave generator in use
     **/
    private void updateCaves(ChunkProviderOverworld provider) {
        if (alt_caves) {
            if (cavesOverride == null) {
                cavesOverride = new WeakHashMap<ChunkProviderOverworld, Boolean>();

                try {
                    try {
                        fieldCaveGenerator = ChunkProviderOverworld.class.getDeclaredField("caveGenerator");
                    } catch (NoSuchFieldException e) {
                        fieldCaveGenerator = ChunkProviderOverworld.class.getDeclaredField("field_186003_v");
                    }
                    fieldCaveGenerator.setAccessible(true);
                } catch (Exception e) {
                    BiomeHeightTweaker.log(Level.FATAL, "Unable to find reflected class: ");
                    BiomeHeightTweaker.log(Level.FATAL, e.toString());
                    throw new RuntimeException(e);
                }
            }
            if (fieldCaveGenerator == null) {
                return;
            }
            // now all one time init is done, and validated
            if (cavesOverride.containsKey(provider)) {
                return; // nop we already edited this class
            }
            // we need to inject the replacement cave provider
            MapGenBase cavegen = TerrainGen.getModdedMapGen(new CustomGenCaves(), InitMapGenEvent.EventType.CAVE);
            try {
                fieldCaveGenerator.set(provider, cavegen);
            } catch (Exception e) {
                BiomeHeightTweaker.log(Level.ERROR, "error injecting custom cave generator");
            }
            cavesOverride.put(provider, true);
        }
    }

    @SubscribeEvent
    public void LoadWorld(WorldEvent.Load event) {
        IChunkProvider prov = event.getWorld().getChunkProvider();
        log(Level.INFO, "In LoadWorld");
        log(Level.INFO, "test: " + prov.toString());
        if (prov instanceof ChunkProviderServer) {
            log(Level.INFO, "Got Chunk Provider Server");
            if (((ChunkProviderServer) prov).chunkGenerator instanceof ChunkProviderOverworld) {
                log(Level.INFO, "Init Overworld Chunk Provider");

                updateCaves((ChunkProviderOverworld) ((ChunkProviderServer) prov).chunkGenerator);
            }
        }
    }
}

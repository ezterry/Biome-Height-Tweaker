package com.ezrol.terry.minecraft.biomeheighttweaker;

import net.minecraft.init.Biomes;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
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

    private WeakHashMap<ChunkGeneratorOverworld, Boolean> cavesOverride = null;
    private Field fieldCaveGenerator = null;
    private static Logger ourLogger=null;


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
        if(ourLogger == null){
            return;
        }
        if (logging || lvl == Level.ERROR) {
            ourLogger.log(lvl, message);
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ourLogger = event.getModLog();
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
    private void updateCaves(ChunkGeneratorOverworld provider) {
        if (alt_caves) {
            if (cavesOverride == null) {
                cavesOverride = new WeakHashMap<>();

                try {
                    try {
                        fieldCaveGenerator = ChunkGeneratorOverworld.class.getDeclaredField("caveGenerator");
                    } catch (NoSuchFieldException e) {
                        fieldCaveGenerator = ChunkGeneratorOverworld.class.getDeclaredField("field_186003_v");
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

    private void updateCavesSponge(ChunkProviderServer spongeProv) {
        Boolean origLogging = logging;
        logging = true;
        log(Level.INFO, "Sponge Chunk provider found, Enable logging for cave generator swap");
        try {
            Class spongeChunkGen = Class.forName("org.spongepowered.common.world.gen.SpongeChunkGenerator");

            Field genpop = spongeChunkGen.getDeclaredField("genpop");
            genpop.setAccessible(true);


            //Object gen = baseGenerator.get(((ChunkProviderServer) prov).chunkGenerator);
            @SuppressWarnings("unchecked") List<Object> pop = (List<Object>) genpop.get(spongeProv.chunkGenerator);
            Iterator<Object> i = pop.iterator();
            int idx = 0;
            int injectPoint = -1;

            while (i.hasNext()) {
                Object x = i.next();
                if (x.getClass().getName().equals("net.minecraft.world.gen.MapGenCaves")) {
                    //original cave generator
                    log(Level.INFO, "Found MapGenCaves ref in sponge populators, removing");
                    i.remove();
                    injectPoint = idx;
                }
                idx += 1;
            }
            if (injectPoint >= 0) {
                log(Level.INFO, "Adding custom caves @ index " + injectPoint);
                MapGenBase cavegen = TerrainGen.getModdedMapGen(new CustomGenCaves(), InitMapGenEvent.EventType.CAVE);
                pop.add(injectPoint, cavegen);
            }
        } catch (ClassNotFoundException e) {
            log(Level.ERROR, "Unable to get class SpongeChunkGenerator to inject cavegen");
        } catch (NoSuchFieldException e) {
            log(Level.ERROR, "Unable to get field genpop, will not install custom cave gen");
        } catch (IllegalAccessException e) {
            log(Level.ERROR, "can't access genpop, custom cave generator not installed");
        }
        logging = origLogging;
        log(Level.INFO, "Cave injection done, logging level restored");
    }

    @SubscribeEvent
    public void LoadWorld(WorldEvent.Load event) {
        IChunkProvider prov = event.getWorld().getChunkProvider();
        log(Level.INFO, "In LoadWorld");
        log(Level.INFO, "test: " + prov.toString());
        if (prov instanceof ChunkProviderServer) {
            log(Level.INFO, "Got Chunk Provider Server");
            if (((ChunkProviderServer) prov).chunkGenerator instanceof ChunkGeneratorOverworld) {
                log(Level.INFO, "Init Overworld Chunk Provider");

                updateCaves((ChunkGeneratorOverworld) ((ChunkProviderServer) prov).chunkGenerator);
            } else {
                String chunkGenerator = ((ChunkProviderServer) prov).chunkGenerator.getClass().getName();
                if (chunkGenerator.equals("org.spongepowered.mod.world.gen.SpongeChunkGeneratorForge")) {
                    if (alt_caves) {
                        updateCavesSponge(((ChunkProviderServer) prov));
                    }
                } else {
                    log(Level.INFO, "chunk generator = " + chunkGenerator);
                }
            }
        }
    }
}

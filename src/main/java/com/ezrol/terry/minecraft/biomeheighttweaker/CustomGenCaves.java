package com.ezrol.terry.minecraft.biomeheighttweaker;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenCaves;

//note most of the code from this class is adjusted from the code in MapGenCaves so re-use with caution as it may
//not be as open source as the rest of the codebase.

@SuppressWarnings("WeakerAccess")
public class CustomGenCaves extends MapGenCaves {

    protected void addLargeRoom(long p_180703_1_, int p_180703_3_, int p_180703_4_, ChunkPrimer p_180703_5_,
                                double p_180703_6_, double p_180703_8_, double p_180703_10_) {
        // larger rooms (may not always be larger but on average)
        this.addTunnel(p_180703_1_, p_180703_3_, p_180703_4_, p_180703_5_, p_180703_6_, p_180703_8_, p_180703_10_,
                4.0F + this.rand.nextFloat() * 7.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    protected void oddRooms(long p_180703_1_, int p_180703_3_, int p_180703_4_, ChunkPrimer p_180703_5_,
                            double p_180703_6_, double p_180703_8_, double p_180703_10_) {
        // larger rooms (may not always be larger but on average)
        this.addTunnel(p_180703_1_, p_180703_3_, p_180703_4_, p_180703_5_, p_180703_6_, p_180703_8_, p_180703_10_,
                1.0F + this.rand.nextFloat() * 9.0F, 0.0F, 0.0F, -1, -1, 0.5D + (this.rand.nextFloat() * 0.5));
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int p_180701_4_, int p_180701_5_,
                                     ChunkPrimer chunkPrimerIn) {
        // larger chains (15-20)
        int i = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(20) + 1) + 1);

        if (this.rand.nextInt(5) != 0) {
            i = 0;
        }

        for (int j = 0; j < i; ++j) {
            double d0 = (double) (chunkX * 16 + this.rand.nextInt(16));
            double d1 = (double) this.rand.nextInt(this.rand.nextInt(120) + 8);
            double d2 = (double) (chunkZ * 16 + this.rand.nextInt(16));
            int k = 1;

            if (this.rand.nextInt(5) == 0) {
                // normal small rooms
                this.addRoom(this.rand.nextLong(), p_180701_4_, p_180701_5_, chunkPrimerIn, d0, d1, d2);
                k += this.rand.nextInt(4);
            } else if (this.rand.nextInt(5) == 0) {
                this.addLargeRoom(this.rand.nextLong(), p_180701_4_, p_180701_5_, chunkPrimerIn, d0, d1, d2);
                k += this.rand.nextInt(6);
            } else if (this.rand.nextInt(10) == 0) {
                this.oddRooms(this.rand.nextLong(), p_180701_4_, p_180701_5_, chunkPrimerIn, d0, d1, d2);
                k += this.rand.nextInt(4);
            } else if (this.rand.nextInt(10) == 0) {
                // fork in the tunnel
                k++;
            }

            for (int l = 0; l < k; ++l) {
                float f = this.rand.nextFloat() * ((float) Math.PI * 2F);
                float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                // caves on average 2 larger
                float f2 = this.rand.nextFloat() * 4.0F + this.rand.nextFloat();

                // but can be much larger more frequent
                if (this.rand.nextInt(7) == 0) {
                    f2 *= this.rand.nextFloat() * this.rand.nextFloat() * 6.0F + 1.0F;
                }

                this.addTunnel(this.rand.nextLong(), p_180701_4_, p_180701_5_, chunkPrimerIn, d0, d1, d2, f2, f, f1, 0,
                        0, 1.0D);
            }
        }
    }
}

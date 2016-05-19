package com.ezrol.terry.minecraft.biomeheighttweaker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * @author ezterry
 *
 *         Class to spruce blocks to convert a village to spruce
 * 
 *         Tested with seed: 5379835038257694226 x=8882 z=7394
 */

public class SpruceVillages {
	private Biome check;
	private Block spruce_stairs;

	public SpruceVillages(Biome c) {
		check = c;
		spruce_stairs = Blocks.SPRUCE_STAIRS;
	}

	@SubscribeEvent
	public void getVillageBlockID(BiomeEvent.GetVillageBlockID event) {
		if (event.getBiome() == check) {
			IBlockState original = event.getOriginal();
			Block originalBlock = original.getBlock();

			if (originalBlock == Blocks.OAK_STAIRS) {
				event.setReplacement(spruce_stairs.getDefaultState().withProperty(BlockStairs.FACING,
						original.getValue(BlockStairs.FACING)));
				event.setResult(Result.DENY);
				return;
			}
			if (originalBlock == Blocks.GRAVEL) {
				event.setReplacement(Blocks.PACKED_ICE.getDefaultState());
				event.setResult(Result.DENY);
				return;
			}
			if (originalBlock == Blocks.LOG || originalBlock == Blocks.LOG2) {
				event.setReplacement(
						Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE));
				event.setResult(Result.DENY);
				return;
			}
			if (originalBlock == Blocks.PLANKS) {
				event.setReplacement(
						Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE));
				event.setResult(Result.DENY);
				return;
			}
			if (originalBlock == Blocks.OAK_FENCE) {
				event.setReplacement(Blocks.SPRUCE_FENCE.getDefaultState());
				event.setResult(Result.DENY);
				return;
			}
		}
	}
}

package com.ezrol.terry.minecraft.biomeheighttweaker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * @author ezterry
 *
 *         Class to spruce blocks to convert a village to spruce
 * 
 *         Tested with seed: 5379835038257694226 x=8882 y=7394
 */

public class SpruceVillages {
	private BiomeGenBase check;
	private Block spruce_stairs;

	public SpruceVillages(BiomeGenBase c) {
		check = c;
		spruce_stairs = Blocks.spruce_stairs;
	}

	@SubscribeEvent
	public void getVillageBlockID(BiomeEvent.GetVillageBlockID event) {
		if (event.getBiome() == check) {
			IBlockState original = event.getOriginal();
			Block originalBlock = original.getBlock();

			if (originalBlock == Blocks.oak_stairs) {
				event.setReplacement(spruce_stairs.getDefaultState().withProperty(BlockStairs.FACING,
						original.getValue(BlockStairs.FACING)));
				event.setResult(Result.DENY);
				return;
			}
			if (originalBlock == Blocks.gravel) {
				event.setReplacement(Blocks.packed_ice.getDefaultState());
				event.setResult(Result.DENY);
				return;
			}
			if (originalBlock == Blocks.log || originalBlock == Blocks.log2) {
				event.setReplacement(
						Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE));
				event.setResult(Result.DENY);
				return;
			}
			if (originalBlock == Blocks.planks) {
				event.setReplacement(
						Blocks.planks.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE));
				event.setResult(Result.DENY);
				return;
			}
			if (originalBlock == Blocks.oak_fence) {
				event.setReplacement(Blocks.spruce_fence.getDefaultState());
				event.setResult(Result.DENY);
				return;
			}
		}
	}
}

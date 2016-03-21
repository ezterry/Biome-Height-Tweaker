package com.ezrol.terry.minecraft.biomeheighttweaker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStairs;
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
		if (event.biome == check) {
			Block originalblock = event.original.getBlock();
			if (originalblock == Blocks.oak_stairs) {
				event.replacement = spruce_stairs.getDefaultState().withProperty(BlockStairs.FACING,
						event.original.getValue(BlockStairs.FACING));
				event.setResult(Result.DENY);
				return;
			}
			if (originalblock == Blocks.gravel) {
				event.replacement = Blocks.packed_ice.getDefaultState();
				event.setResult(Result.DENY);
				return;
			}
			if (originalblock == Blocks.log || originalblock == Blocks.log2) {
				event.replacement = Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT,
						BlockPlanks.EnumType.SPRUCE);
				event.setResult(Result.DENY);
				return;
			}
			if (originalblock == Blocks.planks) {
				event.replacement = Blocks.planks.getDefaultState().withProperty(BlockPlanks.VARIANT,
						BlockPlanks.EnumType.SPRUCE);
				event.setResult(Result.DENY);
				return;
			}
			if (originalblock == Blocks.oak_fence) {
				event.replacement = Blocks.spruce_fence.getDefaultState();
				event.setResult(Result.DENY);
				return;
			}
		}
	}
}

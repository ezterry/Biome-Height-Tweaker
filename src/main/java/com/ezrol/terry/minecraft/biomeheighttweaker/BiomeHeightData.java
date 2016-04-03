package com.ezrol.terry.minecraft.biomeheighttweaker;

public class BiomeHeightData {
	/**
	 * store the default height and variation of a particular biome
	 */
	private float height;
	private float variation;
	private String name;

	public BiomeHeightData(String name, float height, float variation) {
		this.name = name;
		this.height = height;
		this.variation = variation;
	}

	public float getHeight() {
		return height;
	}

	public float getVariation() {
		return variation;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return ("{" + name + ": h: " + height + " v: " + variation + "}");
	}
}

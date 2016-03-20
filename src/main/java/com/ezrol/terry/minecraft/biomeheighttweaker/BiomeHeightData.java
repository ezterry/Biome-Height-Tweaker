package com.ezrol.terry.minecraft.biomeheighttweaker;

public class BiomeHeightData {
	/**
	 * store the hight and variation of a particular biome
	 */
	private float height;
	private float variation;
	private String name;
	
	public BiomeHeightData(String name){
		this.name = name;
		this.height = 0.0f;
		this.variation = 0.0f;
	}
	public BiomeHeightData(String name,float height,float variation){
		this.name = name;
		this.height = height;
		this.variation = variation;
	}
	
	public float getHeight(){
		return height;
	}
	public float getVariation(){
		return variation;
	}
	public void update(float height,float variation){
		this.height = height;
		this.variation = variation;
	}
	public void setHeight(float height){
		this.height = height;
	}
	public void setVariation(float variation){
		this.variation = variation;
	}
	
	public String getName(){
		return name;
	}
	
	public String toString(){
		return("{" + name + ": h: " + height + " v: " + variation + "}");
	}
}

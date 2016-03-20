package com.ezrol.terry.minecraft.biomeheighttweaker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Level;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ButtonEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.NumberSliderEntry;
import net.minecraftforge.fml.client.config.IConfigElement;

public class ConfigGui extends GuiConfig{
	public ConfigGui(GuiScreen parent){
		super(parent,initList(), BiomeHeightTweaker.MODID,true,false,"Biome Height Tweaker Configuration");
	}
	public static List<IConfigElement> initList(){
		ConfigHandler config=BiomeHeightTweaker.config;
		List<IConfigElement> lst = new ArrayList<IConfigElement>();
		Iterator<Object> prop=config.getGuiPropList().iterator();
		Object obj;
		int index=0;
		NumberSliderEntry foo;
		
		while(prop.hasNext()){
			obj=prop.next();
			if(obj instanceof Property){
				lst.add(new ConfigElement((Property)obj));
			}
			else if(obj instanceof ConfigCategory){
				Iterator<Property> nodes=((ConfigCategory)obj).getOrderedValues().iterator();
				Property node;
				while(nodes.hasNext()){
					node=nodes.next();
					node.setConfigEntryClass(FloatNumberSlider.class);
					
				}
				
				ConfigElement biome = new ConfigElement((ConfigCategory)obj);
				lst.add(biome);
			}
			else{
				BiomeHeightTweaker.log(Level.ERROR,"Bad config property type!");
			}
			index++;
		}
		return lst;
	}
	/**
     * NumberSliderEntry
     *
     * Provides a slider for numeric properties.
     * with some optimization for floats (still using double behind the scene but with formatting it to 6 decimal places some
     * odd rounding is removed to prevent unexpected changes
     */
    public static class FloatNumberSlider extends ButtonEntry
    {
        protected final double beforeValue;

        public FloatNumberSlider(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
        {
            super(owningScreen, owningEntryList, configElement, new GuiSlider(0, owningEntryList.controlX, 0, owningEntryList.controlWidth, 18,
                    "", "", Double.valueOf(configElement.getMinValue().toString()), Double.valueOf(configElement.getMaxValue().toString()),
                    Double.valueOf(configElement.get().toString()), true, true));

            this.beforeValue = Float.valueOf(configElement.get().toString());
        }

        @Override
        public void updateValueButtonText()
        {
        	String s = String.format("%.6g", ((GuiSlider) this.btnValue).getValue());
        	((GuiSlider) this.btnValue).setValue(Double.valueOf(s));
            ((GuiSlider) this.btnValue).updateSlider();
        }

        @Override
        public void valueButtonPressed(int slotIndex) {}

        @Override
        public boolean isDefault()
        {
        	return String.format("%.6g", ((GuiSlider) this.btnValue).getValue()).equals(String.format("%.6g",  Double.valueOf(configElement.getDefault().toString())));
        }

        @Override
        public void setToDefault()
        {
            if (this.enabled())
            {
                ((GuiSlider) this.btnValue).setValue(Double.valueOf(configElement.getDefault().toString()));
                ((GuiSlider) this.btnValue).updateSlider();
            }
        }

        @Override
        public boolean isChanged()
        {
            return ! String.format("%.6g", ((GuiSlider) this.btnValue).getValue()).equals(String.format("%.6g", beforeValue));
        }

        @Override
        public void undoChanges()
        {
            if (this.enabled())
            {
                ((GuiSlider) this.btnValue).setValue(beforeValue);
                ((GuiSlider) this.btnValue).updateSlider();
            }
        }

        @Override
        public boolean saveConfigElement()
        {
            if (this.enabled() && this.isChanged())
            {
                configElement.set(Float.valueOf(String.format("%.6g", ((GuiSlider) this.btnValue).getValue())));
                return configElement.requiresMcRestart();
            }
            return false;
        }

        @Override
        public Object getCurrentValue()
        {
            return String.format("%.6g", ((GuiSlider) this.btnValue).getValue());
        }

        @Override
        public Object[] getCurrentValues()
        {
            return new Object[] { getCurrentValue() };
        }
    }


}

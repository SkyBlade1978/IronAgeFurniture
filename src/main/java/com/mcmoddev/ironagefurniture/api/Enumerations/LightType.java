package com.mcmoddev.ironagefurniture.api.Enumerations;

import net.minecraft.util.IStringSerializable;

public enum LightType implements IStringSerializable {
	 SCONCE("sconce"),
     CANDLESTICK("candlestick");
	
     private final String name;

     private LightType(String name)
     {
         this.name = name;
     }

     public String toString()
     {
         return this.name;
     }

     public String getName()
     {
         return this.name;
     }
}

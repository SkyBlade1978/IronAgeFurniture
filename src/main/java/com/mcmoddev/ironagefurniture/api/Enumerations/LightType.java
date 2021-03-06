package com.mcmoddev.ironagefurniture.api.Enumerations;

import net.minecraft.util.IStringSerializable;

public enum LightType implements IStringSerializable {
	 TORCH("torch"),
     CANDLE("candle"),
     GLOWSTONE("glowstone"),
     CELESTIAL("celestial");
	
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

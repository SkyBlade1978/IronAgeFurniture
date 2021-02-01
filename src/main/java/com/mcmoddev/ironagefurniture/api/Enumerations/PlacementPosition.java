package com.mcmoddev.ironagefurniture.api.Enumerations;

import net.minecraft.util.IStringSerializable;

public enum PlacementPosition implements IStringSerializable {
	 WALL("wall"),
     FLOOR("floor");
	
     private final String name;

     private PlacementPosition(String name)
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

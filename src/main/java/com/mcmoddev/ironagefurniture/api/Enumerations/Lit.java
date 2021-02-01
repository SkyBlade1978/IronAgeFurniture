package com.mcmoddev.ironagefurniture.api.Enumerations;

import net.minecraft.util.IStringSerializable;

public enum Lit implements IStringSerializable {
	 LIT("lit"),
     UNLIT("unlit");
	
     private final String name;

     private Lit(String name)
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

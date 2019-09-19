package com.mcmoddev.ironagefurniture;

import java.util.HashMap;
import java.util.Map;

import com.mcmoddev.ironagefurniture.init.BlockInitialiser;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Ironagefurniture.MODID, version = Ironagefurniture.VERSION)
public class Ironagefurniture
{
    public static final String MODID = "ironagefurniture";
    public static final String VERSION = "0.0.1";
    
	public static final Map<String,Block> BlockRegistry = new HashMap<String, Block>();
	public static final Map<String,Item> ItemRegistry = new HashMap<String, Item>();
    
    public static CreativeTabs ironagefurnitureTab = new CreativeTabs("ironagefurnitureTab"){
		@Override
		public Item getTabIconItem(){
			return Item.getItemFromBlock(Blocks.BOOKSHELF);
		}
		
		public boolean hasSearchBar() {
			return true;
		};
	};
    
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        System.out.println("DIRT BLOCK >> "+Blocks.DIRT.getUnlocalizedName());
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	BlockInitialiser.init();   	
    }
}
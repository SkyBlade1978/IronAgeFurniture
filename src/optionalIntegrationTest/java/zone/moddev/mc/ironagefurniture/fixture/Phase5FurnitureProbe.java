package zone.moddev.mc.ironagefurniture.fixture;

import java.util.Set;

import zone.moddev.mc.ironagefurniture.api.Blocks.Cabinet;
import zone.moddev.mc.ironagefurniture.api.Blocks.DiningTable;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Category;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSetting.Slot;
import zone.moddev.mc.ironagefurniture.api.surface.SurfaceSettingHost;
import zone.moddev.mc.ironagefurniture.api.surface.VanillaSurfaceFoods;
import zone.moddev.mc.ironagefurniture.api.tile.TileEntityCabinet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;

final class Phase5FurnitureProbe {
    private Phase5FurnitureProbe() { }

    static void verify(WorldServer world, Set<String> mods) {
        Block table = block("table_dining_oak");
        Block cabinet = block("cabinet_wood_ironage_oak");
        Block blocker = block("surface_display_blocker");
        require(Item.getItemFromBlock(blocker) == null, "Surface blocker has an item");
        verifyRecipes(world, table, cabinet);

        FakePlayer player = FakePlayerFactory.getMinecraft(world);
        player.inventory.clear();
        BlockPos tablePos = new BlockPos(1, 101, 1);
        BlockPos cabinetPos = new BlockPos(1, 101, 4);
        world.setBlockState(tablePos.down(), Blocks.STONE.getDefaultState());
        world.setBlockState(tablePos.east().down(), Blocks.STONE.getDefaultState());
        world.setBlockState(tablePos, table.getDefaultState());
        world.setBlockState(tablePos.east(), table.getDefaultState());
        require(connections(world, tablePos) == 2 && connections(world, tablePos.east()) == 8,
                "Dining tables did not join east/west");
        world.setBlockToAir(tablePos.east());
        require(connections(world, tablePos) == 0, "Dining table did not separate");
        require(world.getTileEntity(tablePos) == null, "Bare table allocated a tile");

        player.setSneaking(true);
        IBlockState separate = table.getStateForPlacement(world, tablePos.east(), EnumFacing.EAST,
                0.5F, 0.5F, 0.5F, 0, player, new ItemStack(table));
        world.setBlockState(tablePos.east(), separate);
        table.onBlockPlacedBy(world, tablePos.east(), separate, player, new ItemStack(table));
        player.setSneaking(false);
        require(connections(world, tablePos) == 0 && connections(world, tablePos.east()) == 0,
                "Sneak-placement did not keep tables separate");
        world.setBlockToAir(tablePos.east());

        IBlockState north = cabinet.getDefaultState().withProperty(Cabinet.FACING, EnumFacing.NORTH);
        world.setBlockState(cabinetPos, north);
        world.setBlockState(cabinetPos.east(), north);
        cabinet.onBlockPlacedBy(world, cabinetPos.east(), north, player, new ItemStack(cabinet));
        require(world.getBlockState(cabinetPos).getValue(Cabinet.TYPE) != Cabinet.CabinetType.SINGLE,
                "Adjacent cabinets did not pair");
        world.setBlockToAir(cabinetPos.east());
        require(world.getBlockState(cabinetPos).getValue(Cabinet.TYPE) == Cabinet.CabinetType.SINGLE,
                "Remaining cabinet did not separate");
        world.setBlockState(cabinetPos.up(), north);
        cabinet.onBlockPlacedBy(world, cabinetPos.up(), north, player, new ItemStack(cabinet));
        require(cabinet.getActualState(north, world, cabinetPos).getValue(Cabinet.VERTICAL)
                == Cabinet.VerticalCabinetType.LOWER, "Stacked cabinets did not pair");
        world.setBlockToAir(cabinetPos.up());

        TileEntityCabinet cabinetTile = (TileEntityCabinet)world.getTileEntity(cabinetPos);
        IItemHandler front = cabinetTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH);
        IItemHandler top = cabinetTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        ItemStack stored = tagged(new ItemStack(Items.IRON_INGOT, 7), "Inside cabinet");
        require(front.insertItem(0, stored.copy(), true) != null, "Cabinet accepted front insertion");
        require(top.getSlots() == 27 && top.insertItem(0, stored.copy(), false) == null,
                "Cabinet top automation did not accept inventory");
        require(ItemStack.areItemStacksEqual(top.getStackInSlot(0), stored),
                "Cabinet storage did not retain exact item");
        require(ItemStack.areItemStacksEqual(((IInventory)roundTrip(cabinetTile)).getStackInSlot(0), stored),
                "Cabinet inventory did not survive NBT");

        ItemStack display = tagged(new ItemStack(Items.APPLE), "On the surface");
        world.setBlockState(tablePos.up(), Blocks.STONE.getDefaultState());
        click(world, tablePos, player, display.copy());
        require(world.getTileEntity(tablePos) == null && world.getBlockState(tablePos.up()).getBlock() == Blocks.STONE,
                "A table display replaced an occupied cell");
        world.setBlockToAir(tablePos.up());

        verifyDisplay(world, tablePos, blocker, player, display);
        for (VanillaSurfaceFoods.Food food : VanillaSurfaceFoods.all()) {
            ItemStack item = new ItemStack(food.item, 1, Math.max(0, food.metadata));
            require(SurfaceSetting.classify(item) == Category.MEAL, "Vanilla food is not a place setting");
            verifyDisplay(world, tablePos, blocker, player, tagged(item, "Vanilla " + food.model));
        }
        verifyDisplay(world, tablePos, blocker, player,
                tagged(new ItemStack(Items.GOLDEN_APPLE, 1, 1), "Enchanted golden apple"));
        if (mods.contains("ironagebrewing")) {
            ItemStack drinkware = stack("ironagebrewing:drinkware");
            require(SurfaceSetting.classify(drinkware) == Category.DRINKWARE, "Brewing surface provider missing");
            verifyDisplay(world, tablePos, blocker, player, tagged(drinkware, "Drinkware"));
        }
        if (mods.contains("harvestcraft")) {
            ItemStack pie = stack("harvestcraft:applepieitem");
            require(SurfaceSetting.classify(pie) == Category.MEAL, "HarvestCraft surface provider missing");
            verifyDisplay(world, tablePos, blocker, player, tagged(pie, "HarvestCraft pie"));
        }
        require(click(world, cabinetPos, player, display.copy()), "Cabinet did not display item");
        require(ItemStack.areItemStacksEqual(top.getStackInSlot(0), stored),
                "Cabinet display changed storage");
        world.setBlockToAir(cabinetPos);
        require(world.isAirBlock(cabinetPos.up()), "Cabinet left a surface blocker behind");
        int displayDrops = 0;
        int storageDrops = 0;
        for (EntityItem drop : world.getEntitiesWithinAABB(EntityItem.class,
                new AxisAlignedBB(cabinetPos.add(-1, -1, -1), cabinetPos.add(2, 2, 2)))) {
            ItemStack stack = drop.getEntityItem();
            if (ItemStack.areItemStacksEqual(stack, display)) displayDrops++;
            if (ItemStack.areItemsEqual(stack, stored) && ItemStack.areItemStackTagsEqual(stack, stored)) {
                storageDrops += stack.stackSize;
            }
        }
        require(displayDrops == 1 && storageDrops == 7, "Cabinet break lost or duplicated tagged items");
        verifyDisplayBreaking(world, player, table, cabinet, mods);
        VanillaFoodSurfaceProbe.verify(world);
        LogManager.getLogger().info("IAF PHASE 5 FURNITURE PROBE PASSED");
    }

    private static void verifyDisplayBreaking(WorldServer world, FakePlayer player, Block table,
            Block cabinet, Set<String> mods) {
        BlockPos pos = new BlockPos(16, 101, 4);
        EntityPlayerMP serverPlayer = player;
        GameType previous = serverPlayer.interactionManager.getGameType();
        NetHandlerPlayServer previousConnection = serverPlayer.connection;
        serverPlayer.connection = new NetHandlerPlayServer(world.getMinecraftServer(),
                new NetworkManager(EnumPacketDirection.SERVERBOUND), serverPlayer) {
            @Override public void sendPacket(Packet<?> packet) { }
        };
        try {
            for (Block furniture : new Block[]{table, cabinet}) {
                for (GameType mode : new GameType[]{GameType.CREATIVE, GameType.SURVIVAL}) {
                    for (ItemStack stack : new ItemStack[]{new ItemStack(Items.ARMOR_STAND),
                            new ItemStack(Items.DIAMOND), new ItemStack(Blocks.STONE)}) {
                        world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
                        world.setBlockState(pos, furniture.getDefaultState());
                        serverPlayer.interactionManager.setGameType(mode);
                        ItemStack item = tagged(stack, "Display break " + pos + " " + mode);
                        require(click(world, pos, player, item.copy()), "Cannot place break-test display");
                        TileEntity tile = world.getTileEntity(pos);
                        SurfaceSettingHost surface = (SurfaceSettingHost)tile;
                        serverPlayer.setHeldItem(EnumHand.MAIN_HAND, null);
                        serverPlayer.setPositionAndRotation(pos.getX() + 0.5D, pos.getY() + 2.0D,
                                pos.getZ() + 0.5D, 0.0F, 90.0F);
                        require(serverPlayer.interactionManager.tryHarvestBlock(pos.up()), "Cannot mine display");
                        require(world.isAirBlock(pos.up()), "Broken display kept a blocker");
                        require(!surface.getSurfaceSetting().hasAnyItem(), "Broken display kept stored items");
                        require(world.getBlockState(pos).getBlock() == furniture, "Breaking display removed furniture");
                        if (world.getTileEntity(pos) != null) {
                            require(!((SurfaceSettingHost)roundTrip(world.getTileEntity(pos)))
                                    .getSurfaceSetting().hasAnyItem(), "Broken display returned after NBT reload");
                        }
                        require(countDrops(world, pos.up(), item) == (mode == GameType.CREATIVE ? 0 : 1),
                                "Display break lost or duplicated its exact item");
                        pos = pos.east(4);
                    }
                }
                world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
                world.setBlockState(pos, furniture.getDefaultState());
                ItemStack item = tagged(new ItemStack(Items.ARMOR_STAND), "External removal " + pos);
                require(click(world, pos, player, item.copy()), "Cannot place external-removal display");
                SurfaceSettingHost surface = (SurfaceSettingHost)world.getTileEntity(pos);
                world.setBlockToAir(pos.up());
                require(!surface.getSurfaceSetting().hasAnyItem(), "External removal left a visible item");
                require(countDrops(world, pos.up(), item) == 1, "External removal lost or duplicated item");
                require(world.getBlockState(pos).getBlock() == furniture, "External removal broke furniture");
                pos = pos.east(4);
                world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
                world.setBlockState(pos, furniture.getDefaultState());
                IBlockState state = furniture.getDefaultState();
                if (furniture instanceof DiningTable) state = state.withProperty(DiningTable.DATA, true);
                world.setBlockState(pos, state, 2);
                TileEntity tile = world.getTileEntity(pos);
                SurfaceSettingHost orphan = (SurfaceSettingHost)tile;
                ItemStack savedItem = tagged(new ItemStack(Items.ARMOR_STAND), "Reload orphan " + pos);
                orphan.getSurfaceSetting().setSingleItem(savedItem, EnumFacing.NORTH);
                world.setTileEntity(pos, roundTrip(tile));
                TileEntity reloaded = world.getTileEntity(pos);
                reloaded.onLoad();
                FMLCommonHandler.instance().bus().post(
                        new TickEvent.WorldTickEvent(Side.SERVER, TickEvent.Phase.END, world));
                require(world.getBlockState(pos.up()).getBlock() == block("surface_display_blocker"),
                        "Reload did not restore the missing display blocker");
                require(ItemStack.areItemStacksEqual(
                        ((SurfaceSettingHost)reloaded).getSurfaceSetting().getCompatibilityItem(), savedItem),
                        "Orphan repair changed the stored item");
                require(countDrops(world, pos.up(), savedItem) == 0, "Orphan repair duplicated the stored item");
                pos = pos.east(4);
            }
            if (mods.contains("ironagebrewing")) {
                for (GameType mode : new GameType[]{GameType.CREATIVE, GameType.SURVIVAL}) {
                    world.setBlockState(pos, cabinet.getDefaultState());
                    serverPlayer.interactionManager.setGameType(mode);
                    ItemStack cup = tagged(stack("ironagebrewing:drinkware"), "Separate cups " + mode);
                    require(click(world, pos, player, cup.copy()), "Cannot place first cup");
                    SurfaceSettingHost host = (SurfaceSettingHost)world.getTileEntity(pos);
                    SurfaceSetting setting = host.getSurfaceSetting();
                    require(setting.insert(cup, EnumFacing.NORTH, 0.75F, 0.5F, EnumFacing.Axis.X),
                            "Cannot place second cup");
                    host.markSurfaceSettingChanged();
                    serverPlayer.setHeldItem(EnumHand.MAIN_HAND, null);
                    for (Slot slot : new Slot[]{Slot.GUEST_A, Slot.GUEST_B}) {
                        require(setting.getItem(slot) != null, "Expected cup missing before mining");
                        serverPlayer.setPositionAndRotation(pos.getX() + setting.getX(slot), pos.getY() + 2.0D,
                                pos.getZ() + setting.getZ(slot), 0.0F, 90.0F);
                        require(serverPlayer.interactionManager.tryHarvestBlock(pos.up()), "Cannot mine cup");
                        require(setting.getItem(slot) == null, "Wrong cup removed");
                        require(setting.getItemCount() == (slot == Slot.GUEST_A ? 1 : 0),
                                "Cup mining removed another displayed item");
                    }
                    require(world.isAirBlock(pos.up()), "Last cup left its blocker");
                    require(world.getBlockState(pos).getBlock() == cabinet, "Cup mining removed cabinet");
                    require(countDrops(world, pos.up(), cup) == (mode == GameType.CREATIVE ? 0 : 2),
                            "Cup mining lost or duplicated items");
                    pos = pos.east(4);
                }
                serverPlayer.interactionManager.setGameType(GameType.SURVIVAL);
                for (Block furniture : new Block[]{table, cabinet}) {
                    for (Item food : new Item[]{Items.BREAD, Items.APPLE}) {
                        world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
                        world.setBlockState(pos, furniture.getDefaultState());
                        ItemStack meal = tagged(new ItemStack(food), "Meal with drink " + pos);
                        ItemStack cup = tagged(stack("ironagebrewing:drinkware"), "Drink with meal " + pos);
                        require(click(world, pos, player, meal.copy()), "Cannot place meal beside drink");
                        serverPlayer.setHeldItem(EnumHand.MAIN_HAND, cup.copy());
                        require(furniture.onBlockActivated(world, pos, world.getBlockState(pos), player,
                                EnumHand.MAIN_HAND, serverPlayer.getHeldItemMainhand(), EnumFacing.UP,
                                0.25F, 1.0F, 0.5F), "Drink placement rejected");
                        SurfaceSetting setting = ((SurfaceSettingHost)world.getTileEntity(pos)).getSurfaceSetting();
                        require(setting.getItemCount() == 2, "Food and drink could not share a surface");
                        world.setBlockToAir(pos);
                        require(countDrops(world, pos.up(), meal) == 1 && countDrops(world, pos.up(), cup) == 1,
                                "Food and drink combination lost or duplicated items");
                        pos = pos.east(4);
                    }
                }
            }
        } finally {
            serverPlayer.interactionManager.setGameType(previous);
            serverPlayer.connection = previousConnection;
        }
        LogManager.getLogger().info("IAF SURFACE REMOVAL PROBE PASSED");
    }

    private static int countDrops(WorldServer world, BlockPos pos, ItemStack expected) {
        int count = 0;
        for (EntityItem entity : world.getEntitiesWithinAABB(EntityItem.class,
                new AxisAlignedBB(pos.add(-1, -1, -1), pos.add(2, 2, 2)))) {
            ItemStack item = entity.getEntityItem();
            if (ItemStack.areItemsEqual(item, expected) && ItemStack.areItemStackTagsEqual(item, expected)) {
                count += item.stackSize;
            }
        }
        return count;
    }

    private static void verifyRecipes(WorldServer world, Block table, Block cabinet) {
        InventoryCrafting grid = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer player) { return false; }
        }, 3, 3);
        for (int i = 0; i < 3; i++) grid.setInventorySlotContents(i, new ItemStack(Blocks.WOODEN_SLAB, 1, 0));
        grid.setInventorySlotContents(3, new ItemStack(Items.STICK));
        grid.setInventorySlotContents(5, new ItemStack(Items.STICK));
        require(output(world, grid) == Item.getItemFromBlock(table), "Oak dining table recipe missing");
        grid.setInventorySlotContents(0, new ItemStack(Blocks.WOODEN_SLAB, 1, 1));
        require(output(world, grid) != Item.getItemFromBlock(table), "Dining table accepted non-oak slab");
        grid.clear();
        grid.setInventorySlotContents(0, new ItemStack(Blocks.CHEST));
        grid.setInventorySlotContents(4, new ItemStack(Blocks.PLANKS, 1, 0));
        grid.setInventorySlotContents(8, OreDictionary.getOres("nuggetIron").isEmpty()
                ? new ItemStack(Items.IRON_INGOT) : OreDictionary.getOres("nuggetIron").get(0).copy());
        require(output(world, grid) == Item.getItemFromBlock(cabinet), "Oak cabinet recipe missing");
    }

    private static Item output(WorldServer world, InventoryCrafting grid) {
        ItemStack output = CraftingManager.getInstance().findMatchingRecipe(grid, world);
        return output == null ? null : output.getItem();
    }

    private static void verifyDisplay(WorldServer world, BlockPos pos, Block blocker, FakePlayer player, ItemStack item) {
        require(click(world, pos, player, item.copy()), "Table display interaction was rejected");
        require(world.getBlockState(pos.up()).getBlock() == blocker && player.getHeldItemMainhand() == null,
                "Table did not reserve display space or consume exactly one item");
        SurfaceSetting setting = ((SurfaceSettingHost)roundTrip(world.getTileEntity(pos))).getSurfaceSetting();
        require(ItemStack.areItemStacksEqual(setting.getCompatibilityItem(), item), "Display lost stack NBT");
        if (SurfaceSetting.classify(item) == Category.MEAL) {
            require(setting.getItem(Slot.CENTER) != null, "A single meal should sit in the centre");
        }
        Slot occupied = null;
        for (Slot slot : Slot.values()) {
            if (setting.getItem(slot) != null) occupied = slot;
        }
        require(occupied != null, "Surface slot missing");
        float x = (float)setting.getX(occupied);
        float z = (float)setting.getZ(occupied);
        IBlockState state = world.getBlockState(pos.up());
        require(blocker.onBlockActivated(world, pos.up(), state, player, EnumHand.MAIN_HAND,
                null, EnumFacing.UP, x, 0.5F, z), "Displayed item could not be retrieved");
        require(world.isAirBlock(pos.up()) && world.getTileEntity(pos) == null,
                "Empty display retained blocker or table tile");
        require(ItemStack.areItemStacksEqual(player.inventory.getStackInSlot(0), item),
                "Retrieval did not return the original stack");
        player.inventory.clear();
    }

    private static boolean click(WorldServer world, BlockPos pos, FakePlayer player, ItemStack item) {
        player.setHeldItem(EnumHand.MAIN_HAND, item);
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND,
                item, EnumFacing.UP, 0.5F, 1.0F, 0.5F);
    }

    private static int connections(WorldServer world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().getActualState(state, world, pos).getValue(DiningTable.CONNECTIONS);
    }

    private static TileEntity roundTrip(TileEntity tile) {
        return TileEntity.create(tile.getWorld(), tile.writeToNBT(new NBTTagCompound()));
    }

    private static ItemStack tagged(ItemStack item, String name) {
        return item.setStackDisplayName(name);
    }

    private static Block block(String name) {
        Block block = Block.getBlockFromName("ironagefurniture:" + name);
        require(block != null && block != Blocks.AIR, "Missing " + name);
        return block;
    }

    private static ItemStack stack(String name) {
        Item item = Item.getByNameOrId(name);
        require(item != null, "Missing " + name);
        return new ItemStack(item);
    }

    private static void require(boolean pass, String message) {
        if (!pass) throw new IllegalStateException(message);
    }
}

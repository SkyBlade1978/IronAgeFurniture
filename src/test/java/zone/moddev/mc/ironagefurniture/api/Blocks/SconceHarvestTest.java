package zone.moddev.mc.ironagefurniture.api.Blocks;

import static org.junit.Assert.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import org.junit.BeforeClass;
import org.junit.Test;

public class SconceHarvestTest {
    @BeforeClass public static void bootstrap() {
        Bootstrap.register();
        zone.moddev.mc.ironagefurniture.IronAgeFurnitureConfiguration.INTEGRATION_BASEMETALS = false;
    }

    @Test public void handsAndWrongToolsMustStillRecoverEverySconceFamily() {
        LightHolderSconceFloor[] blocks = {
            new LightHolderSconceFloor(Material.IRON, "test", 10, 4),
            new LightHolderSconceWall(Material.IRON, "test", 10, 4),
            new LightSourceSconceTorchFloorTwin(Material.IRON, "test", 10, 4),
            new LightSourceSconceTorchWallTwinUnlit(Material.IRON, "test", 10, 4),
            new LightSourceSconceCandleFloorUnlit(Material.IRON, "test", 10, 4, 4),
            new LightSourceSconceCandleWall(Material.IRON, "test", 10, 4, 4),
            new LightSourceSconceLavaFloor(Material.IRON, "test", 10, 4),
            new LightSourceSconceLavaWall(Material.IRON, "test", 10, 4),
            new LightSourceSconceRockSaltFloor(Material.IRON, "test", 10, 4)
        };
        for (LightHolderSconceFloor block : blocks) {
            assertTrue(block.getClass().getName(), block.canHarvestBlock(null, BlockPos.ORIGIN, null));
            assertEquals("pickaxe", block.getHarvestTool(block.getDefaultState()));
            assertEquals(1, block.getHarvestLevel(block.getDefaultState()));
        }
    }
}

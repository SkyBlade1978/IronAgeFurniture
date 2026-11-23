package zone.moddev.mc.ironagefurniture.client.gui;

import zone.moddev.mc.ironagefurniture.api.container.ContainerCrate;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GuiCrate extends GuiContainer {
	private static final int TEXT_COLOR = 0xFF362316;
	private final ContainerCrate menu;

	public GuiCrate(ContainerCrate menu) {
		super(menu);
		this.menu = menu;
		this.xSize = 176;
		this.ySize = 188;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		int x = this.guiLeft;
		int y = this.guiTop;
		drawRect(x, y, x + this.xSize, y + this.ySize, 0xFF422B1B);
		drawRect(x + 3, y + 3, x + this.xSize - 3, y + this.ySize - 3, 0xFFD9B98A);
		drawRect(x + 49, y + 16, x + 127, y + 94, 0xFF896241);
		for (Slot slot : this.menu.inventorySlots) {
			int left = x + slot.xPos - 1;
			int top = y + slot.yPos - 1;
			drawRect(left, top, left + 18, top + 18, 0xFF765034);
			drawRect(left + 1, top + 1, left + 17, top + 17, 0xFFC5A47C);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String title = this.menu.getTitle().isEmpty() ? I18n.format("container.ironagefurniture.crate") : this.menu.getTitle();
		this.fontRendererObj.drawString(this.fontRendererObj.trimStringToWidth(title, this.xSize - 16), 8, 7, TEXT_COLOR);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, 95, TEXT_COLOR);
	}
}

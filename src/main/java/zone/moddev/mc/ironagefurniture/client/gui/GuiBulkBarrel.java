package zone.moddev.mc.ironagefurniture.client.gui;

import zone.moddev.mc.ironagefurniture.api.container.ContainerBulkBarrel;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GuiBulkBarrel extends GuiContainer {
	private static final int TEXT_COLOR = 0xFF2F1C10;
	private static final String KEY = "gui.ironagefurniture.bulk_barrel.";
	private final ContainerBulkBarrel menu;

	public GuiBulkBarrel(ContainerBulkBarrel menu) {
		super(menu);
		this.menu = menu;
		this.xSize = 216;
		this.ySize = 100;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.add(new GuiButton(ContainerBulkBarrel.TAKE_ONE, this.guiLeft + 11, this.guiTop + 69,
			94, 20, I18n.format(KEY + "take_one")));
		this.buttonList.add(new GuiButton(ContainerBulkBarrel.TAKE_STACK, this.guiLeft + 111, this.guiTop + 69,
			94, 20, I18n.format(KEY + "take_stack")));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		this.mc.playerController.sendEnchantPacket(this.menu.windowId, button.id);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		int x = this.guiLeft;
		int y = this.guiTop;
		drawRect(x, y, x + this.xSize, y + this.ySize, 0xFF2E170A);
		drawRect(x + 3, y + 3, x + this.xSize - 3, y + this.ySize - 3, 0xFFC19761);
		drawRect(x + 11, y + 27, x + 29, y + 45, 0xFF967043);
		drawRect(x + 11, y + 52, x + 205, y + 61, 0xFF6B3A1B);

		if (this.menu.getCapacity() > 0) {
			int width = 192 * this.menu.getCount() / this.menu.getCapacity();
			drawRect(x + 12, y + 53, x + 12 + width, y + 60, 0xFFFFDB82);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRendererObj.drawString(I18n.format(KEY + "title"), 11, 10, TEXT_COLOR);
		ItemStack stack = this.menu.getSlot(0).getStack();
		if (stack != null) {
			this.fontRendererObj.drawString(this.fontRendererObj.trimStringToWidth(stack.getDisplayName(), 170),
				36, 27, TEXT_COLOR);
		}

		this.fontRendererObj.drawString(this.menu.getCount() + " / " + this.menu.getCapacity(), 36, 39, TEXT_COLOR);
	}
}

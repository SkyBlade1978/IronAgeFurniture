package zone.moddev.mc.ironagefurniture.client.gui;

import java.io.IOException;

import zone.moddev.mc.ironagefurniture.api.Blocks.WallShelf.ShelfContentKind;
import zone.moddev.mc.ironagefurniture.api.container.ContainerShelfMedia;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GuiShelfMedia extends GuiContainer {
	private static final int TEXT_COLOR = 0xFF2F1C10;
	private static final int ROW_MARGIN = 8;
	private static final int ICON_SIZE = 16;
	private static final int CLOSE_BUTTON = 0;
	private static final int CLOSE_WIDTH = 64;
	private static final int CLOSE_HEIGHT = 20;
	private static final int FOOTER_PADDING = 4;

	private final ShelfContentKind kind;

	public GuiShelfMedia(ContainerShelfMedia menu) {
		super(menu);
		this.kind = menu.getMediaKind();
		this.xSize = 244;
		int lastRowY = menu.getSlot(menu.inventorySlots.size() - 1).yPos;
		this.ySize = lastRowY + ICON_SIZE + CLOSE_HEIGHT + 2 * FOOTER_PADDING;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.add(new GuiButton(CLOSE_BUTTON, this.guiLeft + this.xSize - ROW_MARGIN - CLOSE_WIDTH,
			this.guiTop + this.ySize - FOOTER_PADDING - CLOSE_HEIGHT,
			CLOSE_WIDTH, CLOSE_HEIGHT, I18n.format("gui.ironagefurniture.shelving.close")));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawRect(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, 0xFF2E170A);
		drawRect(this.guiLeft + 3, this.guiTop + 3, this.guiLeft + this.xSize - 3,
			this.guiTop + this.ySize - 3, 0xFFC19761);

		Slot hovered = this.rowAt(mouseX, mouseY);
		for (Slot slot : this.inventorySlots.inventorySlots) {
			int x = this.guiLeft + slot.xPos;
			int y = this.guiTop + slot.yPos;
			if (slot == hovered) {
				drawRect(this.guiLeft + ROW_MARGIN, y - 1, this.guiLeft + this.xSize - ROW_MARGIN,
					y + ICON_SIZE + 1, 0xFFD4B789);
			}
			drawRect(x - 1, y - 1, x + ICON_SIZE + 1, y + ICON_SIZE + 1, 0xFF6B3A1B);
			drawRect(x, y, x + ICON_SIZE, y + ICON_SIZE, 0xFF967043);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String titleKey = this.kind == ShelfContentKind.BOOKS ? "item.book.name" : "item.record.name";
		if (this.kind == ShelfContentKind.BOTTLES) {
			titleKey = "gui.ironagefurniture.shelving.bottles";
		}
		if (this.inventoryIsFull()) {
			titleKey = "gui.ironagefurniture.shelving.inventory_full";
		}
		this.fontRendererObj.drawString(I18n.format(titleKey), 11, 8, TEXT_COLOR);

		for (Slot slot : this.inventorySlots.inventorySlots) {
			ItemStack stack = slot.getStack();
			if (stack != null) {
				int nameX = slot.xPos + ICON_SIZE + 8;
				int nameWidth = this.xSize - nameX - 2 * ROW_MARGIN;
				String name = displayName(stack);
				if (this.fontRendererObj.getStringWidth(name) > nameWidth) {
					name = this.fontRendererObj.trimStringToWidth(name, nameWidth - this.fontRendererObj.getStringWidth("..."))
						+ "...";
				}
				this.fontRendererObj.drawString(name, nameX, slot.yPos + 4, TEXT_COLOR);
			}
		}
	}

	private static String displayName(ItemStack stack) {
		if (stack.getItem() instanceof ItemRecord && !stack.hasDisplayName()) {
			return ((ItemRecord)stack.getItem()).getRecordNameLocal();
		}
		return stack.getDisplayName();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		Slot row = this.rowAt(mouseX, mouseY);
		if (mouseButton == 0 && row != null) {
			if (!this.inventoryIsFull()) {
				this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, row.slotNumber);
			}
			return;
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == CLOSE_BUTTON) {
			this.mc.player.closeScreen();
		}
	}

	private boolean inventoryIsFull() {
		return this.mc.player.inventory.getFirstEmptyStack() < 0;
	}

	private Slot rowAt(int mouseX, int mouseY) {
		for (Slot slot : this.inventorySlots.inventorySlots) {
			if (slot.getHasStack() && this.isPointInRegion(ROW_MARGIN, slot.yPos,
					this.xSize - 2 * ROW_MARGIN, ICON_SIZE, mouseX, mouseY)) {
				return slot;
			}
		}
		return null;
	}
}

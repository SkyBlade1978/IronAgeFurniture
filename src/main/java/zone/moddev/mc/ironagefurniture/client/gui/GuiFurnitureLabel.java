package zone.moddev.mc.ironagefurniture.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import zone.moddev.mc.ironagefurniture.api.container.ContainerFurnitureLabel;
import zone.moddev.mc.ironagefurniture.api.network.StorageNetwork;
import zone.moddev.mc.ironagefurniture.api.tile.FurnitureLabel;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GuiFurnitureLabel extends GuiContainer {
	private static final int DONE = 0;
	private static final int CANCEL = 1;

	private final ContainerFurnitureLabel menu;
	private GuiTextField text;
	private boolean submitted;

	public GuiFurnitureLabel(ContainerFurnitureLabel menu) {
		super(menu);
		this.menu = menu;
		this.xSize = 212;
		this.ySize = 84;
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);

		String draft = this.text == null ? this.menu.getText() : this.text.getText();
		this.text = new GuiTextField(0, this.fontRendererObj, this.guiLeft + 12, this.guiTop + 28, this.xSize - 24, 20);
		this.text.setMaxStringLength(FurnitureLabel.MAX_LENGTH);
		this.text.setText(draft);
		this.text.setFocused(true);
		this.text.setCanLoseFocus(false);

		this.buttonList.add(new GuiButton(CANCEL, this.guiLeft + this.xSize - 146, this.guiTop + 56, 64, 20,
			I18n.format("gui.cancel")));
		this.buttonList.add(new GuiButton(DONE, this.guiLeft + this.xSize - 76, this.guiTop + 56, 64, 20,
			I18n.format("gui.done")));
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.text.updateCursorCounter();
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		super.onGuiClosed();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.text.drawTextBox();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawRect(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, 0xFF2E170A);
		drawRect(this.guiLeft + 3, this.guiTop + 3, this.guiLeft + this.xSize - 3, this.guiTop + this.ySize - 3, 0xFFC19761);
		String title = I18n.format("sign.edit");
		this.fontRendererObj.drawString(title, this.guiLeft + (this.xSize - this.fontRendererObj.getStringWidth(title)) / 2,
			this.guiTop + 12, 0xFF2F1C10);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		this.text.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char character, int key) {
		if (key == Keyboard.KEY_ESCAPE) {
			this.mc.player.closeScreen();
		} else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
			this.submit();
		} else if (!this.submitted) {
			this.text.textboxKeyTyped(character, key);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == DONE) {
			this.submit();
		} else if (button.id == CANCEL) {
			this.mc.player.closeScreen();
		}
	}

	private void submit() {
		if (!this.submitted) {
			this.submitted = true;
			StorageNetwork.channel.sendToServer(new StorageNetwork.EditLabel(this.menu.windowId, this.text.getText()));
		}
	}
}

package zone.moddev.mc.ironagefurniture.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;

/** Exercises the normal button input path in the client probe. */
public final class ShelfMediaInputProbe {
	private ShelfMediaInputProbe() { }

	public static void clickClose(GuiShelfMedia screen, GuiButton button) throws IOException {
		int right = button.xPosition + button.width;
		int bottom = button.yPosition + button.height;
		if (button.height != 20 || button.xPosition < 0 || button.yPosition < 0
				|| right > screen.width || bottom > screen.height) {
			throw new IllegalStateException("The Close button is clipped");
		}
		screen.mouseClicked(right - 2, bottom - 2, 0);
	}
}

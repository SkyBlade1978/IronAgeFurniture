package zone.moddev.mc.ironagefurniture.client.gui;

import org.lwjgl.input.Keyboard;

public final class ShelfLabelInputProbe {
	private ShelfLabelInputProbe() { }

	public static void type(GuiFurnitureLabel screen, String text, boolean save) {
		for (char character : text.toCharArray()) {
			screen.keyTyped(character, Keyboard.KEY_NONE);
		}
		screen.keyTyped(' ', save ? Keyboard.KEY_RETURN : Keyboard.KEY_ESCAPE);
	}
}

package logisticspipes.gui.guidebook.book;

import java.awt.Color;

import net.minecraft.client.Minecraft;

import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.font.Tokenizer;

public class DrawablePage implements IDrawable {

	private Tokenizer.Token[] tokens;

	public static final float HEADER_SCALING = 1.5F;

	/*
	 * This is very much a WIP, butchered for testing purposes 
	 */

	@Override
	public int draw(Minecraft mc, GuiGuideBook gui, int mouseX, int mouseY, int yOffset) {
		String unformattedText = GuiGuideBook.currentPage.page.getText();
		tokens = Tokenizer.INSTANCE.tokenize(unformattedText);
		gui.fr.drawTokens(tokens, gui.getAreaX0(), gui.getAreaY0() + yOffset + 5, new Color(0xFFFFFFFF));
		return 0;
	}
}

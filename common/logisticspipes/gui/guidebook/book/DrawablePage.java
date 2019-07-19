package logisticspipes.gui.guidebook.book;

import java.awt.Color;

import net.minecraft.client.Minecraft;

import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.font.renderer.Tokenizer;

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
		int xOffset = 0;
		for (Tokenizer.Token token : tokens) {
			if (xOffset + gui.fr.widthToken(token) < gui.getAreaAcrossX()) {
				xOffset += gui.fr.drawToken(token, gui.getAreaX0() + xOffset, gui.getAreaY0() + yOffset + 5, new Color(0xFFFFFFFF));
			} else {
				yOffset += 10;
				xOffset = 0;
				xOffset = gui.fr.drawToken(token, gui.getAreaX0() + xOffset, gui.getAreaY0() + yOffset + 5, new Color(0xFFFFFFFF));
			}
		}
		return yOffset;
	}
}

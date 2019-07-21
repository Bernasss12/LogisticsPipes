package logisticspipes.gui.guidebook.book;

import java.awt.Color;
import java.awt.Point;

import net.minecraft.client.Minecraft;

import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.font.renderer.Tokenizer;

public class DrawablePage implements IDrawable {

	private Tokenizer.Token[] tokens;

	public static final float HEADER_SCALING = 1.5F;

	/*
	 * This is very much a WIP, butchered for testing purposes
	 */

	boolean tokenized = false;

	@Override
	public int draw(Minecraft mc, GuiGuideBook gui, int mouseX, int mouseY, int ySliderOffset) {
		String unformattedText = GuiGuideBook.currentPage.page.getText();
		if (!tokenized) {
			tokens = Tokenizer.INSTANCE.tokenize(unformattedText);
			tokenized = true;
		}
		int xOffset = 0;
		int yOffset = 0;
		Point temp;
		for (Tokenizer.Token token : tokens) {
			if (xOffset + gui.fr.widthToken(token) < gui.getAreaAcrossX()) {
				temp = gui.fr.drawToken(token, gui.getAreaX0() + xOffset, gui.getAreaY0() + ySliderOffset + yOffset + 5, new Color(0xFFFFFFFF));
				xOffset = temp.y == 0 ? xOffset + temp.x : 0;
				yOffset += temp.y;
			} else {
				yOffset += 10;
				temp = gui.fr.drawToken(token, gui.getAreaX0() + xOffset, gui.getAreaY0() + ySliderOffset + yOffset + 5, new Color(0xFFFFFFFF));
				xOffset = temp.x;
				yOffset += temp.y;
			}
		}
		return yOffset;
	}
}

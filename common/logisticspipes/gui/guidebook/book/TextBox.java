package logisticspipes.gui.guidebook.book;

import java.awt.Point;

import net.minecraft.util.math.MathHelper;

import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.font.renderer.IToken;
import logisticspipes.utils.font.renderer.Token;
import logisticspipes.utils.font.renderer.TokenLineBreak;

public class TextBox {

	private GuiGuideBook gui;
	private int x, y, width, height;
	private int drawnHeight;

	private IToken[] tokens;

	boolean needsScroll;

	public TextBox(GuiGuideBook gui, int x, int y, int width, int height, int mouseX, int mouseY, IToken[] tokens) {
		this.gui = gui;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.tokens = tokens;
		drawnHeight = calculateDrawnHeight();
		needsScroll = (drawnHeight > height);
	}

	public void draw(float scroll) {
		int xOffset = 0;
		int yOffset = 8;
		int ySliderOffset = calculateSliderOffset(scroll);
		Point temp;
		for (IToken token : tokens) {
			if (token.getClass().equals(Token.class)) {
				if (xOffset + gui.fr.offsetToken(token).x > width) {
					xOffset = 0;
					yOffset += 10;
				}
				temp = gui.fr.drawToken((Token) token, x + xOffset, y + yOffset + ySliderOffset);
				xOffset += temp.x;
				yOffset += temp.y;
			}
			if (token.getClass().equals(TokenLineBreak.class)) {
				xOffset = 0;
				yOffset += 10;
			}
			//Images
			//Links
			//Headers
		}
	}

	private int calculateDrawnHeight() {
		int xOffset = 0;
		int yOffset = 0;
		Point temp;
		for (IToken token : tokens) {
			if (token.getClass().equals(Token.class)) {
				temp = gui.fr.offsetToken(token);
				if (xOffset + temp.x > width) {
					xOffset = 0;
					xOffset += temp.x;
					yOffset += 10;
				} else {
					xOffset += temp.x;
					yOffset += temp.y;
				}
			}
			if (token.getClass().equals(TokenLineBreak.class)) {
				xOffset = 0;
				yOffset += 10;
			}
			//Images
			//Links
			//Headers
		}
		return yOffset;
	}

	private int calculateSliderOffset(float scroll) {
		return needsScroll ? (MathHelper.clamp((int) (drawnHeight - height * scroll), 0, drawnHeight - height)) : 0;
	}
}

package logisticspipes.gui.guidebook.book;

import java.awt.Color;
import java.awt.Point;

import net.minecraft.client.Minecraft;

import lombok.Setter;

import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.font.renderer.IToken;
import logisticspipes.utils.font.renderer.Token;
import logisticspipes.utils.font.renderer.Tokenizer;

public class DrawablePage implements IDrawable {

	private GuiGuideBook gui;

	private IToken[] tokens;

	@Setter
	private String unformattedText;
	private TextBox textBox;
	private int mouseX, mouseY;

	public static final float HEADER_SCALING = 1.5F;

	public boolean needsScroll;

	/*
	 * This is very much a WIP, butchered for testing purposes
	 */

	@Override
	public void draw(Minecraft mc, GuiGuideBook gui, int mouseX, int mouseY, float scroll) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.gui = gui;
		if(tokens == null) reloadPage();
		if(textBox != null) textBox.draw(scroll);
		this.needsScroll = textBox.needsScroll;
	}

	public void reloadPage(){
		unformattedText = GuiGuideBook.currentPage.page.getText();
		tokens = Tokenizer.INSTANCE.tokenize(unformattedText);
		textBox = new TextBox(gui, gui.getAreaX0(), gui.getAreaY0(), gui.getAreaAcrossX(), gui.getAreaAcrossY(), mouseX, mouseY, tokens);
	}
	
	public boolean getScrollNeeds() {
		return needsScroll;
	}
}

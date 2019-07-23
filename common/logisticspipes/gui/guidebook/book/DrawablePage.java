package logisticspipes.gui.guidebook.book;

import lombok.Setter;

import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.font.renderer.IToken;
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

	public DrawablePage(GuiGuideBook gui) {
		this.gui = gui;
	}

	/*
	 * This is very much a WIP, butchered for testing purposes
	 */

	@Override
	public void draw(int mouseX, int mouseY, float scroll) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		if (tokens == null) reloadPage();
		if (textBox != null) textBox.draw(scroll);
		this.needsScroll = textBox.needsScroll;
	}

	public void reloadPage() {
		unformattedText = gui.currentPage.page.getText();
		tokens = Tokenizer.INSTANCE.tokenize(unformattedText);
		textBox = new TextBox(gui, gui.getAreaX0(), gui.getAreaY0(), gui.getAreaAcrossX(), gui.getAreaAcrossY(), mouseX, mouseY, tokens);
	}

	public boolean getSliderNeeds() {
		return needsScroll;
	}
}

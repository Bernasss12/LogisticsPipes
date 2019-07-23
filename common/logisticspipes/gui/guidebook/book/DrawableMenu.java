package logisticspipes.gui.guidebook.book;

import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.utils.GuideBookContents;

public class DrawableMenu implements IDrawable {

	private GuiGuideBook gui;

	public DrawableMenu(GuiGuideBook gui) {
		this.gui = gui;
	}

	public boolean needsScroll = false;

	@Override
	public void draw(int mouseX, int mouseY, float scroll) {
		int areaCurrentY = 0;
		mouseX = mouseX < gui.getGuiX0() || mouseX > gui.getGuiX3() ? 0 : mouseX;
		mouseY = mouseY < gui.getGuiY0() || mouseY > gui.getGuiY3() ? 0 : mouseY;
		for (GuideBookContents.Division div : gui.gbc.getDivisions()) {
			gui.drawMenuText(gui, gui.getAreaX0(), gui.getAreaY0() + areaCurrentY, gui.getAreaAcrossX(), 19, div.getTitle()); // Re add offset
			areaCurrentY += 20;
			for (int chapterIndex = 0; chapterIndex < div.getChapters().size(); chapterIndex++) {
				gui.divisionsList.get(div.getDindex()).getList().get(chapterIndex).drawMenuItem(gui.mc, mouseX, mouseY, gui.getAreaX0() + (chapterIndex % gui.getTileMax() * (gui.getTileSize() + gui.getTileSpacing())), gui.getAreaY0() + areaCurrentY, gui.getTileSize(), gui.getTileSize(), false); // Re add offset
				int tileBottom = (gui.getAreaY0() + areaCurrentY + gui.getTileSize()); // Re add offset
				int maxBottom = gui.getAreaY1();
				boolean above = tileBottom > maxBottom;
				gui.divisionsList.get(div.getDindex()).getList().get(chapterIndex).drawTitle(gui.mc, mouseX, mouseY, above);
				if ((chapterIndex + 1) % gui.getTileMax() == 0) areaCurrentY += gui.getTileSpacing() + gui.getTileSize();
				if (chapterIndex == div.getChapters().size() - 1) areaCurrentY += gui.getTileSize();
			}
		}
	}

	@Override
	public boolean getSliderNeeds() {
		return needsScroll;
	}
}
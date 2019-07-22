package logisticspipes.gui.guidebook.book;

import net.minecraft.client.Minecraft;

import logisticspipes.gui.guidebook.GuiGuideBook;

public interface IDrawable {
	void draw(Minecraft mc, GuiGuideBook gui, int mouseX, int mouseY, float scroll);
}
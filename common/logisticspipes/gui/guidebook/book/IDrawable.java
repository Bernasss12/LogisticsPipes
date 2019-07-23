package logisticspipes.gui.guidebook.book;

public interface IDrawable {

	void draw(int mouseX, int mouseY, float scroll);

	boolean getSliderNeeds();
}
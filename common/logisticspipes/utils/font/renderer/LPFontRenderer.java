package logisticspipes.utils.font.renderer;

import java.awt.Color;
import java.awt.Point;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import lombok.Getter;
import org.lwjgl.opengl.GL11;

import logisticspipes.LPConstants;
import logisticspipes.utils.font.BDF;
import logisticspipes.utils.font.FontParser;
import logisticspipes.utils.font.FontWrapper;

public class LPFontRenderer {

	private Minecraft mc;
	@Getter
	private BDF fontPlain;
	private BDF fontBold;
	private FontWrapper wrapperPlain;
	private FontWrapper wrapperBold;

	public LPFontRenderer(Minecraft mc, String fontName) {
		this.mc = mc;
		ResourceLocation fontResourcePlain = new ResourceLocation(LPConstants.LP_MOD_ID, "fonts/" + fontName + "-plain.bdf");
		ResourceLocation fontResourceBold = new ResourceLocation(LPConstants.LP_MOD_ID, "fonts/" + fontName + "-bold.bdf");
		this.fontPlain = FontParser.INSTANCE.read(fontResourcePlain);
		this.fontBold = FontParser.INSTANCE.read(fontResourceBold);
		this.wrapperPlain = new FontWrapper(this.fontPlain);
		this.wrapperBold = new FontWrapper(this.fontBold);
	}

	/**
	 * Draws the given char at the given spot with the set color and returns the xOffset for the next character.
	 *
	 * @param c       Character to be drawn
	 * @param x       X coordinate to start drawing the character (left boundary)
	 * @param y       Y coordinate to draw the character, this corresponds to the LINE of the text, the character will be drawn above(almost always) AND below(if needed) this value.
	 * @param color   RGBA color for the character to be drawn.
	 * @param wrapper This is passed so you could draw with different fonts in quick succession if needed.
	 * @return the width of the drawn character
	 */
	protected int draw(char c, int x, int y, Color color, FontWrapper wrapper, boolean italics) {
		// Find current the character's uv coordinates
		int texIndex = wrapper.getTextureIndex(c); // Index of the character texture in the textures map.
		if (texIndex == -1) return 0;
		int width = wrapper.getCharWidth(texIndex); // Width of the draw character
		int height = wrapper.getCharHeight(texIndex); // Height of the drawn character
		int textureY = wrapper.getGlyphY(c);
		BDF.IGlyph glyph = wrapper.getGlyph(c);
		if (width == -1 || height == -1 || textureY == -1 || glyph == null) return 0;
		double x0 = x + glyph.getOffsetX();
		double y0 = y - glyph.getOffsetY() - glyph.getHeight();
		double x1 = x + glyph.getWidth() + glyph.getOffsetX();
		double y1 = y - glyph.getOffsetY();
		double u0 = (0.0D) / (double) width;
		double v0 = (textureY) / (double) height;
		double u1 = (glyph.getWidth()) / (double) width;
		double v1 = (textureY + glyph.getHeight()) / (double) height;
		double italicsOffset = italics ? (glyph.getHeight() * Math.tan(0.2181662D)) : 0.0D;
		GlStateManager.bindTexture(wrapper.getTextures().get(texIndex));
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		// Buffer vertex setting
		buffer.pos(x0 + italicsOffset, y0, 15.0D).tex(u0, v0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(x0, y1, 15.0D).tex(u0, v1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(x1, y1, 15.0D).tex(u1, v1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(x1 + italicsOffset, y0, 15.0D).tex(u1, v0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		tessellator.draw();
		GlStateManager.disableAlpha();
		return glyph.getDWidthX();
	}

	protected Point offset(char c, FontWrapper wrapper, boolean italics) {
		BDF.IGlyph glyph = wrapper.getGlyph(c);
		return new Point(glyph != null ? glyph.getDWidthX() : 0, 0);
	}

	private int drawChar(char c, int x, int y, Color color, FontWrapper wrapper, boolean underline, boolean strikethrough, boolean italics, boolean shadow) {
		BDF.IGlyph glyph = wrapper.getGlyph(c);
		if (strikethrough) {
			lineDrawHorizontal(x, y - 3, glyph.getDWidthX(), 1, color);
		}
		if (underline) {
			lineDrawHorizontal(x, y + 1, glyph.getDWidthX(), 1, color);
		}
		if (shadow) {
			draw(c, x + 1, y + 1, color, wrapper, italics);
		}
		return draw(c, x, y, color, wrapper, italics);
	}

	public int drawString(String string, int x, int y, Color color, FontWrapper wrapper, boolean underline, boolean strikethrough, boolean italics) {
		int xOffset = 0;
		for (char c : string.toCharArray()) {
			xOffset += drawChar(c, x + xOffset, y, color, wrapper, underline, strikethrough, italics, false);
		}
		return xOffset + (italics ? 2 : 0);
	}

	public Point offsetString(String string, FontWrapper wrapper, boolean italics) {
		int xOffset = 0;
		for (char c : string.toCharArray()) {
			xOffset += offset(c, wrapper, italics).x;
		}
		return new Point(xOffset + (italics ? 2 : 0), 0);
	}

	public int stringDrawWithShadow(String string, int x, int y, Color color, FontWrapper wrapper, boolean underline, boolean strikethrough, boolean italics) {
		int xOffset = 0;
		for (char c : string.toCharArray()) {
			xOffset += drawChar(c, x + xOffset, y, color, wrapper, underline, strikethrough, italics, true);
		}
		return xOffset + (italics ? 2 : 0);
	}

	public Point drawToken(Token token, int x, int y) {
		FontWrapper wrapper = token.getTags().contains(Tokenizer.TokenTag.Bold) ? wrapperBold : wrapperPlain;
		boolean italics = token.getTags().contains(Tokenizer.TokenTag.Italic);
		boolean strikethrough = token.getTags().contains(Tokenizer.TokenTag.Strikethrough);
		boolean underline = token.getTags().contains(Tokenizer.TokenTag.Underline);
		int yOffset = token.getClass().equals(TokenLineBreak.class) ? 10 : 0;
		Color color = token.getColor();
		return new Point(drawString(token.getStr(), x, y, color, wrapper, underline, strikethrough, italics), yOffset);
	}

	public Point offsetToken(IToken token) {
		if (token.getClass().equals(Token.class)) {
			FontWrapper wrapper = ((Token) token).getTags().contains(Tokenizer.TokenTag.Bold) ? wrapperBold : wrapperPlain;
			boolean italics = ((Token) token).getTags().contains(Tokenizer.TokenTag.Italic);
			return offsetString(((Token) token).getStr(), wrapper, italics);
		} else if (token.getClass().equals(TokenLineBreak.class)){
			return new Point(0, 10);
		} else return new Point();
		// Image
		// Link
		// Header
	}

	public void lineDrawHorizontal(int x, int y, int width, int thickness, Color color) {
		GlStateManager.disableTexture2D();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(x, y, 5.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(x, y + thickness, 5.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(x + width, y + thickness, 5.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(x + width, y, 5.0D).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
	}
}

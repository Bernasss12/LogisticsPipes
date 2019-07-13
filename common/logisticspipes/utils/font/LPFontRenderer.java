package logisticspipes.utils.font;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import logisticspipes.LPConstants;

public class LPFontRenderer {

	private Minecraft mc;
	private BDF fontPlain;
	private FontWrapper wrapperPlain;

	public LPFontRenderer(Minecraft mc, String fontName) {
		this.mc = mc;
		ResourceLocation fontResource = new ResourceLocation(LPConstants.LP_MOD_ID, "fonts/" + fontName + ".bdf");
		this.fontPlain = FontParser.INSTANCE.read(fontResource);
		this.wrapperPlain = new FontWrapper(this.fontPlain);
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
	protected int draw(char c, int x, int y, Color color, FontWrapper wrapper) {
		// Find current the character's uv coordinates
		int texIndex = wrapper.getTextureIndex(c); // Index of the character texture in the textures map.
		if (texIndex == -1) return 0;
		int width = wrapper.getCharWidth(texIndex); // Width of the draw character
		int height = wrapper.getCharHeight(texIndex); // Height of the drawn character
		int textureY = wrapper.getGlyphY(c);
		BDF.IGlyph glyph = wrapper.getGlyph(c);
		if (width == -1 || height == -1 || textureY == -1 || glyph == null) return 0;
		int xw = width;
		int yh = height;
		double w = glyph.getWidth() / (double) width;
		double h = glyph.getHeight() / (double) height;
		double u = 0.0D;
		double v = textureY / (double) height;
		double finalX = (double)x + glyph.getOffsetX();
		double finalY = (double)y + glyph.getOffsetY();
		GlStateManager.bindTexture(wrapper.getTextures().get(texIndex));
		GlStateManager.enableAlpha();
		//GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		//GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		//GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		// Buffer vertex setting
		buffer.pos(finalX, finalY, 5.0D).tex(u, v).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(finalX, finalY + glyph.getHeight(), 5.0D).tex(u, v + h).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(finalX + glyph.getWidth(), finalY + glyph.getHeight(), 5.0D).tex(u + w, v + h).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(finalX + glyph.getWidth(), finalY, 5.0D).tex(u + w, v).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		tessellator.draw();
		return glyph.getDWidthX();
	}

	public void drawString(String string, int x, int y, Color color){
		int xOffset = 0;
		for (char c: string.toCharArray()){
			xOffset += draw(c, x + xOffset, y, color, wrapperPlain);
		}
	}
}

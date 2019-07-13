package logisticspipes.utils.font

import lombok.Getter
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.TextureUtil
import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer
import java.util.*

private val buffer = ByteBuffer.allocateDirect(10000000).asIntBuffer()

class FontWrapper(val font: BDF) {
    var textures: List<Int> = emptyList(); private set

    var glyphPosY: Map<Char, Int> = emptyMap()
    var textureIndex: Map<Char, Int> = emptyMap(); private set

    var widthMap: Map<Int, Int> = emptyMap(); private set
    var heightMap: Map<Int, Int> = emptyMap(); private set

    var charWidth = 0; private set
    var charHeight = 0; private set

    private val maxTexSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE)

    private var destroyed = false

    init {
        allocateTextures()

        var currentHeight = 0
        var currentTex = 0
        for ((k, v) in font.glyphs) {
            charWidth = maxOf(charWidth, v.width)
            charHeight = maxOf(charHeight, v.height)

            if (currentHeight + v.height > heightMap[currentTex]!!) {
                currentHeight = 0
                currentTex++
                if (currentTex > textures.size)
                    error("A fatal error occurred while writing texture sheet. This shouldn't ever happen unless this code has a bug. RIP")
            }

            glyphPosY += k to currentHeight
            textureIndex += k to currentTex

            setTexture(v.bitmap, currentTex, currentHeight, v.width, v.height)
            currentHeight += v.height
        }
    }

    private fun setTexture(bitmap: BitSet, texture: Int, y: Int, width: Int, height: Int) {
        buffer.clear()
        for (i in 0 until width * height)
            buffer.put(if (bitmap[i]) -1 else 0)
        buffer.flip()
        GlStateManager.bindTexture(textures[texture])
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer)
    }

    // Creates the necessary textures with all the glyphs in them
    private fun allocateTextures() {
        val textureWidth = (font.glyphs.values.maxBy { it.width }?.width ?: 0).powerOf2() // Calculate size of

        var heightTmp = font.glyphs.values.sumBy { it.height }
        var texCount = 1
        while (heightTmp > maxTexSize) {
            heightTmp -= maxTexSize
            texCount++
        }
        for (i in 0 until texCount) {
            val height = if (i < texCount - 1) maxTexSize else heightTmp.powerOf2()

            val texId = GL11.glGenTextures()
            textures += texId
            widthMap += i to textureWidth
            heightMap += i to height

            GlStateManager.bindTexture(texId)
            TextureUtil.allocateTexture(texId, textureWidth, height)

            println("Allocated $textureWidth*$height texture ($texId)")
        }
    }

    // Getter for the texture indexes, returns -1 if null.
    fun getTextureIndex(c: Char): Int {
        return textureIndex[c] ?: -1
    }

    // Getter for width and height, return -1 if null.
    fun getCharWidth(textureIndex: Int): Int {
        return widthMap[textureIndex] ?: -1
    }

    fun getCharHeight(textureIndex: Int): Int {
        return heightMap[textureIndex] ?: -1
    }

    // Getter for the glyph's Y coordinate
    fun getGlyphY(c: Char): Int {
        return glyphPosY[c] ?: -1
    }

    // Getter for the Glyph object
    fun getGlyph(c: Char): BDF.IGlyph? {
        return font.glyphs[c] ?: font.glyphs[font.defaulChar]
    }

    // No idea how this works, found on StackOverflow :P This returns the closest greatest power of 2.
    private fun Int.powerOf2(): Int {
        var n = this - 1
        n = n or n.ushr(1)
        n = n or n.ushr(2)
        n = n or n.ushr(4)
        n = n or n.ushr(8)
        n = n or n.ushr(16)
        return if (n < 0) 1 else if (n >= Integer.MAX_VALUE) Integer.MAX_VALUE else n + 1
    }
}
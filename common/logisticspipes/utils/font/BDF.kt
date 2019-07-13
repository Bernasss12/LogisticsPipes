package logisticspipes.utils.font

import java.util.*

data class BDF(val glyphs: Map<Char, IGlyph>, val defaulChar: Char){
    interface IGlyph{
        val name: String
        val charPoint: Char
        val dWidthX: Int
        val dWidthY: Int
        val width: Int
        val height: Int
        val offsetX: Int
        var offsetY: Int
        val bitmap: BitSet
    }
}

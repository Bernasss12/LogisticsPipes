package logisticspipes.utils.font.renderer

import logisticspipes.LogisticsPipes
import logisticspipes.utils.string.StringUtils.handleColor
import java.awt.Color

/*
 * TODO (This will not be pushed to the main repo, it is merely for personal tracking :P)
 *  - Add cases for underlined and for ~~strikethrough~~
 *  - Add Image, Link and Color parsing
 *  - Add Item Translated name support
 */

object Tokenizer {

    var current = mutableListOf<TokenTag>()
    var currentColor = Color.WHITE
    var definition = Definition.None

    fun tokenize(str: String): Array<Token> {
        var strC = str.toCharArray()
        var string = StringBuilder()
        var tokens = mutableListOf<Token>()
        strC.forEachIndexed { index, c ->
            if (definition == Tokenizer.Definition.None) when (c) {
                '\n' -> string.handleLineBreak(c, index, strC, tokens)
                '_', '*' -> string.handleAsteriskAndUnderscore(c, index, strC, tokens)
                '[' -> string.handleSquareParenthesis(c, index, strC, tokens)
                '~' -> string.handleTilda(c, index, strC, tokens)
                else -> string.handleDefault(c, index, strC, tokens)
            } else when (c){
                '[', ']' -> string.handleSquareParenthesis(c, index, strC, tokens)
                else -> string.handleDefault(c, index, strC, tokens)
            }
        }
        return tokens.toTypedArray()
    }

    private fun StringBuilder.handleLineBreak(c: Char, index: Int, strC: CharArray, tokens: MutableList<Token>){
        var lineBreakTag = mutableListOf<TokenTag>()
        lineBreakTag.add(Tokenizer.TokenTag.LineBreak)
        if(strC.prevChar(index) == ' '){
            if(strC.prevChar(index-1) == ' '){
                tokens.add(Token("", lineBreakTag.toMutableList(), currentColor))
            }else{
                tokens.add(Token("", current.toMutableList(), currentColor))
            }
        }else{
            tokens.add(Token(" ", current.toMutableList(), currentColor))
        }
        clear()
    }

    private fun StringBuilder.handleSquareParenthesis(c: Char, index: Int, strC: CharArray, tokens: MutableList<Token>) {
        fun StringBuilder.handleColor() {
            fun opener() {
                deleteCharAt(lastIndex)
                definition = Definition.Color
                if (isNotEmpty()) tokens.add(Token(toString(), current.toMutableList(), currentColor ?: Color.WHITE))
                clear()
            }

            fun closer() {
                currentColor = toString().toColor()
                definition = Tokenizer.Definition.None
                clear()
            }

            if (c == '[') opener() else closer()
        }

        if (c == '[') {
            if (definition == Tokenizer.Definition.None) when (strC.prevChar(index)) {
                '\\' -> handleDefault(c, index, strC, tokens)
                '+' -> handleColor()
            }
        } else if (c == ']'){
            if (definition != Tokenizer.Definition.None && strC.prevChar(index) != '\\') when (definition) {
                Tokenizer.Definition.None -> handleDefault(c, index, strC, tokens)
                Tokenizer.Definition.Color -> handleColor()
            }
        }
    }

    /*
    * Handler for the '~' character, that leads to the Strikethrough text format.
    * */
    private fun StringBuilder.handleTilda(c: Char, index: Int, strC: CharArray, tokens: MutableList<Token>) {
        fun StringBuilder.handleStrikethrough() {
            fun opener() {
                deleteCharAt(lastIndex)
                if (isNotEmpty()) tokens.add(Token(toString(), current.toMutableList(), currentColor ?: Color.WHITE))
                current.add(Tokenizer.TokenTag.Strikethrough)
                clear()
            }

            fun closer() {
                deleteCharAt(lastIndex)
                if (isNotEmpty()) tokens.add(Token(toString(), current.toMutableList(), currentColor ?: Color.WHITE))
                current.remove(Tokenizer.TokenTag.Strikethrough)
                clear()
            }

            if (!current.contains(Tokenizer.TokenTag.Strikethrough)) opener() else closer()
        }

        fun StringBuilder.handleUnderline() {
            fun opener() {
                if (isNotEmpty()) tokens.add(Token(toString(), current.toMutableList(), currentColor ?: Color.WHITE))
                current.add(Tokenizer.TokenTag.Underline)
                clear()
            }

            fun closer() {
                if (isNotEmpty()) tokens.add(Token(toString(), current.toMutableList(), currentColor ?: Color.WHITE))
                current.remove(Tokenizer.TokenTag.Underline)
                clear()
            }

            if (!current.contains(Tokenizer.TokenTag.Underline)) opener() else closer()
        }

        when (strC.nextChar(index)) {
            c -> handleDefault(c, index, strC, tokens)
            else -> {
                when (strC.prevChar(index)) {
                    c -> handleStrikethrough()
                    else -> handleUnderline()
                }
            }
        }
    }

    /*
     Handler for the '*' and '_' characters which can lead to either Bold or Italic formatting depending on the situation.
     */
    private fun StringBuilder.handleAsteriskAndUnderscore(c: Char, index: Int, strC: CharArray, tokens: MutableList<Token>) {
        fun StringBuilder.handleBold() {
            fun opener() {
                this.deleteCharAt(this.lastIndex)
                if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toMutableList(), currentColor ?: Color.WHITE))
                current.add(Tokenizer.TokenTag.Bold)
                this.clear()
            }

            fun closer() {
                this.deleteCharAt(this.lastIndex)
                if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toMutableList(), currentColor ?: Color.WHITE))
                current.remove(Tokenizer.TokenTag.Bold)
                this.clear()
            }

            if (!current.contains(Tokenizer.TokenTag.Bold)) opener() else closer()
        }

        fun StringBuilder.handleItalic() {
            fun opener() {
                if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toMutableList(), currentColor ?: Color.WHITE))
                current.add(Tokenizer.TokenTag.Italic)
                this.clear()
            }

            fun closer() {
                if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toMutableList(), currentColor ?: Color.WHITE))
                current.remove(Tokenizer.TokenTag.Italic)
                this.clear()
            }

            if (!current.contains(Tokenizer.TokenTag.Italic)) opener() else closer()
        }
        when (strC.nextChar(index)) {
            c -> handleDefault(c, index, strC, tokens)
            else -> {
                when (strC.prevChar(index)) {
                    c -> handleBold()
                    else -> handleItalic()
                }
            }
        }
    }

    /*
     Handler for the default case:
     Runs if no Markdown tags are detected within the current character and the ones before and after.
     */
    private fun StringBuilder.handleDefault(c: Char, index: Int, strC: CharArray, tokens: MutableList<Token>) {
        if (c != '\\') this.append(c)
        else if (strC.prevChar(index) == '\\') this.append(c)
        if (index == strC.lastIndex || c == ' ') {
            tokens.add(Token(this.toString(), current, currentColor ?: Color.WHITE))
            this.clear()
        }
    }

    /*
     Returns the next Char in the array:
     If the previous index is out of bounds returns an indifferent character, in this case: ' '
     If said character was escaped using '\' the function returns the same indifferent character as before.
     */
    private fun CharArray.prevChar(index: Int): Char {
        if (index == 0) return ' '
        if ((index - 1 != 0) && this[index - 2] == '\\') return ' '
        return this[index - 1]
    }

    /*
    Returns the next Char in the array:
    If the next index is out of bounds returns an indifferent character, in this case: ' '
    */
    private fun CharArray.nextChar(index: Int): Char {
        if (index == this.lastIndex) return ' '
        return this[index + 1]
    }

    private fun String.toColor(): Color {
        if (this.first() == '#') return Color(this.substring(1, lastIndex).toInt(16))
        val field = Class.forName("java.awt.Color").getField(this.toUpperCase())
        return try {
            field.get(null) as Color
        } catch (e: Exception) {
            LogisticsPipes.log.error("The given string: $this does not correspond to an hex color value nor a color name. Defaulted to WHITE.")
            Color.WHITE
        }
    }

    /*
     Token that has the token text as well as all the formatting tags associated with that text.
     */
    class Token(val str: String, val tags: MutableList<TokenTag>, val color: Color)

    /*
     Used to track the tags a token has so the renderer knows how to draw said token.
     This is also a sealed class instead of an enum class because this way I can store the linked page in the Link tag as well as an image in an Image tag, for example.
     */
    enum class TokenTag {
        Italic,
        Bold,
        Strikethrough,
        Underline,
        LineBreak
    }

    enum class Definition {
        None,
        Color
    }
}




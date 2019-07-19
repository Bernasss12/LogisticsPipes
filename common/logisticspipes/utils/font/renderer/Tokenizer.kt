package logisticspipes.utils.font.renderer

import logisticspipes.LogisticsPipes
import java.awt.Color

/*
 * TODO (This will not be pushed to the main repo, it is merely for personal tracking :P)
 *  - Add cases for underlined and for strikethrough
 *  - Add Image, Link and Color parsing
 *  - Add Item Translated name support
 */

object Tokenizer {

    var state = State.Normal
    var current = mutableListOf<TokenTag>()


    fun tokenize(str: String): Array<Token> {
        var strC = str.toCharArray()
        var string = StringBuilder()
        var tokens = mutableListOf<Token>()
        strC.forEachIndexed { index, c ->
            when (c) {
                '_', '*' -> string.handleAsteriskAndUnderscore(c, index, strC, tokens)
                '[', ']' -> string.handleSquareParenthesis(c, index, strC, tokens)
                '(', ')' -> string.handleParenthesis(c, index, strC, tokens)
                else -> string.handleDefault(c, index, strC, tokens)
            }
        }
        return  tokens.toTypedArray()
    }

    private fun StringBuilder.handleParenthesis(c: Char, index: Int, strC: CharArray, tokens: MutableList<Token>) {
        fun handleColor() {
            fun opener() {
                if (strC.prevChar(index) != ']') {
                    handleDefault(c, index, strC, tokens)
                } else {
                    this.clear()
                }
            }

            fun closer() {
                tokens.add(Token(this.toString(), current.toMutableList()))
                current.remove(Tokenizer.TokenTag.Color())
                state = Tokenizer.State.Normal
                this.clear()
            }

            when (c) {
                '(' -> opener()
                ')' -> closer()
            }
        }

        if (strC.prevChar(index) == '\\') {
            handleDefault(c, index, strC, tokens)
        } else when (state) {
            Tokenizer.State.Color, Tokenizer.State.ColorText -> handleColor()
            else -> Unit
        }
    }

    private fun StringBuilder.handleSquareParenthesis(c: Char, index: Int, strC: CharArray, tokens: MutableList<Token>) {
        fun handleColor() {
            fun opener() {
                this.deleteCharAt(lastIndex)
                if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toMutableList()))
                state = State.Color
                this.clear()
            }

            fun closer() {
                if (strC.prevChar(index) == '\\') {
                    return handleDefault(c, index, strC, tokens)
                }
                if (strC.nextChar(index) == '(') {
                    current.add(TokenTag.Color(color = this.toString().toColor()))
                    state = State.ColorText
                    this.clear()
                }
            }


            when (c) {
                '[' -> opener()
                ']' -> closer()
            }
        }

        // Chooses which special tag is being set by checking the character prior to the '['
        fun opener() = when (strC.prevChar(index)) {
            '\\' -> handleDefault(c, index, strC, tokens)
            '+' -> handleColor()
            else -> Unit
        }

        fun closer() = when (strC.nextChar(index)) { //TODO Change
            '(' -> {
                if (TokenTag.Color() in current) handleColor()
                else handleDefault(c, index, strC, tokens)
            }
            else -> Unit
        }

        // Choose between both possibilities
        when (c) {
            '[' -> opener()
            ']' -> closer()
        }
    }

    private fun StringBuilder.handleAsteriskAndUnderscore(c: Char, index: Int, strC: CharArray, tokens: MutableList<Token>) {
        fun StringBuilder.handleBold() {
            fun opener() {
                this.deleteCharAt(this.lastIndex)
                if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toMutableList()))
                current.add(Tokenizer.TokenTag.Bold)
                this.clear()
            }

            fun closer() {
                this.deleteCharAt(this.lastIndex)
                if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toMutableList()))
                current.remove(Tokenizer.TokenTag.Bold)
                this.clear()
            }

            if (!current.contains(Tokenizer.TokenTag.Bold)) opener() else closer()
        }

        fun StringBuilder.handleItalic() {
            fun opener() {
                if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toMutableList()))
                current.add(Tokenizer.TokenTag.Italic)
                this.clear()
            }

            fun closer() {
                if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toMutableList()))
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
            tokens.add(Token(this.toString(), current))
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
    class Token(val str: String, val tags: MutableList<TokenTag>)

    /*
     Used to track the tags a token has so the renderer knows how to draw said token.
     This is also a sealed class instead of an enum class because this way I can store the linked page in the Link tag as well as an image in an Image tag, for example.
     */
    sealed class TokenTag {
        object Italic : TokenTag()
        object Bold : TokenTag()
        class Color(var color: java.awt.Color = java.awt.Color.WHITE) : TokenTag()
    }

    enum class State {
        Normal,
        Color,
        ColorText,
        Image,
        ImageText,
        Item,
        ItemText,
        LinkParsing
    }
}




package logisticspipes.utils.font.renderer

import logisticspipes.LogisticsPipes
import java.awt.Color

/*
 * TODO (This will not be pushed to the main repo, it is merely for personal tracking :P)
 *  - Add Image and Link
 *  - Add Item Translated name support
 */

object Tokenizer {

    var definition = Definition.None
    var currentColor = Color.RED
    var current = mutableListOf<TokenTag>()
    private lateinit var temporaryHeader: TokenHeader

    fun tokenize(str: String): Array<IToken> {
        val strC = str.toCharArray()
        val string = StringBuilder()
        val tokens = mutableListOf<IToken>()
        strC.forEachIndexed { index, c ->
            handleCharacter(c, index, tokens, strC, string)
        }
        return tokens.toTypedArray()
    }

    private fun StringBuilder.handleSpecialDefinitions(c: Char, index: Int, tokens: MutableList<IToken>, strC: CharArray) {
        fun startDefinition(def: Definition){
            if(isNotEmpty() && this[lastIndex] == c && c != ' ') deleteCharAt(lastIndex)
            if (isNotEmpty()){
                if (current.contains(TokenTag.Colored)){
                    tokens.add(Token(toString(), current.toMutableList(), clr = currentColor))
                }else{
                    tokens.add(Token(toString(), current.toMutableList()))
                }
            }
            clear()
            definition = def
        }

        when(c){
            '[' -> when(strC.prevChar(index)){
                '+' -> startDefinition(Definition.Color)
                else -> Unit
            }
            ']' -> when(definition){
                //Tokenizer.Definition.Color -> //closeColor()
            }
        }
    }

    private fun StringBuilder.handleSpecialTag(c: Char, index: Int, tokens: MutableList<IToken>, strC: CharArray) {
        fun StringBuilder.handleLineBreak() {
            when {
                definition == Tokenizer.Definition.Header -> {
                    definition = Definition.AddHeader
                }
                strC.isAfterTwoSpaces(index) -> {
                    tokens.add(TokenLineBreak())
                }
                strC.isAfterOneSpace(index) -> Unit
                else -> {
                    append(' ')
                    tokens.add(Token(toString(), current.toMutableList()))
                }
            }
            clear()
        }

        fun StringBuilder.handleSingleCharacter(tag: TokenTag) {
            fun toggleTag() {
                if (isNotEmpty()) tokens.add(Token(toString(), current.toMutableList()))
                when {
                    current.contains(tag) -> current.remove(tag)
                    else -> current.add(tag)
                }
                clear()
            }

            when (c) {
                strC.nextChar(index) -> Unit
                else -> toggleTag()
            }
        }

        fun StringBuilder.handleDoubleCharacter(tag: TokenTag, singleTag: TokenTag) {
            fun toggleTag() {
                if (isNotEmpty()){
                    if (current.contains(TokenTag.Colored)){
                        tokens.add(Token(toString(), current.toMutableList(), clr = currentColor))
                    }else{
                        tokens.add(Token(toString(), current.toMutableList()))
                    }
                }
                when {
                    current.contains(tag) -> current.remove(tag)
                    else -> current.add(tag)
                }
                clear()
            }

            fun toggleTags(){
                toggleTag()
                when {
                    current.contains(singleTag) -> current.remove(singleTag)
                    else -> current.add(singleTag)
                }
            }

            when (c) {
                strC.nextChar(index) -> Unit
                strC.prevChar(index) -> {
                    if (strC.prevChar(index-1) == c){
                        toggleTags()
                    }else{
                        toggleTag()
                    }
                }
                else -> handleSingleCharacter(singleTag)
            }
        }

        fun StringBuilder.handleSpecialHeader() {
            fun startHeader() {
                if (isNotEmpty()) {
                    if (current.contains(TokenTag.Colored)){
                        tokens.add(Token(toString(), current.toMutableList(), clr = currentColor))
                    }else{
                        tokens.add(Token(toString(), current.toMutableList()))
                    }
                }
                clear()
                temporaryHeader = TokenHeader()
                definition = Tokenizer.Definition.Header
            }

            if (strC.prevChar(index) == c) startHeader()
        }

        when (c) {
            '\n' -> this.handleLineBreak()
            '_', '*' -> handleDoubleCharacter(TokenTag.Bold, TokenTag.Italic)
            '~' -> handleDoubleCharacter(TokenTag.Strikethrough, TokenTag.Underline)
            '^' -> handleDoubleCharacter(TokenTag.Colored, TokenTag.Shadow)
            '#' -> handleSpecialHeader()
            else -> Unit
        }
    }

    private fun StringBuilder.handleTextDefault(c: Char, index: Int, tokens: MutableList<IToken>, strC: CharArray) {
        append(c)
        if ((c == ' ' && strC.prevChar(index) != ' ') || index == lastIndex) {
            if (isNotEmpty()){
                if (current.contains(TokenTag.Colored)){
                    tokens.add(Token(toString(), current.toMutableList(), clr = currentColor))
                }else{
                    tokens.add(Token(toString(), current.toMutableList()))
                }
            }
            clear()
        }
    }

    private fun handleAddHeader(tokens: MutableList<IToken>){
        tokens.add(temporaryHeader.clone())
        tokens.add(TokenLineBreak())
        temporaryHeader = TokenHeader()
        definition = Definition.None
    }

    private fun handleCharacter(c: Char, index: Int, tokens: MutableList<IToken>, strC: CharArray, string: StringBuilder) {
        when (definition) {
            Tokenizer.Definition.None -> when (c) {
                '\\', '_', '*', '~', '^', '\n', '#' -> if (strC.isEscaped(index)) string.handleTextDefault(c, index, tokens, strC) else string.handleSpecialTag(c, index, tokens, strC)
                '[', ']', '(', ')', '<', '>' -> if (strC.isEscaped(index)) string.handleTextDefault(c, index, tokens, strC) else string.handleSpecialDefinitions(c, index, tokens, strC)
                else -> string.handleTextDefault(c, index, tokens, strC)
            }
            Tokenizer.Definition.Header -> when (c) {
                '\\', '_', '*', '~', '^', '\n', '#' -> if (strC.isEscaped(index)) string.handleTextDefault(c, index, temporaryHeader.tokens, strC) else string.handleSpecialTag(c, index, temporaryHeader.tokens, strC)
                else -> string.handleTextDefault(c, index, temporaryHeader.tokens, strC)
            }
            Tokenizer.Definition.AddHeader -> handleAddHeader(tokens)
        }
    }

    /*
     Returns the next Char in the array:
     If the previous index is out of bounds returns an indifferent character, in this case: 'a'
     If said character was escaped using '\' the function returns the same indifferent character as before.
     */
    private fun CharArray.prevChar(index: Int): Char {
        return if (index == 0 || (index > 1 && this[index - 1] == '\\')) 'a' else this[index - 1]
    }

    /*
    Returns the next Char in the array:
    If the next index is out of bounds returns an indifferent character, in this case: 'a'
    */
    private fun CharArray.nextChar(index: Int): Char {
        return if (index == lastIndex) 'a' else this[index + 1]
    }

    /*
     Checks if the previous character is a backslash and if the one prior to that is also a backslash, because if the backslash itself is escaped it does not escape the current character.
     */
    private fun CharArray.isEscaped(index: Int): Boolean {
        return (prevChar(index) == '\\') && (prevChar(index - 1) != '\\')
    }

    /*
     Checks if the two previous characters are spaces.
     */
    private fun CharArray.isAfterTwoSpaces(index: Int): Boolean {
        return ((isAfterOneSpace(index)) && (isAfterOneSpace(index - 1)))
    }

    /*
     Checks if the previous character is a space
     */
    private fun CharArray.isAfterOneSpace(index: Int): Boolean {
        return (prevChar(index) == ' ')
    }

    /*
     Converts a string of type #hex or a color name to a valid non-null Color object.
     */
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
     Used to track the tags a token has so the renderer knows how to draw said token.
     This is also a sealed class instead of an enum class because this way I can store the linked page in the Link tag as well as an image in an Image tag, for example.
     */
    enum class TokenTag {
        Italic,
        Bold,
        Strikethrough,
        Underline,
        Shadow,
        Colored
    }

    enum class Parsing {
        Text
    }

    enum class Definition {
        None,
        Header,
        AddHeader,
        Color
    }
}




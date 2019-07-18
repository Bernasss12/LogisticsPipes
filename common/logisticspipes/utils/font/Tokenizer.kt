package logisticspipes.utils.font

/*
 * TODO (This will not be pushed to the main repo, it is merely for personal tracking :P)
 *  - Add cases for underlined and for strikethrough
 *  - Add Image, Link and Color parsing
 *  - Add Item Translated name support
 */

object Tokenizer {
    fun tokenize(str: String): Array<Token> {
        var current = mutableListOf<Current>()
        var tokens = mutableListOf<Token>()
        var strC = str.toCharArray()
        var string = StringBuilder()
        strC.forEachIndexed { index, c ->
            when (c) {
                '_', '*' -> string.handleAsteriskAndUnderscore(c, index, tokens, current, strC)
                else -> string.handleDefault(c, index, tokens, current, strC)
            }
        }
        return tokens.toTypedArray()
    }

    private fun StringBuilder.handleBold(c: Char, tokens: MutableList<Token>, current: MutableList<Current>, strC: CharArray) {
        fun opener() {
            this.deleteCharAt(this.lastIndex)
            if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toTokenTagList()))
            current.add(Current.Bold)
            this.clear()
        }

        fun closer() {
            this.deleteCharAt(this.lastIndex)
            if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toTokenTagList()))
            current.remove(Current.Bold)
            this.clear()
        }

        if (!current.contains(Tokenizer.Current.Bold)) opener() else closer()
    }

    private fun StringBuilder.handleItalic(c: Char, tokens: MutableList<Token>, current: MutableList<Current>, strC: CharArray) {
        fun opener() {
            if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toTokenTagList()))
            current.add(Current.Italic)
            this.clear()
        }

        fun closer() {
            if (this.isNotEmpty()) tokens.add(Token(this.toString(), current.toTokenTagList()))
            current.remove(Current.Italic)
            this.clear()
        }

        if (!current.contains(Tokenizer.Current.Italic)) opener() else closer()
    }

    private fun StringBuilder.handleAsteriskAndUnderscore(c: Char, index: Int, tokens: MutableList<Token>, current: MutableList<Current>, strC: CharArray) {
        when (strC.nextChar(index)) {
            c -> handleDefault(c, index, tokens, current, strC)
            else -> {
                when (strC.prevChar(index)) {
                    c -> handleBold(c, tokens, current, strC)
                    else -> handleItalic(c, tokens, current, strC)
                }
            }
        }
    }

    /*
     Handler for the default case:
     Runs if no Markdown tags are detected within the current character and the ones before and after.
     */
    private fun StringBuilder.handleDefault(c: Char, index: Int, tokens: MutableList<Token>, current: MutableList<Current>, strC: CharArray) {
        if (c != '\\') this.append(c)
        else if (strC.prevChar(index) == '\\') this.append(c)
        if (index == strC.lastIndex || c == ' ') {
            tokens.add(Token(this.toString(), current.toTokenTagList()))
            this.clear()
        }
    }

    /*
     Turns a workable MutableList of Current classes into a List of TokenTags, only really used to put into Tokens
     */
    private fun MutableList<Current>.toTokenTagList(): List<TokenTag> {
        var list = listOf<TokenTag>()
        this.forEach { list += it.toTokenTag() }
        return list
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

    /*
     Token that has the token text as well as all the formatting tags associated with that text.
     */
    class Token(val str: String, val tags: List<TokenTag>) {
        fun contains(str: String): Boolean {
            if (tags.contains(str.toTokenTag())) return true
            return false
        }
    }

    /*
     Used to keep track of the currently opened markdown tags, and to know what to expect.
     It is a sealed class instead of an enum class because this way I can store the used char in case some tag can be set by multiple tags.
     */
    enum class Current {
        Italic {
            override fun toTokenTag(): TokenTag {
                return Tokenizer.TokenTag.Italic
            }
        },
        Bold {
            override fun toTokenTag(): TokenTag {
                return Tokenizer.TokenTag.Bold
            }
        };

        abstract fun toTokenTag(): TokenTag
    }

    /*
     Used to track the tags a token has so the renderer knows how to draw said token.
     This is also a sealed class instead of an enum class because this way I can store the linked page in the Link tag as well as an image in an Image tag, for example.
     */
    sealed class TokenTag {
        object Italic : TokenTag()
        object Bold : TokenTag()
    }

    /*
     This may end up being changed if I find another solution but this is meant to turn a string into a matching TokenTag
     */
    fun String.toTokenTag(): TokenTag? = when (this) {
        "Italic" -> Tokenizer.TokenTag.Italic
        "Bold" -> Tokenizer.TokenTag.Bold
        else -> null
    }
}




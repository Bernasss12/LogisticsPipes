package logisticspipes.utils.font

/*
 * TODO (This will not be pushed to the main repo, it is merely for personal tracking :P)
 *  - Add cases for underlined and for strikethrough
 *  - Add Image, Link and Color parsing
 */

object Tokenizer {
    fun tokenize(str: String): Array<Token> {
        var expecting = mutableListOf<Expecting>()
        var tokens = mutableListOf<Token>()
        var strc = str.toCharArray()
        var tags = mutableListOf<TokenTag>()
        var string = StringBuilder()
        strc.forEachIndexed { index, c ->
            when (c) {
                ' ' -> { // To ease text wrapping in the future the text breaks into a new token every new space character.
                    string.append(c)
                    if (tags.isEmpty()) tokens.add(Token(string.toString(), mutableListOf(TokenTag.Plain)))
                    else if (string.isNotEmpty()) tokens.add(Token(string.toString(), tags.toMutableList()))
                    string.clear()
                }
                '_' -> {
                    if (index != strc.lastIndex && strc[index + 1] == '_') // If next character is the same just ignore it and deal with it retroactively
                    else if (index != 0 && strc[index - 1] == '_') { // If the character before is equal to this one treat as a Bold token
                        if (expecting.indexOf(Tokenizer.Expecting.BoldUnderscore) != -1) { // If a Bold statement was already started end it here
                            if (string.isNotEmpty()) tokens.add(Token(string.toString(), tags.toMutableList()))
                            if (expecting.contains(Tokenizer.Expecting.BoldUnderscore)) expecting.remove(Expecting.BoldUnderscore)
                            if (tags.contains(TokenTag.Bold)) tags.remove(TokenTag.Bold)
                            string.clear()
                        } else { // If no Bold statement is started at this point, start a new one.
                            if (tags.isEmpty() && string.isNotEmpty()) tokens.add(Token(string.toString(), mutableListOf(TokenTag.Plain)))
                            else if (string.isNotEmpty()) tokens.add(Token(string.toString(), tags.toMutableList()))
                            expecting.add(Tokenizer.Expecting.BoldUnderscore)
                            if (!tags.contains(TokenTag.Bold)) tags.add(TokenTag.Bold)
                            string.clear()
                        }
                    } else if (index != 0 && strc[index - 1] != '_') { // If previous character is not an underscore, and the next one isn't either treat this as an Italic token
                        if (expecting.indexOf(Tokenizer.Expecting.ItalicUnderscore) != -1) { // If a Italic statement was already started end it here
                            if (string.isNotEmpty()) tokens.add(Token(string.toString(), tags.toMutableList()))
                            if (expecting.contains(Tokenizer.Expecting.ItalicUnderscore)) expecting.remove(Expecting.ItalicUnderscore)
                            if (tags.contains(TokenTag.Italic)) tags.remove(TokenTag.Italic)
                            string.clear()
                        } else { // If no Italic statement is started at this point, start a new one.
                            if (tags.isEmpty() && string.isNotEmpty()) tokens.add(Token(string.toString(), mutableListOf(TokenTag.Plain)))
                            else if (string.isNotEmpty()) tokens.add(Token(string.toString(), tags.toMutableList()))
                            expecting.add(Tokenizer.Expecting.ItalicUnderscore)
                            if (!tags.contains(TokenTag.Italic)) tags.add(TokenTag.Italic)
                            string.clear()
                        }
                    } else string.append(c)
                }
                '*' -> { // This is the same as the previous Case but using Asterisks instead of Underscores
                    if (index != strc.lastIndex && strc[index + 1] == '*')
                    else if (index != 0 && strc[index - 1] == '*') {
                        if (expecting.indexOf(Tokenizer.Expecting.BoldAsterisk) != -1) {
                            if (string.isNotEmpty()) tokens.add(Token(string.toString(), tags.toMutableList()))
                            if (expecting.contains(Tokenizer.Expecting.BoldAsterisk)) expecting.remove(Expecting.BoldAsterisk)
                            if (tags.contains(TokenTag.Bold)) tags.remove(TokenTag.Bold)
                            string.clear()
                        } else {
                            if (tags.isEmpty() && string.isNotEmpty()) tokens.add(Token(string.toString(), mutableListOf(TokenTag.Plain)))
                            else if (string.isNotEmpty()) tokens.add(Token(string.toString(), tags.toMutableList()))
                            expecting.add(Tokenizer.Expecting.BoldAsterisk)
                            if (!tags.contains(TokenTag.Bold)) tags.add(TokenTag.Bold)
                            string.clear()
                        }
                    } else if (index != 0 && strc[index - 1] != '*') {
                        if (expecting.indexOf(Tokenizer.Expecting.ItalicAsterisk) != -1) {
                            if (string.isNotEmpty()) tokens.add(Token(string.toString(), tags.toMutableList()))
                            if (expecting.contains(Tokenizer.Expecting.ItalicAsterisk)) expecting.remove(Expecting.ItalicAsterisk)
                            if (tags.contains(TokenTag.Italic)) tags.remove(TokenTag.Italic)
                            string.clear()
                        } else {
                            if (tags.isEmpty() && string.isNotEmpty()) tokens.add(Token(string.toString(), mutableListOf(TokenTag.Plain)))
                            else if (string.isNotEmpty()) tokens.add(Token(string.toString(), tags.toMutableList()))
                            expecting.add(Tokenizer.Expecting.ItalicAsterisk)
                            if (!tags.contains(TokenTag.Italic)) tags.add(TokenTag.Italic)
                            string.clear()
                        }
                    } else string.append(c)
                }
                else -> { // If the character isn't one of the special cases, just add it to the buffer
                    string.append(c)
                    if(index == strc.lastIndex) tokens.add(Token(string.toString(), tags)) // And also if it happens to be the last character on the string might as well add the buffer as a token.
                }
            }
        }
        return tokens.toTypedArray()
    }

    class Token(val str: String, val tags: List<TokenTag>) {
        fun contains(str: String): Boolean {
            if (tags.contains(str.toTokenTag()) != null && tags.contains(str.toTokenTag())) return true
            return false
        }
    }

    enum class Expecting {
        ItalicUnderscore,
        ItalicAsterisk,
        BoldUnderscore,
        BoldAsterisk
    }

    sealed class TokenTag {
        object Plain : TokenTag()
        object Italic : TokenTag()
        object Bold : TokenTag()
    }

    fun String.toTokenTag(): TokenTag? = when (this) {
        "Plain" -> Tokenizer.TokenTag.Plain
        "Italic" -> Tokenizer.TokenTag.Italic
        "Bold" -> Tokenizer.TokenTag.Bold
        else -> null
    }
}




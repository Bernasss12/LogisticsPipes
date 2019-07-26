package logisticspipes.utils.font.renderer

import java.awt.Color

interface IToken{
    var str: String
}

class Token(override var str: String, val tags: MutableList<Tokenizer.TokenTag>, clr: Color = Color.WHITE) : IToken {
    var color = clr
}

class TokenHeader : IToken {
    var tokens: MutableList<IToken> = mutableListOf()
    override var str: String = ""
    fun clone():TokenHeader {
        val th = TokenHeader()
        th.tokens = this.tokens.toMutableList()
        return th
    }
}

// ToDo class TokenImage(override var str: String, var url: String) : IToken

// ToDo class TokenLink(var tokens: MutableList<Token>, var url: String) : IToken

class TokenLineBreak(override var str : String = "") : IToken
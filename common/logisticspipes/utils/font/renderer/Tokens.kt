package logisticspipes.utils.font.renderer

import java.awt.Color

interface IToken

class Token(val str: String, val tags: MutableList<Tokenizer.TokenTag>, val color: Color) : IToken

class TokenHeader(val tokens: MutableList<Token>) : IToken

class TokenImage(val tokens: MutableList<Token>, val url: String) : IToken

class TokenLink(val tokens: MutableList<Token>, val url: String) : IToken

class TokenLineBreak : IToken
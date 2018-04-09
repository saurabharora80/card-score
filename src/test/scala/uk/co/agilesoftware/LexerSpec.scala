package uk.co.agilesoftware

import org.scalatest.{FlatSpec, Matchers}
import uk.co.agilesoftware.TokenType.TokenType

import scala.collection.mutable

class LexerSpec extends FlatSpec with Matchers {
  import TokenType._
  Seq("""sum = a + b;""" -> Seq(Token(VARIABLE, "sum"),Token(ASSIGN, "="),Token(VARIABLE, "a"), Token(OPERATOR, "+"),
    Token(VARIABLE, "b"),
    Token(PUNCTUATION, ";")),
    """sum = 22 + 33;""" -> Seq(Token(VARIABLE, "sum"),Token(ASSIGN, "="),Token(NUMBER, "22"), Token(OPERATOR, "+"),
      Token(NUMBER, "33"),
      Token(PUNCTUATION, ";")),
    """stringSum = "one two" + "three";""" -> Seq(Token(VARIABLE, "stringSum"),Token(ASSIGN, "="), Token(STRING, "one two"),
      Token(OPERATOR, "+"), Token(STRING, "three"),Token(PUNCTUATION, ";")),
    """value = true;""" -> Seq(Token(VARIABLE, "value"),Token(ASSIGN, "="), Token(BOOLEAN, "true"),Token(PUNCTUATION, ";"))
  ) foreach {
    case (in, out) =>
      "lexer" should s"""tokenize "$in" """ in {
        Lexer(in) should contain theSameElementsAs out
      }
  }

  Seq(
    "true" -> TokenType.BOOLEAN,
    "false" -> TokenType.BOOLEAN,
    "if" -> TokenType.KEYWORD,
    "else" -> TokenType.KEYWORD,
    "lambda" -> TokenType.KEYWORD
  ).foreach {
    case (v, tokenType) =>
      s"$v" should s"be tokenized as $tokenType" in {
        VarBoolOrKeyword(v) shouldBe Token(tokenType, v)
      }
  }

}


object TokenType extends Enumeration {
  type TokenType = Value
  val VARIABLE, KEYWORD, ASSIGN, OPERATOR, NUMBER, STRING, PUNCTUATION, BOOLEAN = Value
}

case class Token(`type`: TokenType, value: String)

object VarBoolOrKeyword {
  import TokenType._
  def apply(value: String): Token = {
    value match {
      case v if v.equals("true") || v.equals("false") => Token(BOOLEAN, value)
      case v if v.equals("if") || v.equals("else") || v.equals("lambda") => Token(KEYWORD, value)
      case v => Token(VARIABLE, v)
    }
  }
}

object Lexer {

  private implicit class CharValidations(char: Char) {
    def isStartOfString: Boolean = char == '"'
    def isEndOfString: Boolean = isStartOfString
    def isOperator: Boolean = Seq('+').contains(char)
    def isAssignment: Boolean = char == '='
    def isPunctuation: Boolean = Seq(';').contains(char)
  }

  private def collectUntil(startChar: Char, predicate: BufferedIterator[Char] => Boolean)(implicit iterator: BufferedIterator[Char]): String = {
    val characters: mutable.MutableList[Char] = mutable.MutableList(startChar)
    while(iterator.hasNext && predicate(iterator)) {
      characters += iterator.next
    }
    //Read the closing '"' of the String literal
    if(startChar.isStartOfString) characters += iterator.next()
    characters.mkString
  }

  def apply(code: String): List[Token] = {
    import TokenType._
    implicit val iterator: BufferedIterator[Char] = code.iterator.buffered
    var values = collection.mutable.MutableList.empty[Token]
    while(iterator.hasNext) {
      val ch = iterator.next
      ch match {
        case x if x.isWhitespace =>
        case x if x.isDigit => values += Token(NUMBER, collectUntil(x, _.head.isDigit))
        case x if x.isOperator => values += Token(OPERATOR, x.toString)
        case x if x.isAssignment => values += Token(ASSIGN, x.toString)
        case x if x.isPunctuation => values += Token(PUNCTUATION, x.toString)
        case x if x.isStartOfString => values += Token(STRING, collectUntil(x, !_.head.isEndOfString).replaceAll("\"", ""))
        case x => values += VarBoolOrKeyword(collectUntil(x, _.head.isLetter))
      }
    }
    values.toList
  }
}



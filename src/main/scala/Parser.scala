package xyz.hyperreal.backslash

import scala.collection.mutable.ListBuffer
import util.parsing.input.{PagedSeq, PagedSeqReader, Position, Reader}


class Parser( commands: Map[String, Command] ) {

  type Input = Reader[Char]

  def parse( src: io.Source ): AST =
    parseBlock( new PagedSeqReader(PagedSeq.fromSource(src)) ) match {
      case (r1, b) if r1 atEnd => b
      case (r1, _) => problem( r1, s"expected end of input: $r1" )
    }

  def parseBlock( r: Input, v: Vector[StatementAST] = Vector() ): (Input, BlockStatementAST) =
    if (r atEnd)
      (r, BlockStatementAST( v ))
    else if (r.first == '}')
      (r rest, BlockStatementAST( v ))
    else
      parseStatement( r ) match {
        case (r1, s) => parseBlock( r1, v :+ s )
      }

  def parseStatic( r: Input, buf: StringBuilder = new StringBuilder ): (Input, StatementAST) =
    if (r atEnd)
      (r, StaticStatementAST( buf toString ))
    else
      r first match {
        case '\\' =>
          r.rest.first match {
            case c@('\\'|'{'|'}') =>
              buf += c
              parseStatic( r.rest.rest, buf )
            case _ => (r, StaticStatementAST( buf toString ))
          }
        case '}' => (r, StaticStatementAST( buf toString ))
        case c =>
          buf += c
          parseStatic( r.rest, buf )
      }

  def parseStatement( r: Input ): (Input, StatementAST) =
    r first match {
      case '\\' => parseCommand( r.rest )
      case _ => parseStatic( r )
    }

  def consume( r: Input, set: Char => Boolean, buf: StringBuilder = new StringBuilder ): (Input, String) =
    if (r atEnd)
      (r, buf toString)
    else
      r first match {
        case c if set( c ) =>
          buf += c
          consume( r.rest, set, buf )
        case _ => (r, buf toString)
      }

  def consumeDelimited( r: Input, delim: Char ): (Input, String) = {
    val (r1, s) = consume( r, _ != delim )

    (r1.rest, s)
  }

  def parseName( r: Input ): (Input, String) = consume( r, _.isLetterOrDigit )

  def parseString( r: Input ): (Input, String) =
    r.first match {
      case '"' => consumeDelimited( r.rest, '"' )
      case '\'' => consumeDelimited( r.rest, '\'' )
      case _ => consume( r, !_.isWhitespace )
    }

  def parseRenderedArgument( r: Input ): (Input, StatementAST) = {
    val r1 = skipSpace( r )

    if (r1 atEnd)
      problem( r1, "expected command argument" )

    r1 first match {
      case '{' => parseBlock( r1.rest )
      case _ =>
        val (r2, s) = parseString( r1 )

        (r2, StaticStatementAST( s ))
    }
  }

  def parseStaticArgument( r: Input ): (Input, String) = {
    val r1 = skipSpace( r )

    if (r1 atEnd)
      problem( r1, "expected command argument" )

    r1 first match {
      case '{' => consumeDelimited( r1.rest, '}' )
      case _ => parseString( r1 )
    }
  }

  def parseArguments( r: Input, n: Int, buf: ListBuffer[StatementAST] = new ListBuffer[StatementAST] ): (Input, List[StatementAST]) = {
    if (n == 0)
      (r, buf toList)
    else {
      val (r1, s) = parseRenderedArgument( r )

      buf += s
      parseArguments( r1, n - 1, buf )
    }
  }

  def skipSpace( r: Input ): Input = consume( r, _.isWhitespace )._1

  def parseCommand( r: Input ): (Input, StatementAST) = {
    val (r1, name) = parseName( r )

    name match {
      case "if" =>
        val (r2, s) = parseStaticArgument( r1 )
        val (r3, yes) = parseRenderedArgument( r2 )

        (r3, IfStatementAST( List((VariableExpressionAST(s), yes)), None ))
      case _ =>
        commands get name match {
          case None => (r1, VariableStatementAST( name ))
          case Some( c ) =>

            val (r2, args) = parseArguments( r1, c.arity )

            (r2, CommandStatementAST( c, args ))
        }
    }
  }

}
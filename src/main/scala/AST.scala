package xyz.hyperreal.backslash

import scala.util.parsing.input.Position


trait AST

case class IfAST( cond: Seq[(AST, AST)], els: Option[AST] ) extends AST
case class MatchAST( expr: AST, cases: Seq[(AST, AST)], els: Option[AST] ) extends AST
case class UnlessAST( cond: AST, body: AST, els: Option[AST] ) extends AST
case class ForAST( pos: Position, expr: AST, body: AST, els: Option[AST] ) extends AST
case class GroupAST( statements: Seq[AST] ) extends AST
case class LiteralAST( v: Any ) extends AST
case class VariableAST(name: String ) extends AST
case class CommandAST( pos: Position, c: Command, args: List[AST], optional: Map[String, AST] ) extends AST
case class MacroAST( mac: Macro, args: Seq[AST] ) extends AST
case class BreakAST( pos: Position ) extends AST
case class ContinueAST( pos: Position ) extends AST
case class AndAST( left: AST, right: AST ) extends AST
case class OrAST( left: AST, right: AST ) extends AST
case class DotAST( epos: Position, expr: AST, kpos: Position, key: AST ) extends AST
case class SetAST( v: String, expr: AST ) extends AST
case class InAST( cpos: Position, v: String, epos: Position, expr: AST ) extends AST
case class NotAST( expr: AST ) extends AST
case class SeqAST( seq: Vector[AST] ) extends AST
case class ObjectAST( seq: Vector[AST] ) extends AST

case class Macro( parameters: Vector[String], var body: AST )

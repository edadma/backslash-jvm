package xyz.hyperreal

import java.io.File

import scala.util.parsing.input.{Position, Reader}


package object backslash {

  def problem( r: Reader[Char], error: String ): Nothing = problem( r.pos, error )

  def problem( pos: Position, error: String ) =
    if (pos eq null)
      sys.error( error )
    else
      sys.error( pos.line + ": " + error + "\n" + pos.longString )

  case object nil {
    override def toString = ""
  }

  def docroot( name: String, settings: Map[Symbol, Any] ) = new File( settings('docroot).toString, name )

  def round( n: BigDecimal, scale: Int, settings: Map[Symbol, Any] ) =
    n.setScale( scale, settings('roundingMode).asInstanceOf[BigDecimal.RoundingMode.Value] )

  def truthy( a: Any ) = a != nil && a != false

  def falsy( a: Any ) = !truthy( a )

  def display( a: Any ): String =
    a match {
      case l: collection.Seq[_] => l map qdisplay mkString ("[", ", ", "]")
      case m: collection.Map[_, _] => m map { case (k, v) => qdisplay(k) + "=>" + qdisplay(v) } mkString ("{", ", ", "}")
      case s => String.valueOf( s )
    }

  def qdisplay( a: Any ): String =
    a match {
      case s: String => '"' + s + '"'
      case `nil` => "<nil>"
      case _ => display( a )
    }

}
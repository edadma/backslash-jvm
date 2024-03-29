package xyz.hyperreal.backslash

import java.io.File
import java.time.format.{DateTimeFormatter, FormatStyle}

import scala.collection.mutable

import xyz.hyperreal.args.Options

import xyz.hyperreal.json.DefaultJSONReader


object Main extends App {

  val config =
    Map(
      "today" -> "MMMM d, y",
      "include" -> ".",
      "rounding" -> "HALF_EVEN"
    )
  val assigns = new mutable.HashMap[String, Any]
  var templateFile: File = _

  def usage = {
    println(
        """
          |Backslash v0.4.23
          |
          |Usage:  java -jar backslash-0.4.23.jar <options> <template>
          |
          |Options:  --help              display this help and exit
          |          -s <name> <string>  assign <string> to variable <name>
          |          -n <name> <number>  assign <number> to variable <name>
          |
          |Note:  <template> may be -- meaning read from standard input
        """.trim.stripMargin )
    sys.exit
  }

  def json( src: io.Source ) =
    for ((k: String, v) <- DefaultJSONReader.fromString( src mkString ).asInstanceOf[Map[String, Any]])
      assigns(k) = v

  def run( src: io.Source ): Unit = {
    val parser = new Parser( Command.standard )
    val renderer = new Renderer( parser, config )

    renderer.render( parser.parse(src), assigns, Console.out )
  }

  if (args isEmpty)
    usage

  Options( args ) {
    case "-s" :: name :: s :: t =>
      assigns(name) = s
      t
    case "-n" :: name :: n :: t =>
      number( n ) match {
        case None => sys.error( s"not a number: $n" )
        case Some( v ) => assigns(name) = v
      }

      t
    case "-j" :: "--" :: t =>
      json( io.Source.stdin )
      t
    case "-j" :: file :: t if !file.matches( """\s*\{.*""" ) =>
      val jsonFile = new File( file )

      if (jsonFile.exists && jsonFile.isFile && jsonFile.canRead) {
        json( io.Source.fromFile(jsonFile) )
      } else
        sys.error( s"error reading file: $file" )

      t
    case "-j" :: s :: t =>
      json( io.Source.fromString(s) )
      t
    case "--help" :: _ =>
      usage
      Nil
    case "--" :: Nil =>
      run( io.Source.stdin )
      Nil
    case s :: _ if s startsWith "-" => sys.error( s"invalid switch $s" )
    case file :: Nil =>
      templateFile = new File( file )

      if (templateFile.exists && templateFile.isFile && templateFile.canRead) {
        run( io.Source.fromFile(templateFile) )
        Nil
      } else
        sys.error( s"error reading file: $file" )
  }

}
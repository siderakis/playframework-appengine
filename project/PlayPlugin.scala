package play

import play.router.RoutesCompiler.compile
import play.router.RoutesCompiler.GeneratedSource
import play.router.RoutesCompiler.RoutesCompilationError
import sbt._
import sbt.Keys._


object PlayProject extends Plugin with PlayKeys with PlayCommands with PlaySettings

trait PlayCommands {

  val RouteFiles = (state: State, confDirectory: File, generatedDir: File, additionalImports: Seq[String]) => {
    val scalaRoutes = generatedDir ** "routes_*.scala"
    scalaRoutes.get.map(GeneratedSource).foreach(_.sync())
    try { {
      (confDirectory * "*.routes").get ++ (confDirectory * "routes").get
    }.map {
      routesFile =>
        compile(routesFile, generatedDir, additionalImports)
    }
    } catch {
      case RoutesCompilationError(source, message, line, column) => {
        throw new RuntimeException("Error with Routes file: " + message) // reportCompilationError(state, RoutesCompilationException(source, message, line, column.map(_ - 1)))
      }
      case e => throw e
    }

    scalaRoutes.get.map(_.getAbsoluteFile)

  }
}


trait PlayKeys {
  val confDirectory = SettingKey[File]("play-conf")

  val routesImport = SettingKey[Seq[String]]("play-routes-imports")

}

trait PlaySettings {
  this: PlayCommands with PlayKeys =>

  lazy val defaultPlaySettings = Seq[Setting[_]](

    routesImport := Seq.empty[String],
    confDirectory <<= baseDirectory / "src/main/conf",

    sourceGenerators in Compile <+= (state, confDirectory, sourceManaged in Compile, routesImport) map RouteFiles
  )

}

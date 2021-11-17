import sbt.CompileOrder
import sbt.Keys._

object BuildSettings {

  lazy val basicSettings = Seq(
    scalaVersion := "2.11.12",
    organization := "Steve",
    organizationName := "Steve",
    version := "1.0",
    resolvers := Dependencies.resolverSettings,
    compileOrder := CompileOrder.JavaThenScala)
}

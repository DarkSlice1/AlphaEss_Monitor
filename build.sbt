import BuildSettings._
import Dependencies._

fork in (Test, run) := true


lazy val root = Project("alphaess_monitor", file("."))
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .settings(basicSettings: _*)
  .settings(sources in (Compile, doc) := Seq())
  .settings(libraryDependencies ++= coreDependencies)
  .settings(mainClass in Compile := Some("Main"))



import BuildSettings._
import Dependencies._

//javaOptions <++= AspectjKeys.weaverOptions in Aspectj
Test / run / fork := true


lazy val root = Project("alphaess_monitor", file("."))
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .settings(basicSettings: _*)
  .settings((Compile / doc / scalacOptions):= Seq())
  .settings(libraryDependencies ++= coreDependencies)
  .settings(Compile / mainClass := Some("Main"))
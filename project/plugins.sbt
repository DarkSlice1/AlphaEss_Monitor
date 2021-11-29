
resolvers += Classpaths.typesafeReleases
resolvers += Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")

lazy val sbtNativePackagerVersion = "1.1.1"
lazy val sbtAspectJRunVersion = "1.1.2"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.16")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % sbtNativePackagerVersion)

addSbtPlugin("io.kamon" % "sbt-aspectj-runner" % sbtAspectJRunVersion)

addSbtPlugin("com.typesafe.sbt" % "sbt-aspectj" % "0.10.6")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")
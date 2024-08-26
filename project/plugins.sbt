
resolvers += Classpaths.typesafeReleases
resolvers += Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")

lazy val sbtNativePackagerVersion = "1.10.4"
lazy val sbtAspectJRunVersion = "1.1.2"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.5")

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % sbtNativePackagerVersion)

addSbtPlugin("io.kamon" % "sbt-aspectj-runner" % sbtAspectJRunVersion)

addSbtPlugin("com.liyutech" % "sbt-aspectj" % "0.12.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")
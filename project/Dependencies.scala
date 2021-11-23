import sbt._

object Dependencies {

  private val kamon_core            = "io.kamon"      %% "kamon-core"               % "2.3.1"
  private val kamon_scala           = "io.kamon"      %% "kamon-scala"              % "0.6.7"
  private val kamon_akka            = "io.kamon"      %% "kamon-akka"               % "2.3.1"
  private val kamon_akka_remote     = "io.kamon"      %% "kamon-akka-remote-2.4"    % "1.1.0"
  private val kamon_datadog         = "io.kamon"      %% "kamon-datadog"            % "2.3.1"
  private val kamon_system_metrics  = "io.kamon"      %% "kamon-system-metrics"     % "1.0.1"
  private val aspectj_weaver        = "org.aspectj"   % "aspectjweaver"             % "1.8.9"

  //  private val kamon_core = "io.kamon" %% "kamon-bundle" % "2.0.1"
//  private val kamon_reporter = "io.kamon" %% "kamon-apm-reporter" % "2.0.0"

  private val jackson_core          = "com.fasterxml.jackson.core"      % "jackson-core"            % "2.6.7"
  private val jackson_databind      = "com.fasterxml.jackson.core"      % "jackson-databind"        % "2.6.7"
  private val jackson_datatype_jdk8 = "com.fasterxml.jackson.datatype"  % "jackson-datatype-jsr310" % "2.6.7"
  private val jackson_module        = "com.fasterxml.jackson.module"    %% "jackson-module-scala"   % "2.6.7"
  private val json4s_base           = "org.json4s"                      %% "json4s-native"          % "3.4.0"
  private val json4s_jackson        = "org.json4s"                      %% "json4s-jackson"         % json4s_base.revision

  private val gson                  = "com.google.code.gson"            % "gson"                    % "2.8.9"
  private val apache_http           = "org.apache.httpcomponents"       % "httpclient"              % "4.5.13"
  private val squareup              = "com.squareup.okhttp"             % "okhttp"                  % "2.7.5"
  private val bouncycastle          = "org.bouncycastle"                % "bcprov-ext-jdk16"        % "1.46"


  private val kamonDeps = Seq(kamon_core, kamon_akka, kamon_akka_remote, kamon_scala, kamon_datadog, kamon_system_metrics, aspectj_weaver)
  private val JSONDeps = Seq(jackson_core, jackson_databind, jackson_module, jackson_datatype_jdk8, json4s_base, json4s_jackson)


  val coreDependencies =  kamonDeps ++ JSONDeps  ++ Seq(gson,apache_http,squareup,bouncycastle)//++ Seq()

  lazy val resolverSettings = Seq()
}



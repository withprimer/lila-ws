import com.typesafe.sbt.packager.docker._

name := "lila-ws"

version := "3.0"

lazy val `lila-ws` = (project in file("."))
  .enablePlugins(JavaAppPackaging)

val akkaVersion          = "2.6.19"
val kamonVersion         = "2.5.3"
val nettyVersion         = "4.1.77.Final"
val reactivemongoVersion = "1.1.0-RC4"

val os = sys.props.get("os.name") match {
  case Some(osName) if osName.toLowerCase.startsWith("mac") => "osx"
  case _                                                    => "linux"
}

scalaVersion := "3.1.2"

libraryDependencies += "org.reactivemongo" %% "reactivemongo"          % reactivemongoVersion
libraryDependencies += "org.reactivemongo" %% "reactivemongo-bson-api" % reactivemongoVersion
// libraryDependencies += "org.reactivemongo" % "reactivemongo-shaded-native" % s"$reactivemongoVersion-$os-x86-64"
libraryDependencies += "io.lettuce" % "lettuce-core"                 % "6.1.8.RELEASE"
libraryDependencies += "io.netty"   % "netty-handler"                % nettyVersion
libraryDependencies += "io.netty"   % "netty-codec-http"             % nettyVersion
libraryDependencies += "io.netty"   % "netty-transport-native-epoll" % nettyVersion classifier "linux-x86_64"
libraryDependencies += "com.github.ornicar" %% "scalalib"         % "8.0.2"
libraryDependencies += "org.lichess"        %% "scalachess"       % "11.0.1"
libraryDependencies += "com.typesafe.akka"  %% "akka-actor-typed" % akkaVersion
// libraryDependencies += "com.typesafe.akka"          %% "akka-slf4j"       % akkaVersion
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5"
libraryDependencies += "joda-time"                   % "joda-time"       % "2.10.14"
libraryDependencies += "com.github.blemale"         %% "scaffeine"       % "5.2.0" % "compile"
libraryDependencies += "ch.qos.logback"              % "logback-classic" % "1.2.11"
libraryDependencies += "com.typesafe.play"          %% "play-json"       % "2.10.0-RC6"
libraryDependencies += "io.kamon"                   %% "kamon-core"      % kamonVersion
libraryDependencies += "io.kamon"                   %% "kamon-influxdb"  % kamonVersion
// libraryDependencies += "io.kamon"                   %% "kamon-system-metrics"         % kamonVersion
libraryDependencies += "com.softwaremill.macwire" %% "macros" % "2.5.7" % "provided"
libraryDependencies += "com.roundeights"          %% "hasher" % "1.3.0"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += "lila-maven" at "https://raw.githubusercontent.com/ornicar/lila-maven/master"

scalacOptions := Seq(
  "-encoding",
  "utf-8",
  "-rewrite",
  "-source:future-migration",
  "-indent",
  "-explaintypes",
  "-feature",
  "-language:postfixOps"
  // Warnings as errors!
  // "-Xfatal-warnings",
)

javaOptions ++= Seq("-Xms32m", "-Xmx256m")

Compile / doc / sources := Seq.empty

Compile / packageDoc / publishArtifact := false

/* scalafmtOnCompile := true */


// docker
dockerBaseImage := "openjdk:11-jdk"
dockerExposedPorts += 9664


dockerCommands := dockerCommands.value.filterNot {

  // ExecCmd is a case class, and args is a varargs variable, so you need to bind it with @
  case ExecCmd("ENTRYPOINT", args @ _*) => true
  case ExecCmd("CMD",args @ _*) => true

  // don't filter the rest; don't filter out anything that doesn't match a pattern
  case cmd                       => false
}

dockerCommands += Cmd("ENTRYPOINT", "/opt/docker/bin/lila-ws -Dconfig.file=app.conf")

// dockerCommands := Seq(
//   Cmd("FROM", "openjdk:11-jdk"),
//   Cmd("LABEL", s"""MAINTAINER="${maintainer.value}""""),
//   Cmd(RUN,List(id, -u, demiourgos728, 1>/dev/null, 2>&1, ||, ((, getent, group, 0, 1>/dev/null, 2>&1, ||, (, type, groupadd, 1>/dev/null, 2>&1, &&, groupadd, -g, 0, root, ||, addgroup, -g, 0, -S, root, )), &&, (, type, useradd, 1>/dev/null, 2>&1, &&, useradd, --system, --create-home, --uid, 1001, --gid, 0, demiourgos728, ||, adduser, -S, -u, 1001, -G, root, demiourgos728, ))))
//   ExecCmd("CMD", "echo", "Hello, World from Docker")
// )

// [info] * Cmd(WORKDIR,WrappedArray(/opt/docker))
// [info] * Cmd(COPY,WrappedArray(2/opt /2/opt))
// [info] * Cmd(COPY,WrappedArray(4/opt /4/opt))
// [info] * Cmd(USER,WrappedArray(root))
// [info] * ExecCmd(RUN,List(chmod, -R, u=rX,g=rX, /2/opt/docker))
// [info] * ExecCmd(RUN,List(chmod, -R, u=rX,g=rX, /4/opt/docker))
// [info] * ExecCmd(RUN,List(chmod, u+x,g+x, /4/opt/docker/bin/lila-ws))
// [info] * DockerStageBreak
// [info] * Cmd(FROM,WrappedArray(openjdk:11-jdk, as, mainstage))
// [info] * Cmd(USER,WrappedArray(root))
// [info] * Cmd(RUN,List(id, -u, demiourgos728, 1>/dev/null, 2>&1, ||, ((, getent, group, 0, 1>/dev/null, 2>&1, ||, (, type, groupadd, 1>/dev/null, 2>&1, &&, groupadd, -g, 0, root, ||, addgroup, -g, 0, -S, root, )), &&, (, type, useradd, 1>/dev/null, 2>&1, &&, useradd, --system, --create-home, --uid, 1001, --gid, 0, demiourgos728, ||, adduser, -S, -u, 1001, -G, root, demiourgos728, ))))
// [info] * Cmd(WORKDIR,WrappedArray(/opt/docker))
// [info] * Cmd(COPY,WrappedArray(--from=stage0 --chown=demiourgos728:root /2/opt/docker /opt/docker))
// [info] * Cmd(COPY,WrappedArray(--from=stage0 --chown=demiourgos728:root /4/opt/docker /opt/docker))
// [info] * Cmd(EXPOSE,WrappedArray(9664))
// [info] * Cmd(USER,WrappedArray(1001:0))
// [info] * ExecCmd(ENTRYPOINT,List(/opt/docker/bin/lila-ws))
// [info] * ExecCmd(CMD,List())
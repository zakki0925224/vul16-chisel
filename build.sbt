scalaVersion := "2.13.15"
name         := "vul16-chisel"
organization := "io.github.zakki0925224"
version      := "1.0"

logLevel := Level.Info

val chiselVersion = "6.6.0"

libraryDependencies ++= Seq(
    "org.chipsalliance" %% "chisel"     % chiselVersion,
    "edu.berkeley.cs"   %% "chiseltest" % "6.0.0" % Test
)
scalacOptions ++= Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    "-Ymacro-annotations"
)
addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full)

import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "2.1.0-SNAPSHOT"
ThisBuild / organization     := "eu.falcraft"
ThisBuild / organizationName := "falcraft"

mainClass := Some("example.Hello")

lazy val root = (project in file("."))
  .settings(
    name := "Live",
    libraryDependencies ++= Seq(
        scalaTest % Test,
        "org.spigotmc" % "spigot-api" % "1.13.2-R0.1-SNAPSHOT" % "provided",
        "org.bukkit" % "bukkit" % "1.13.2-R0.1-SNAPSHOT" % "provided"
    ),
    resolvers ++= Seq(
      "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
      "bungeecord-repo" at "https://oss.sonatype.org/content/repositories/snapshots/"
    )
  )

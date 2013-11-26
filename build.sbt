sbtPlugin := true

name := "sbt-antlr4"

organization := "com.simplytyped"

version := "0.5"

publishTo := Some {
  val target = if(version.value contains "SNAPSHOT") "snapshots" else "releases"
  Resolver.file("simplytyped.com", new File("/Users/ihji/Works/simplytyped.github.io",target))
}

sbtPlugin := true

name := "sbt-antlr4"

organization := "com.simplytyped"

version := "0.7.8"

publishTo := Some {
  val target = if(version.value contains "SNAPSHOT") "snapshots" else "releases"
  Resolver.file("simplytyped.com", new File("/Users/ihji/Works/simplytyped.github.io/repo",target))
}

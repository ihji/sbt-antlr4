# sbt-antlr4

This plugin provides an ability to run antlr4 when compiling in sbt 0.13.

Put your .g4 files in `src/main/antlr4` directory and make `project/sbt-antlr4.sbt`
file with the following contents:

    resolvers += "simplytyped.com" at "http://simplytyped.com/repo/releases"

    addSbtPlugin("com.simplytyped" % "sbt-antlr4" % "0.5")

And, add `antlr4Settings` to your `build.sbt` file.

    antlr4Settings
 

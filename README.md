# sbt-antlr4

[![Build Status](https://github.com/ihji/sbt-antlr4/actions/workflows/scala.yml/badge.svg)](https://github.com/ihji/sbt-antlr4/actions/workflows/scala.yml)

This plugin provides an ability to run antlr4 when compiling in sbt 1.1.x and 0.13.x.

## How to use

Put your .g4 files in `src/main/antlr4` directory and make `project/sbt-antlr4.sbt`
file with the following contents:

    // sbt 1.1.x
    addSbtPlugin("com.simplytyped" % "sbt-antlr4" % "0.8.3")

    // sbt 0.13.x
    addSbtPlugin("com.simplytyped" % "sbt-antlr4" % "0.7.13")

And, enable the plugin in your `build.sbt` file.

    // sbt 1.1.x
    enablePlugins(Antlr4Plugin)

    // sbt 0.13.x
    antlr4Settings

Now, whenever you invoke `sbt compile` the ANTLR artifacts will be written to
`./target/scala-XX.YY/sources_managed/main/antlr4` (depending on your Scala version).

## Settings

You can select an antl4 version with:

    antlr4Version in Antlr4 := "4.8-1" // default: 4.8-1

`-package` option can be defined by the following setting:

    antlr4PackageName in Antlr4 := Some("com.simplytyped")

You can also adjust `-listener`, `-no-listener`, `-visitor`, `-no-visitor`, `-Werror` options:

    antlr4GenListener in Antlr4 := true // default: true

    antlr4GenVisitor in Antlr4 := false // default: false

    antlr4TreatWarningsAsErrors in Antlr4 := true // default: false
 
## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

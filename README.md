# sbt-antlr4

This plugin provides an ability to run antlr4 when compiling in sbt 0.13.

## How to use

Put your .g4 files in `src/main/antlr4` directory and make `project/sbt-antlr4.sbt`
file with the following contents:

    resolvers += "simplytyped.com" at "http://simplytyped.com/repo/releases"

    addSbtPlugin("com.simplytyped" % "sbt-antlr4" % "0.5")

And, add `antlr4Settings` to your `build.sbt` file.

    antlr4Settings

## Settings

`-package` option can be given by the following setting:

    packageName in Antlr4 := Some("com.simplytyped")
 
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

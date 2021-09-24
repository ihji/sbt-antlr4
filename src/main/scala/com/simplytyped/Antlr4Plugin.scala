/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * License); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.simplytyped

import sbt._
import Keys._

import sbt.internal.io.Source
import scala.sys.process.Process

object Antlr4Plugin extends AutoPlugin {
  object autoImport {
    val Antlr4 = config("antlr4")
    val antlr4Version = settingKey[String]("Version of antlr4")
    val antlr4Generate = taskKey[Seq[File]]("Generate classes from antlr4 grammars")
    val antlr4RuntimeDependency = settingKey[ModuleID]("Library dependency for antlr4 runtime")
    val antlr4Dependency = settingKey[ModuleID]("Build dependency required for parsing grammars")
    val antlr4PackageName = settingKey[Option[String]]("Name of the package for generated classes")
    val antlr4GenListener = settingKey[Boolean]("Generate listener")
    val antlr4GenVisitor = settingKey[Boolean]("Generate visitor")
    val antlr4TreatWarningsAsErrors = settingKey[Boolean]("Treat warnings as errors when generating parser")
    val javaBinaryPath = settingKey[Option[String]]("Path to Java binary")
  }
  import autoImport._

  private val antlr4BuildDependency = settingKey[ModuleID]("Build dependency required for parsing grammars, scoped to plugin")

  def antlr4GeneratorTask : Def.Initialize[Task[Seq[File]]] = Def.task {
    val targetBaseDir = (Antlr4 / javaSource).value
    val classpath = (Antlr4 / managedClasspath).value.files
    val log = streams.value.log
    val packageName = (Antlr4 / antlr4PackageName).value
    val listenerOpt = (Antlr4 / antlr4GenListener).value
    val visitorOpt = (Antlr4 / antlr4GenVisitor).value
    val warningsAsErrorOpt = (Antlr4 / antlr4TreatWarningsAsErrors).value
    val javaBinaryPathOpt = (Antlr4 / javaBinaryPath).value
    val cachedCompile = FileFunction.cached(streams.value.cacheDirectory / "antlr4", FilesInfo.lastModified, FilesInfo.exists) {
      in : Set[File] =>
        runAntlr(
          srcFiles = in,
          targetBaseDir = targetBaseDir,
          classpath = classpath,
          log = log,
          packageName = packageName,
          listenerOpt = listenerOpt,
          visitorOpt = visitorOpt,
          warningsAsErrorOpt = warningsAsErrorOpt,
          javaBinaryPathOpt = javaBinaryPathOpt
        )
    }
    cachedCompile(((Antlr4 / sourceDirectory).value ** "*.g4").get.toSet).toSeq
  }

  def runAntlr(
      srcFiles: Set[File],
      targetBaseDir: File,
      classpath: Seq[File],
      log: Logger,
      packageName: Option[String],
      listenerOpt: Boolean,
      visitorOpt: Boolean,
      warningsAsErrorOpt: Boolean,
      javaBinaryPathOpt: Option[String]) = {
    val targetDir = packageName.map{_.split('.').foldLeft(targetBaseDir){_/_}}.getOrElse(targetBaseDir)
    val baseArgs = Seq("-cp", Path.makeString(classpath), "org.antlr.v4.Tool", "-o", targetDir.toString)
    val packageArgs = packageName.toSeq.flatMap{p => Seq("-package",p)}
    val listenerArgs = if(listenerOpt) Seq("-listener") else Seq("-no-listener")
    val visitorArgs = if(visitorOpt) Seq("-visitor") else Seq("-no-visitor")
    val warningAsErrorArgs = if (warningsAsErrorOpt) Seq("-Werror") else Seq.empty
    val sourceArgs = srcFiles.map{_.toString}
    val args = baseArgs ++ packageArgs ++ listenerArgs ++ visitorArgs ++ warningAsErrorArgs ++ sourceArgs
    val exitCode = Process(javaBinaryPathOpt.getOrElse("java"), args) ! log
    if(exitCode != 0) sys.error(s"Antlr4 failed with exit code $exitCode")
    (targetDir ** "*.java").get.toSet
  }

  override def projectSettings = inConfig(Antlr4)(Seq(
    sourceDirectory := (Compile / sourceDirectory).value / "antlr4",
    javaSource := (Compile / sourceManaged).value / "antlr4",
    managedClasspath := Classpaths.managedJars(configuration.value, classpathTypes.value, update.value),
    antlr4Version := "4.8-1",
    antlr4Generate := antlr4GeneratorTask.value,
    antlr4Dependency := "org.antlr" % "antlr4" % antlr4Version.value,
    antlr4RuntimeDependency := "org.antlr" % "antlr4-runtime" % antlr4Version.value,
    antlr4BuildDependency := antlr4Dependency.value % Antlr4.name,
    antlr4PackageName := None,
    antlr4GenListener := true,
    antlr4GenVisitor := false,
    antlr4TreatWarningsAsErrors := false,
    javaBinaryPath := None
  )) ++ Seq(
    ivyConfigurations += Antlr4,
    Compile / managedSourceDirectories += (Antlr4 / javaSource).value,
    Compile / sourceGenerators += (Antlr4 / antlr4Generate).taskValue,
    watchSources += new Source(sourceDirectory.value, "*.g4", HiddenFileFilter),
    cleanFiles += (Antlr4 / javaSource).value,
    libraryDependencies += (Antlr4 / antlr4BuildDependency).value,
    libraryDependencies += (Antlr4 / antlr4RuntimeDependency).value
  )
}

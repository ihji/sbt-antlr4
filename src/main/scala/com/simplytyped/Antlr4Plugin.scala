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
  }
  import autoImport._

  private val antlr4BuildDependency = settingKey[ModuleID]("Build dependency required for parsing grammars, scoped to plugin")

  def antlr4GeneratorTask : Def.Initialize[Task[Seq[File]]] = Def.task {
    val targetBaseDir = (javaSource in Antlr4).value
    val classpath = (managedClasspath in Antlr4).value.files
    val log = streams.value.log
    val packageName = (antlr4PackageName in Antlr4).value
    val listenerOpt = (antlr4GenListener in Antlr4).value
    val visitorOpt = (antlr4GenVisitor in Antlr4).value
    val warningsAsErrorOpt = (antlr4TreatWarningsAsErrors in Antlr4).value
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
          warningsAsErrorOpt = warningsAsErrorOpt
        )
    }
    cachedCompile(((sourceDirectory in Antlr4).value ** "*.g4").get.toSet).toSeq
  }

  def runAntlr(
      srcFiles: Set[File],
      targetBaseDir: File,
      classpath: Seq[File],
      log: Logger,
      packageName: Option[String],
      listenerOpt: Boolean,
      visitorOpt: Boolean,
      warningsAsErrorOpt: Boolean) = {
    val targetDir = packageName.map{_.split('.').foldLeft(targetBaseDir){_/_}}.getOrElse(targetBaseDir)
    val baseArgs = Seq("-cp", Path.makeString(classpath), "org.antlr.v4.Tool", "-o", targetDir.toString)
    val packageArgs = packageName.toSeq.flatMap{p => Seq("-package",p)}
    val listenerArgs = if(listenerOpt) Seq("-listener") else Seq("-no-listener")
    val visitorArgs = if(visitorOpt) Seq("-visitor") else Seq("-no-visitor")
    val warningAsErrorArgs = if (warningsAsErrorOpt) Seq("-Werror") else Seq.empty
    val sourceArgs = srcFiles.map{_.toString}
    val args = baseArgs ++ packageArgs ++ listenerArgs ++ visitorArgs ++ warningAsErrorArgs ++ sourceArgs
    val exitCode = Process("java", args) ! log
    if(exitCode != 0) sys.error(s"Antlr4 failed with exit code $exitCode")
    (targetDir ** "*.java").get.toSet
  }

  override def projectSettings = inConfig(Antlr4)(Seq(
    sourceDirectory := (sourceDirectory in Compile).value / "antlr4",
    javaSource := (sourceManaged in Compile).value / "antlr4",
    managedClasspath := Classpaths.managedJars(configuration.value, classpathTypes.value, update.value),
    antlr4Version := "4.7.2",
    antlr4Generate := antlr4GeneratorTask.value,
    antlr4Dependency := "org.antlr" % "antlr4" % antlr4Version.value,
    antlr4RuntimeDependency := "org.antlr" % "antlr4-runtime" % antlr4Version.value,
    antlr4BuildDependency := antlr4Dependency.value % Antlr4.name,
    antlr4PackageName := None,
    antlr4GenListener := true,
    antlr4GenVisitor := false,
    antlr4TreatWarningsAsErrors := false
  )) ++ Seq(
    ivyConfigurations += Antlr4,
    managedSourceDirectories in Compile += (javaSource in Antlr4).value,
    sourceGenerators in Compile += (antlr4Generate in Antlr4).taskValue,
    watchSources += new Source(sourceDirectory.value, "*.g4", HiddenFileFilter),
    cleanFiles += (javaSource in Antlr4).value,
    libraryDependencies += (antlr4BuildDependency in Antlr4).value,
    libraryDependencies += (antlr4RuntimeDependency in Antlr4).value
  )
}

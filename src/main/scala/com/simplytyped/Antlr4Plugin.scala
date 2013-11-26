package com.simplytyped

import sbt._
import Keys._

object Antlr4Plugin extends Plugin {
  val Antlr4 = config("antlr4")

  val generate = TaskKey[Seq[File]]("generate")
  val copyTokens = TaskKey[Seq[File]]("copy-tokens")
  val antlr4Dependency = SettingKey[ModuleID]("antlr4-dependency")
  val packageName = SettingKey[Option[String]]("antlr4-package-arg")

  def antlr4GeneratorTask : Def.Initialize[Task[Seq[File]]] = Def.task {
    val cachedCompile = FileFunction.cached(streams.value.cacheDirectory / "antlr4", FilesInfo.lastModified, FilesInfo.exists) {
      in : Set[File] =>
        runAntlr(
          srcFiles = in,
          targetBaseDir = (javaSource in Antlr4).value,
          classpath = (managedClasspath in Compile).value.files,
          log = streams.value.log,
          packageName = (packageName in Antlr4).value
        )
    }
    cachedCompile(((sourceDirectory in Antlr4).value ** "*.g4").get.toSet).toSeq
  }

  def antlr4CopyTokensTask : Def.Initialize[Task[Seq[File]]] = Def.task {
    val srcBase = (javaSource in Antlr4).value
    val tokens = (srcBase ** "*.tokens").get.toSeq
    tokens
  }

  def runAntlr(srcFiles: Set[File], targetBaseDir: File, classpath: Seq[File], log: Logger, packageName: Option[String]) = {
    val targetDir = packageName.map{_.split('.').foldLeft(targetBaseDir){_/_}}.getOrElse(targetBaseDir)
    val baseArgs = Seq("-cp", Path.makeString(classpath), "org.antlr.v4.Tool", "-o", targetDir.toString)
    val packageArgs = packageName.toSeq.flatMap{p => Seq("-package",p)}
    val sourceArgs = srcFiles.map{_.toString}
    val args = baseArgs ++ packageArgs ++ sourceArgs
    val exitCode = Process("java", args) ! log
    if(exitCode != 0) sys.error(s"Antlr4 failed with exit code $exitCode")
    (targetDir ** "*.java").get.toSet
  }

  val antlr4Settings = inConfig(Antlr4)(Seq(
    sourceDirectory <<= (sourceDirectory in Compile) {_ / "antlr4"},
    javaSource <<= (sourceManaged in Compile) {_ / "java"},
    generate <<= antlr4GeneratorTask,
    copyTokens <<= antlr4CopyTokensTask,
    antlr4Dependency := "org.antlr" % "antlr4" % "4.1",
    packageName := None
  )) ++ Seq(
    unmanagedSourceDirectories in Compile <+= (sourceDirectory in Antlr4),
    sourceGenerators in Compile <+= (generate in Antlr4),
    resourceGenerators in Compile <+= (copyTokens in Antlr4),
    cleanFiles <+= (javaSource in Antlr4),
    libraryDependencies <+= (antlr4Dependency in Antlr4)
  )
}

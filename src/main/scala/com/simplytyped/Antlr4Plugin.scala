package com.simplytyped

import sbt._
import Keys._

object Antlr4Plugin extends Plugin {
  val Antlr4 = config("antlr4")

  val antlr4Generate = TaskKey[Seq[File]]("antlr4-generate")
  val antlr4Dependency = SettingKey[ModuleID]("antlr4-dependency")
  val antlr4PackageName = SettingKey[Option[String]]("antlr4-package-name")

  def antlr4GeneratorTask : Def.Initialize[Task[Seq[File]]] = Def.task {
    val cachedCompile = FileFunction.cached(streams.value.cacheDirectory / "antlr4", FilesInfo.lastModified, FilesInfo.exists) {
      in : Set[File] =>
        runAntlr(
          srcFiles = in,
          targetBaseDir = (javaSource in Antlr4).value,
          classpath = (managedClasspath in Compile).value.files,
          log = streams.value.log,
          packageName = (antlr4PackageName in Antlr4).value
        )
    }
    cachedCompile(((sourceDirectory in Antlr4).value ** "*.g4").get.toSet).toSeq
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
    antlr4Generate <<= antlr4GeneratorTask,
    antlr4Dependency := "org.antlr" % "antlr4" % "4.1",
    antlr4PackageName := None
  )) ++ Seq(
    unmanagedSourceDirectories in Compile <+= (sourceDirectory in Antlr4),
    sourceGenerators in Compile <+= (antlr4Generate in Antlr4),
    cleanFiles <+= (javaSource in Antlr4),
    libraryDependencies <+= (antlr4Dependency in Antlr4)
  )
}

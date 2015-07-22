package org.moscatocms.sbt

import sbt._
import Keys._
import org.moscatocms.doctype.ChangelogGenerator
import org.moscatocms.liquibase.DbConfig
import sbt.classpath.ClasspathUtilities
import org.moscatocms.doctype.CodeGenerator
import org.moscatocms.doctype.DoctypeParser._

object DoctypePlugin extends AutoPlugin {
  
  // by defining autoImport, the settings are automatically imported into user's `*.sbt`
  object autoImport {
    
    val moscatoGenerateDoctypes = taskKey[Seq[File]]("Generate doctype resources.")
    val moscatoDoctypeDefinitions = SettingKey[Seq[File]]("moscato-doctype-definitions", "Document type definition files.")

    val moscatoDbUrl = SettingKey[String]("moscato-db-url", "The DB URL.")
    val moscatoDbUsername = SettingKey[String]("moscato-db-username", "The DB username.")
    val moscatoDbPassword = SettingKey[String]("moscato-db-password", "The DB password.")
    val moscatoDbDriver = SettingKey[String]("moscato-db-driver", "The DB driver class name.")
    
    // default values for the tasks and settings
    lazy val baseSettings: Seq[Def.Setting[_]] = Seq(
      
      moscatoGenerateDoctypes in Compile := {
        implicit val log = streams.value.log
        val classPath = (dependencyClasspath in Compile).value
        val classLoader = ClasspathUtilities.toLoader(classPath map { _.data })
        
        val dbConfig = DbConfig(
          moscatoDbUrl.value,
          moscatoDbDriver.value,
          moscatoDbUsername.value,
          moscatoDbPassword.value
        )
        
        val doctypes = moscatoDoctypeDefinitions.value map { parse _ }
        
        val outDir = (sourceManaged in Compile).value
        new ChangelogGenerator(outDir, dbConfig, classLoader).generate(doctypes)
        
        generateSlickCode(
            (runner in Compile).value,
            moscatoDbDriver.value,
            moscatoDbUrl.value,
            streams.value.log,
            classPath,
            outDir,
            organization.value + ".model") ++
          new CodeGenerator(outDir, organization.value).generate(doctypes)
      },
      moscatoDoctypeDefinitions := Nil
    )
    
  }

  import autoImport._

  override def requires = sbt.plugins.JvmPlugin

  // This plugin is automatically enabled for projects which are JvmPlugin.
  override def trigger = allRequirements

  // a group of settings that are automatically added to projects.
  override lazy val projectSettings = Seq(
    sourceGenerators in Compile += (moscatoGenerateDoctypes in Compile).taskValue
  ) ++ baseSettings

  def slickDriver(jdbcDriver: String) = jdbcDriver match {
    case "org.postgresql.Driver" => "slick.driver.PostgresDriver"
    case _ => sys.error(s"Driver $jdbcDriver not supported yet")
  }

  def generateSlickCode(runner: ScalaRun, dbDriver: String, dbUrl: String, log: Logger, classPath: Classpath, outDir: File, pkg: String) = {
    val props = Seq(slickDriver(dbDriver), dbDriver, dbUrl, outDir.getAbsolutePath, pkg)
    toError(runner.run("slick.codegen.SourceCodeGenerator", classPath.files, props, log))
    val f = outDir / pkg.replace(".", "/") / "Tables.scala"
    Seq(file(f.getPath))
  }

}
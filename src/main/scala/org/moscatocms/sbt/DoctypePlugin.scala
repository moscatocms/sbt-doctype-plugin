package org.moscatocms.sbt

import sbt._
import Keys._
import org.moscatocms.doctype.DoctypeGenerator
import org.moscatocms.liquibase.DbConfig
import sbt.classpath.ClasspathUtilities

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
      
      moscatoGenerateDoctypes := {
        implicit val log = streams.value.log
        val classPath = (dependencyClasspath in Runtime).value
        val classLoader = ClasspathUtilities.toLoader(classPath map { _.data })
        
        val dbConfig = DbConfig(
          moscatoDbUrl.value,
          moscatoDbDriver.value,
          moscatoDbUsername.value,
          moscatoDbPassword.value
        )
        
        val outDir = (sourceManaged in Compile).value / "moscato"
        val generator = new DoctypeGenerator(outDir, dbConfig, classLoader)
        generator.generate(moscatoDoctypeDefinitions.value)
        
        val r = (runner in Compile).value
        val pkg = "org.moscatocms.model"
        val props = Seq(slickDriver(moscatoDbDriver.value), moscatoDbDriver.value, moscatoDbUrl.value, outDir.getAbsolutePath, pkg)
        toError(r.run("slick.codegen.SourceCodeGenerator", classPath.files, props, streams.value.log))
        val fname = outDir + "/" + pkg.replace(".", "/") + "/Tables.scala"
        Seq(file(fname))
      },
      moscatoDoctypeDefinitions := Nil
    )
    
    sourceGenerators in Compile += moscatoGenerateDoctypes.taskValue
  }

  import autoImport._
  override def requires = sbt.plugins.JvmPlugin

  // This plugin is automatically enabled for projects which are JvmPlugin.
  override def trigger = allRequirements

  // a group of settings that are automatically added to projects.
  override val projectSettings = baseSettings

  def slickDriver(jdbcDriver: String) = jdbcDriver match {
    case "org.postgresql.Driver" => "slick.driver.PostgresDriver"
    case _ => sys.error(s"Driver $jdbcDriver not supported yet")
  }
}
package org.moscatocms.sbt

import sbt._
import Keys._
import org.moscatocms.doctype.ChangelogGenerator
import org.moscatocms.liquibase.DbConfig
import sbt.classpath.ClasspathUtilities
import org.moscatocms.doctype.Doctype
import org.moscatocms.doctype.CodeGenerator
import org.moscatocms.doctype.DoctypeParser._
import java.io.FilenameFilter
import java.util.regex.Pattern
import org.reflections.Reflections
import scala.collection.JavaConverters._
import org.reflections.util.ConfigurationBuilder
import org.reflections.scanners.ResourcesScanner
import org.reflections.util.ClasspathHelper
import java.util.Arrays
import org.moscatocms.liquibase.LiquibaseRunner
import scala.collection.JavaConversions._
import org.moscatocms.doctype.ChangelogUtils

object MoscatoDoctypePlugin extends AutoPlugin {
  
  // by defining autoImport, the settings are automatically imported into user's `*.sbt`
  object autoImport {
    
    val moscatoParseDoctypes = taskKey[Seq[Doctype]]("Parse Moscato doctypes.")
    val moscatoGenerateDoctypes = taskKey[Seq[File]]("Generate Moscato doctype resources.")
    val moscatoGenerateChangelogs = taskKey[Seq[File]]("Generate Moscato doctype changelogs.")

    val moscatoChangelogDir = SettingKey[File]("moscato-changelog-dir", "The directory to write changelogs to.")
    
    // default values for the tasks and settings
    lazy val baseSettings: Seq[Def.Setting[_]] = Seq(
      
      moscatoParseDoctypes in Compile := {
        implicit val log = streams.value.log
        val doctypeDir = (resourceDirectory in Compile).value / "moscato" / "doctypes"
        val doctypeFiles:Seq[File] = if (doctypeDir.isDirectory)
            doctypeDir.listFiles.filter { _.getName.endsWith(".json") }
            else Nil
        doctypeFiles map { parse _ }
      },
      
      moscatoGenerateChangelogs in Compile := {
        implicit val log = streams.value.log
        val doctypes = (moscatoParseDoctypes in Compile).value
        val changelogGenerator = new ChangelogGenerator(moscatoChangelogDir.value)
        doctypes.map { doctype => changelogGenerator.generate(doctype) }
      },
      
      moscatoGenerateDoctypes in Compile := {
        val doctypes = (moscatoParseDoctypes in Compile).value
        val classPath = (dependencyClasspath in Compile).value
        val classLoader = ClasspathUtilities.toLoader(classPath map { _.data })
        val dbPath = (baseDirectory.value / "target" / "moscato-doctypes-db").getAbsolutePath
        val dbConfig = DbConfig(s"jdbc:h2:$dbPath:moscato", "org.h2.Driver", None, None)

        val changelogs = (moscatoGenerateChangelogs in Compile).value
        changelogs.foreach { changelog =>
          new LiquibaseRunner(dbConfig, classLoader).update(changelog)
        }

        val srcOutDir = (sourceManaged in Compile).value
        generateSlickCode(
            (runner in Compile).value,
            dbConfig,
            streams.value.log,
            classPath,
            srcOutDir,
            organization.value + ".model") ++
          new CodeGenerator(srcOutDir, organization.value).generate(doctypes)
      },
      
      moscatoChangelogDir := (resourceManaged in Compile).value / organization.value.replace(".", "/") / "changelog"
    )
    
  }

  import autoImport._

  override def requires = sbt.plugins.JvmPlugin

  // This plugin is automatically enabled for projects which are JvmPlugin.
  override def trigger = allRequirements

  // a group of settings that are automatically added to projects.
  override lazy val projectSettings = Seq(
    resourceGenerators in Compile += (moscatoGenerateChangelogs in Compile).taskValue,
    sourceGenerators in Compile += (moscatoGenerateDoctypes in Compile).taskValue
  ) ++ baseSettings

  def slickDriver(jdbcDriver: String) = jdbcDriver match {
    case "org.postgresql.Driver" => "slick.driver.PostgresDriver"
    case "org.h2.Driver" => "slick.driver.H2Driver"
    case _ => sys.error(s"Driver $jdbcDriver not supported yet")
  }

  def generateSlickCode(runner: ScalaRun, dbConfig: DbConfig, log: Logger, classPath: Classpath, outDir: File, pkg: String) = {
    val driver = dbConfig.driver
    val props = Seq(slickDriver(driver), driver, dbConfig.url, outDir.getAbsolutePath, pkg)
    toError(runner.run("slick.codegen.SourceCodeGenerator", classPath.files, props, log))
    val f = outDir / pkg.replace(".", "/") / "Tables.scala"
    Seq(file(f.getPath))
  }

}
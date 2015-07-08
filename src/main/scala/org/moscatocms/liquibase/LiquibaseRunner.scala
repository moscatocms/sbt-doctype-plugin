package org.moscatocms.liquibase

import sbt.File
import liquibase.Liquibase
import liquibase.integration.commandline.CommandLineUtils
import liquibase.resource.FileSystemResourceAccessor
import sbt.classpath.ClasspathUtilities

class LiquibaseRunner(val conf: DbConfig, val classLoader: ClassLoader) {
  
  def update(changelogFile: File) {
    execAndClose(liquibaseInstance(changelogFile)) {
      _.update("")
    }
  }
  
   def execAndClose(liquibase: Liquibase)(f: (Liquibase) => Unit): Unit = {
    try {
      f(liquibase)
    } finally {
      liquibase.getDatabase.close()
    }
  }

  def liquibaseInstance(changelogFile: File) = {
    new Liquibase(
      changelogFile.getAbsolutePath,
      new FileSystemResourceAccessor,
      database()
    )
  }
  
  def database() = {
    CommandLineUtils.createDatabaseObject(
      //new FileSystemResourceAccessor,
      classLoader,
      conf.url,
      conf.username,
      conf.password,
      conf.driver,
      null.asInstanceOf[String], // defaultCatalog
      null.asInstanceOf[String], // defaultSchema
      false, // outputDefaultCatalog
      true, // outputDefaultSchema
      null.asInstanceOf[String], // databaseClass
      null.asInstanceOf[String], // driverPropertiesFile
      null, // databaseClass
      null, // driverPropertiesFile
      null, // propertyProviderClass
      null.asInstanceOf[String], // liquibaseChangelogCatalog
      null.asInstanceOf[String] // liquibaseChangelogSchema
    )
  }
}
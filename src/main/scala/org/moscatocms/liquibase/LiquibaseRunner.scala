package org.moscatocms.liquibase

import sbt.File
import liquibase.Liquibase
import liquibase.integration.commandline.CommandLineUtils
import liquibase.resource.FileSystemResourceAccessor
import sbt.classpath.ClasspathUtilities
import liquibase.resource.CompositeResourceAccessor
import liquibase.resource.ClassLoaderResourceAccessor

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
      new CompositeResourceAccessor(
        new FileSystemResourceAccessor,
        new ClassLoaderResourceAccessor(classLoader)
      ),
      database()
    )
  }
  
  def database() = {
    CommandLineUtils.createDatabaseObject(
      //new FileSystemResourceAccessor,
      classLoader,
      conf.url,
      conf.username.orNull,
      conf.password.orNull,
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
      "liquibase" // liquibaseChangelogSchema
    )
  }
}
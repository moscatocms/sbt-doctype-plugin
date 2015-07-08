package org.moscatocms.doctype

import sbt._
import scala.xml.{Node, XML}
import org.moscatocms.doctype.DoctypeParser._
import org.moscatocms.liquibase._

class DoctypeGenerator(
    outputDir: File,
    dbConf: DbConfig,
    classLoader: ClassLoader
) {
  
  def generate(definitions: Seq[File])(implicit log: Logger): Seq[File] = {
    log.info(s"Generating doctypes.")
    outputDir.mkdirs()
    val doctypes = definitions map { parse _ }
    val changelogFile = generateTables(doctypes)
    new LiquibaseRunner(dbConf, classLoader).update(changelogFile)
    Seq(changelogFile)
  }
  
  def generateTables(doctypes: Seq[Doctype]): File = {
    val liquibaseFile = outputDir / "changelog.xml"
    XML.save(
      filename = liquibaseFile.getAbsolutePath,
      node = getChangelog(doctypes),
      xmlDecl = true)
    liquibaseFile
  }
  
  def getChangelog(doctypes: Seq[Doctype]): Node = {
    <databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
        xsi:schemaLocation="
            http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd
            http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd">
      { doctypes map { getChangelog _ } }
    </databaseChangeLog>
  }
  
  def getChangelog(doctype: Doctype) = {
    val sequence = s"seq__${doctype.table}__id"
    <changeSet id={s"doctype__${doctype.table}"} author="moscato">
      <createSequence sequenceName={sequence} startValue="1" incrementBy="1"/>
      <createTable tableName={doctype.table}>
        <column name="id" type="bigint" defaultValue={s"nextval('$sequence')"}>
          <constraints nullable="false" primaryKey="true"/>
        </column>
        { doctype.definition.fields map { field =>
        <column name={field.name} type={getColumnType(field.fieldType)}>
          { if (field.isRequired) {
            <constraints nullable="false"/>
          }}
        </column>
        }}
      </createTable>
    </changeSet>
  }

  def getColumnType(fieldType: FieldType) = fieldType match {
    case Link => "text"
    case Html => "text"
  }
  
}

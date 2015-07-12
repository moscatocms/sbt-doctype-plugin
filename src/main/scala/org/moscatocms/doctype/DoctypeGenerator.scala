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
    doctypes foreach { doctype =>
      writeChangelog(outputDir / (doctype.table + ".xml"), getChangelog(doctype))
    }
    writeChangelog(liquibaseFile, getChangelog(doctypes))
    liquibaseFile
  }
  
  def writeChangelog(file: File, changeSets: Seq[Node]) {
    XML.save(
      filename = file.getAbsolutePath,
      node = <databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
        xsi:schemaLocation="
            http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd
            http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd">
        {changeSets}
      </databaseChangeLog>,
      xmlDecl = true)
  }
  
  def getChangelog(doctypes: Seq[Doctype]): Seq[Node] = {
    <include file="org/moscatocms/migrations/moscato-changelog.xml"/> ++
    { doctypes map { doctype =>
      <include file={doctype.table + ".xml"} relativeToChangelogFile="true"/>
    }}
  }
  
  def getChangelog(doctype: Doctype): Seq[Node] = {
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

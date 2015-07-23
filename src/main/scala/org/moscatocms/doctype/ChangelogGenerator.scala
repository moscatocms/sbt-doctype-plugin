package org.moscatocms.doctype

import sbt._
import scala.xml.{Node, XML}
import org.moscatocms.liquibase._

class ChangelogGenerator(
  outputDir: File
) {
  
  def generate(doctype: Doctype)(implicit log: Logger): File = {
    log.info(s"Generating database schema for doctype ${doctype.table}")
    val changelogFile = outputDir / (doctype.table + ".xml")
    writeChangelog(changelogFile, getChangelog(doctype))
    changelogFile
  }
  
  def writeChangelog(file: File, changeSets: Seq[Node]) {
    file.getParentFile.mkdirs()
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
  
  def getChangelog(doctype: Doctype): Seq[Node] = {
    val sequence = s"seq__${doctype.table}__id"
    <changeSet id={s"doctype__${doctype.table}"} author="moscato">
<!--
      <createSequence sequenceName={sequence} startValue="1" incrementBy="1"/>
-->
      <createTable tableName={doctype.table}>
<!--
        <column name="id" type="bigint" defaultValue={s"nextval('$sequence')"}>
-->
        <column name="id" type="bigint" autoIncrement="true">
          <constraints nullable="false" primaryKey="true"/>
        </column>
        { doctype.definition.fields map { field =>
        <column name={field.name} type={getColumnType(field.fieldType)}>
          { if (field.required) {
            <constraints nullable="false"/>
          }}
        </column>
        }}
      </createTable>
    </changeSet>
  }

  def getColumnType(fieldType: FieldType) = fieldType match {
    case Link => "VARCHAR"
    case Html => "VARCHAR"
  }
  
  def generateCompleteChangelog(changelogPaths: Seq[String])(implicit log: Logger): File = {
    //val changelogs = changelogPaths map { _.split("/").last } mkString(", ")
    val changelogs = changelogPaths.mkString("\n")
    log.info(s"Generating database tables for changelogs (${changelogs})")
    val liquibaseFile = outputDir / "changelog.xml"
    writeChangelog(liquibaseFile, getCompleteChangelog(changelogPaths))
    liquibaseFile
  }
  
  def getCompleteChangelog(changelogPaths: Seq[String]): Seq[Node] = {
    <include file="org/moscatocms/migrations/moscato-changelog.xml"/> ++
    { changelogPaths map { path => <include file={path}/> }}
  }
  
}

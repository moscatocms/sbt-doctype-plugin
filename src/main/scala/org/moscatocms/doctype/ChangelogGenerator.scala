package org.moscatocms.doctype

import sbt._
import scala.xml.{Node, XML}
import org.moscatocms.liquibase._

class ChangelogGenerator(
  outputDir: File
) {
  
  import ChangelogUtils._
  
  def generate(doctype: Doctype)(implicit log: Logger): File = {
    log.info(s"Generating database schema for doctype ${doctype.table}")
    val changelogFile = outputDir / (doctype.table + ".xml")
    writeChangelog(changelogFile, getChangelog(doctype))
    changelogFile
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
  
}

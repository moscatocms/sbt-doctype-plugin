package org.moscatocms.doctype

import org.moscatocms.util.StringUtil._

abstract class FieldType(val name: String)

case object Link extends FieldType("link")

case object Html extends FieldType("html")

/*
object FieldType extends Enumeration {
  type FieldType = Value
  val Link = Value("link")
  val Html = Value("html")
}
*/

case class Field(
  val name: String,
  val fieldType: FieldType,
  val required: Option[Boolean]
) {
  def isRequired = required.getOrElse(false)
}

case class DoctypeDefinition(
  val name: String,
  val fields: Seq[Field]
)

class Doctype(
  val table: String,
  val definition: DoctypeDefinition
) {
  def className() = underscoreToCamel(table)
}
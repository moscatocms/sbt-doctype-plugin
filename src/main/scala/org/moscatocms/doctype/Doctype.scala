package org.moscatocms.doctype

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
  val required: Boolean
)

case class DoctypeDefinition(
  val name: String,
  val fields: Seq[Field]
)

class Doctype(
  val table: String,
  val definition: DoctypeDefinition
)
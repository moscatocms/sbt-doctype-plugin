package org.moscatocms.doctype

import spray.json._

object DoctypeJsonProtocol extends DefaultJsonProtocol {
  
  implicit object FieldTypeJsonFormat extends JsonFormat[FieldType] {
    def read(value: JsValue) = value match {
      case JsString(Link.name) => Link
      case JsString(Html.name) => Html
      case JsString(f) => deserializationError(s"Invalid field type: $f")
      case _ => deserializationError("Field type string expected")
    }
    def write(fieldType: FieldType) = ???
  }
  
  implicit object FieldJsonFormat extends JsonFormat[Field] {
    def write(f: Field) = ???

    def read(value: JsValue) = value.asJsObject.getFields("name", "type", "required") match {
      case Seq(JsString(name), fieldType, JsBoolean(required)) =>
        Field(name, FieldTypeJsonFormat.read(fieldType), required)
      case Seq(JsString(name), fieldType) =>
        Field(name, FieldTypeJsonFormat.read(fieldType), false)
      case _ => deserializationError("Field object expected")
    }
  }

  implicit val doctypeDefinitionJsonFormat = jsonFormat2(DoctypeDefinition)
  
}
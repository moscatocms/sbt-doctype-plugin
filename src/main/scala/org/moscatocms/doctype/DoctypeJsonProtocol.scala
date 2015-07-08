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
  
  implicit val fieldJsonFormat = jsonFormat3(Field)
  implicit val doctypeDefinitionJsonFormat = jsonFormat2(DoctypeDefinition)
  
}
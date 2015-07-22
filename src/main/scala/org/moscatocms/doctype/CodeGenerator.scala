package org.moscatocms.doctype

import sbt._
import org.moscatocms.doctype.txt.DataTemplate
import org.moscatocms.doctype.txt.DomainTemplate
import org.moscatocms.doctype.txt.RouteTemplate
import java.nio.file._
import java.nio.charset.StandardCharsets.UTF_8
import org.moscatocms.util.StringUtil._

class DoctypeExtensions(doctype: Doctype) {
  def className = underscoreToCamel(doctype.table).capitalize
}

class FieldExtensions(field: Field) {
  def scalaType = field.fieldType match {
    case Link => "String"
    case Html => "String"
  }
}

object CodeGenerator {
  implicit def extendDoctype(doctype: Doctype) = new DoctypeExtensions(doctype)
  implicit def extendField(field: Field) = new FieldExtensions(field)
}

class CodeGenerator(
  outputDir: File,
  packageName: String
) {

  import CodeGenerator._

  def generate(doctypes: Seq[Doctype]): Seq[File] = {
    doctypes.flatMap { generate _ }
  }

  def generate(doctype: Doctype): Seq[File] = Seq(
    write(DataTemplate(doctype, packageName).toString, doctype.className + "Data"),
    write(DomainTemplate(doctype, packageName).toString, doctype.className + "s"),
    write(RouteTemplate(doctype, packageName).toString, doctype.className + "Route")
  )

  def write(code: String, className: String): File = {
    val file = outputDir / packageName.replace(".", "/") / "doctypes" / (className + ".scala")
    file.getParentFile.mkdirs()
    Files.write(Paths.get(file.getAbsolutePath), code.getBytes(UTF_8))
    file
  }

}


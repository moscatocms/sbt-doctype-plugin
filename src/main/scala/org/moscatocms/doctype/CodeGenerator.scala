package org.moscatocms.doctype

import sbt._
import txt.Row
import java.nio.file._
import java.nio.charset.StandardCharsets.UTF_8

class CodeGenerator(
  outputDir: File
) {

  def generate(doctypes: Seq[Doctype]): Seq[File] = {
    doctypes.flatMap { generate _ }
  }

  def generate(doctype: Doctype): Seq[File] = {
    val code = Row(doctype).toString
    val file = outputDir / (doctype.className + ".scala")
    Files.write(Paths.get(file.getAbsolutePath), code.getBytes(UTF_8))
    Seq(file)
  }
  
}
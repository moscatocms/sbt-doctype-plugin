package org.moscatocms.doctype

import sbt._
import spray.json._
import DoctypeJsonProtocol._

object DoctypeParser {
  
  def parse(definitionFile: File)(implicit log: Logger): Doctype = {
    log.info(s"Parsing doctype definition $definitionFile.")
    val definition = IO.read(definitionFile).parseJson
    val table = definitionFile.getName.split('.')(0)
    new Doctype(table, definition.convertTo[DoctypeDefinition])
  }
  
}
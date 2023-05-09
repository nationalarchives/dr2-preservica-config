package uk.gov.nationalarchives.dp

import uk.gov.nationalarchives.dp.client.FileInfo._

import java.io.File
import scala.xml.XML

object FileProcessors {
  implicit class FileUtils(f: File) {
    def withoutSuffix: String = f.getName.split("\\.").head
  }

  private[dp] def processSchemas(file: File, xmlData: String): SchemaFileInfo = SchemaFileInfo(file.withoutSuffix, "", file.getName, xmlData)

  private[dp] def processTransforms(file: File, xmlData: String): TransformFileInfo = {
    val name = file.withoutSuffix
    val fromNameSpace = XML.loadString(xmlData).getNamespace("tns")
    val purpose = if (name.contains("view")) "view" else "edit"
    TransformFileInfo(name, fromNameSpace, "http://www.w3.org/1999/xhtml", purpose, file.getName, xmlData)
  }

  private[dp] def processIndexDefinitions(file: File, xmlData: String): IndexDefinitionInfo = IndexDefinitionInfo(file.withoutSuffix, xmlData)

  private[dp] def processMetadataTemplates(file: File, xmlData: String): MetadataTemplateInfo = MetadataTemplateInfo(file.withoutSuffix, xmlData)
}

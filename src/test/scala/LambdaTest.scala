import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.mockito._
import uk.gov.nationalarchives.dp.Lambda
import uk.gov.nationalarchives.dp.client.AdminClient
import uk.gov.nationalarchives.dp.client.FileInfo.{IndexDefinitionInfo, MetadataTemplateInfo, SchemaFileInfo, TransformFileInfo}
import uk.gov.nationalarchives.dp.client.Utils.AuthDetails
import zio._

import scala.jdk.CollectionConverters._
class LambdaTest extends AnyFlatSpec with MockitoSugar {

  val authDetails: AuthDetails = AuthDetails("test", "test")
  def result[T](res: Task[T]): T = Unsafe.unsafe { implicit unsafe =>
    Runtime.default.unsafe.run(res).getOrThrow()
  }

  "processFiles" should "add the schema files correctly" in {
    val client = mock[AdminClient[Task]]
    val schemaCaptor = ArgumentCaptor.forClass(classOf[List[SchemaFileInfo]])
    when(client.addOrUpdateSchemas(schemaCaptor.capture(), any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateTransforms(any[List[TransformFileInfo]](), any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateMetadataTemplates(any[List[MetadataTemplateInfo]](), any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateIndexDefinitions(any[List[IndexDefinitionInfo]](), any[AuthDetails]())).thenReturn(ZIO.unit)

    result(new Lambda().processFiles(client, authDetails))

    val schemaFileInfo: List[SchemaFileInfo] = schemaCaptor.getAllValues.asScala.toList.head
    val sortedFileInfo = schemaFileInfo.sortBy(_.name)
    sortedFileInfo.size should equal(2)
    val closureDataSchema = sortedFileInfo.head
    closureDataSchema.name should equal("closure-data-schema")
    closureDataSchema.description should equal("")
    closureDataSchema.originalName should equal("closure-data-schema.xsd")

    val closureResultSchema = sortedFileInfo.last
    closureResultSchema.name should equal("closure-result-schema")
    closureResultSchema.description should equal("")
    closureResultSchema.originalName should equal("closure-result-schema.xsd")
  }

  "processFiles" should "add the transform files correctly" in {
    val client = mock[AdminClient[Task]]
    val transformCaptor = ArgumentCaptor.forClass(classOf[List[TransformFileInfo]])
    when(client.addOrUpdateSchemas(any[List[SchemaFileInfo]], any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateTransforms(transformCaptor.capture(), any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateMetadataTemplates(any[List[MetadataTemplateInfo]](), any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateIndexDefinitions(any[List[IndexDefinitionInfo]](), any[AuthDetails]())).thenReturn(ZIO.unit)

    result(new Lambda().processFiles(client, authDetails))

    val transformFileInfo: List[TransformFileInfo] = transformCaptor.getAllValues.asScala.head
    val sortedFileInfo = transformFileInfo.sortBy(_.name)
    sortedFileInfo.size should equal(3)
    val closureDataEdit = sortedFileInfo.head
    val closureDataView = sortedFileInfo.tail.head
    val closureResultView = sortedFileInfo.last

    closureDataEdit.name should equal("closure-data-edit-transform")
    closureDataEdit.originalName should equal("closure-data-edit-transform.xsl")
    closureDataEdit.from should equal("https://nationalarchives.gov.uk/ClosureData")
    closureDataEdit.to should equal("http://www.w3.org/1999/xhtml")
    closureDataEdit.purpose should equal("edit")

    closureDataView.name should equal("closure-data-view-transform")
    closureDataView.originalName should equal("closure-data-view-transform.xsl")
    closureDataView.from should equal("https://nationalarchives.gov.uk/ClosureData")
    closureDataView.to should equal("http://www.w3.org/1999/xhtml")
    closureDataView.purpose should equal("view")

    closureResultView.name should equal("closure-result-view-transform")
    closureResultView.originalName should equal("closure-result-view-transform.xsl")
    closureResultView.from should equal("https://nationalarchives.gov.uk/ClosureResult")
    closureResultView.to should equal("http://www.w3.org/1999/xhtml")
    closureResultView.purpose should equal("view")
  }

  "processFiles" should "add the metadata templates correctly" in {
    val client = mock[AdminClient[Task]]
    val metadataTemplateCaptor = ArgumentCaptor.forClass(classOf[List[MetadataTemplateInfo]])
    when(client.addOrUpdateSchemas(any[List[SchemaFileInfo]], any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateTransforms(any[List[TransformFileInfo]], any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateMetadataTemplates(metadataTemplateCaptor.capture(), any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateIndexDefinitions(any[List[IndexDefinitionInfo]](), any[AuthDetails]())).thenReturn(ZIO.unit)

    result(new Lambda().processFiles(client, authDetails))

    val closureDataTemplateValues: List[MetadataTemplateInfo] = metadataTemplateCaptor.getAllValues.asScala.head
    val sortedFileInfo = closureDataTemplateValues.sortBy(_.name)
    sortedFileInfo.size should equal(1)
    sortedFileInfo.head.name should equal("closure-data-template")
  }

  "processFiles" should "add the index definitions correctly" in {
    val client = mock[AdminClient[Task]]
    val indexDefinitionCaptor = ArgumentCaptor.forClass(classOf[List[IndexDefinitionInfo]])
    when(client.addOrUpdateSchemas(any[List[SchemaFileInfo]], any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateTransforms(any[List[TransformFileInfo]], any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateMetadataTemplates(any[List[MetadataTemplateInfo]], any[AuthDetails]())).thenReturn(ZIO.unit)
    when(client.addOrUpdateIndexDefinitions(indexDefinitionCaptor.capture(), any[AuthDetails]())).thenReturn(ZIO.unit)

    result(new Lambda().processFiles(client, authDetails))

    val indexDefinitionInfo: List[IndexDefinitionInfo] = indexDefinitionCaptor.getAllValues.asScala.head
    val sortedFileInfo = indexDefinitionInfo.sortBy(_.name)
    sortedFileInfo.size should equal(1)
    sortedFileInfo.head.name should equal("closure-result-index-definition")
  }
}

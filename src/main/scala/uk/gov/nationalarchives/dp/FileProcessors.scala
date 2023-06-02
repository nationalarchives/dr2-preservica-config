package uk.gov.nationalarchives.dp

import cats.effect.IO
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import fs2.{Chunk, text}
import fs2.interop.reactivestreams.PublisherOps
import org.reactivestreams.Publisher
import uk.gov.nationalarchives.DAS3Client
import uk.gov.nationalarchives.dp.client.AdminClient
import uk.gov.nationalarchives.dp.client.FileInfo._
import cats.implicits._
import io.circe.{Decoder, HCursor}
import io.circe.parser.decode
import java.nio.ByteBuffer
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.xml.XML

object FileProcessors {
  implicit class KeyUtils(s: String) {
    def getName: String = s.split("/").last

    def withoutSuffix: String = s.getName.split("\\.").head
  }

  case class S3Objects(entities: List[S3Object])
  case class S3Object(bucket: String, key: String)

  implicit val decodeEntity: Decoder[S3Object] = (c: HCursor) =>
    for {
      key <- c.downField("s3").downField("object").downField("key").as[String]
      bucket <- c.downField("s3").downField("bucket").downField("name").as[String]
    } yield {
      S3Object(bucket, key)
    }

  implicit val decodeS3: Decoder[S3Objects] = (c: HCursor) =>
    for {
      entities <- c.downField("Records").as[List[S3Object]]
    } yield {
      S3Objects(entities)
    }

  private[dp] def processSchemas(key: String, xmlData: String): SchemaFileInfo =
    SchemaFileInfo(key.withoutSuffix, "", key.getName, xmlData)

  private[dp] def processTransforms(key: String, xmlData: String): TransformFileInfo = {
    val name = key.withoutSuffix
    val fromNameSpace = XML.loadString(xmlData).getNamespace("tns")
    val purpose = if (name.contains("view")) "view" else "edit"
    TransformFileInfo(name, fromNameSpace, "http://www.w3.org/1999/xhtml", purpose, key.getName, xmlData)
  }

  private[dp] def processIndexDefinitions(key: String, xmlData: String): IndexDefinitionInfo =
    IndexDefinitionInfo(key.withoutSuffix, xmlData)

  private[dp] def processMetadataTemplates(key: String, xmlData: String): MetadataTemplateInfo =
    MetadataTemplateInfo(key.withoutSuffix, xmlData)

  private[dp] def processFiles(
      client: AdminClient[IO],
      s3Client: DAS3Client[IO],
      secretName: String,
      s3Objects: List[S3Object]
  ): IO[List[Unit]] = {
    s3Objects
      .map(event => {
        val key = event.key
        for {
          publisher <- s3Client.download(event.bucket, key)
          xml <- xmlFromPublisher(publisher)
          _ <- key.split("/").head match {
            case "schemas" =>
              val schemas = processSchemas(key, xml) :: Nil
              client.addOrUpdateSchemas(schemas, secretName)
            case "index_definitions" =>
              val indexDefinitions = processIndexDefinitions(key, xml) :: Nil
              client.addOrUpdateIndexDefinitions(indexDefinitions, secretName)
            case "metadata_templates" =>
              val metadataTemplates = processMetadataTemplates(key, xml) :: Nil
              client.addOrUpdateMetadataTemplates(metadataTemplates, secretName)
            case "transforms" =>
              val transforms = processTransforms(key, xml) :: Nil
              client.addOrUpdateTransforms(transforms, secretName)
          }
        } yield ()
      })
      .sequence
  }

  private def xmlFromPublisher(pub: Publisher[ByteBuffer]) = {
    pub
      .toStreamBuffered[IO](1024 * 1024)
      .map(f => Chunk.byteBuffer(f))
      .through(text.utf8.decodeC)
      .compile
      .fold(List[String]())((acc, str) => {
        str :: acc
      })
      .map(_.mkString("\n"))
  }

  private[dp] def s3ObjectsFromEvent(input: SQSEvent): IO[List[S3Object]] =
    input.getRecords.asScala
      .map(record => {
        for {
          _ <- IO.println(record.getBody)
          decoded <- IO.fromEither(decode[S3Objects](record.getBody))
        } yield decoded

      })
      .toList
      .sequence
      .map(_.flatMap(_.entities))
}

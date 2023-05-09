package uk.gov.nationalarchives.dp

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import uk.gov.nationalarchives.dp.client.{AdminClient, FileInfo}
import zio._
import zio.config._
import ConfigDescriptor._
import uk.gov.nationalarchives.dp.FileProcessors._
import uk.gov.nationalarchives.dp.client.Utils.AuthDetails
import uk.gov.nationalarchives.dp.client.zio.ZioClient._
import zio.aws.core.config.AwsConfig
import zio.aws.netty.NettyHttpClient
import zio.aws.secretsmanager.SecretsManager
import zio.aws.secretsmanager.model.GetSecretValueRequest
import zio.aws.secretsmanager.model.primitives.SecretIdType
import zio.json._
import zio.stream.ZStream

import java.io.{File, InputStream, OutputStream}
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

class Lambda extends RequestStreamHandler {
  private case class SchemaConfig(url: String, secretName: String)

  private def getAuthDetails(secretName: String): ZIO[Any, Object, AuthDetails] = {
    val aws = NettyHttpClient.default >>> AwsConfig.default >>> SecretsManager.live
    SecretsManager.getSecretValue(GetSecretValueRequest(SecretIdType(secretName)))
      .provideLayer(aws)
      .flatMap(res => ZIO.fromOption(res.secretString.toOption))
      .flatMap(secretString => ZIO.fromEither(secretString.fromJson[Map[String, String]]))
      .map(m => AuthDetails(m.head._1, m.head._2))
  }

  private def getInput[T <: FileInfo](folderName: String, processor: (File, String) => T): ZIO[Any, Throwable, List[T]] = {
    val folderPath = Paths.get(getClass.getResource(s"/$folderName").toURI)
    ZIO.collectAll {
      Files.list(folderPath).iterator().asScala.map(_.toFile).toList.map { file =>
        ZStream.fromFile(file).runCollect.map(a => a.toList.map(_.toChar).mkString)
          .map(xmlData => processor(file, xmlData))
      }
    }
  }

  private val source: ConfigSource = ConfigSource.fromSystemEnv()
  private val descriptor: ConfigDescriptor[SchemaConfig] = (string("PRESERVICA_URL") zip string("SECRET_NAME")).to[SchemaConfig]

  def processFiles(client: AdminClient[Task], authDetails: AuthDetails): ZIO[Any, Throwable, Unit] = for {
    schemas <- getInput("schemas", processSchemas)
    _ <- client.addOrUpdateSchemas(schemas, authDetails)
    indexDefinitions <- getInput("index_definitions", processIndexDefinitions)
    _ <- client.addOrUpdateIndexDefinitions(indexDefinitions, authDetails)
    metadataTemplates <- getInput("metadata_templates", processMetadataTemplates)
    _ <- client.addOrUpdateMetadataTemplates(metadataTemplates, authDetails)
    transforms <- getInput("transforms", processTransforms)
    _ <- client.addOrUpdateTransforms(transforms, authDetails)
  } yield ()

  override def handleRequest(inputStream: InputStream, outputStream: OutputStream, context: Context): Unit =
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.run {
        for {
          config <- read(descriptor from source)
          auth <- getAuthDetails(config.secretName)
          client <- adminClient(config.url)
          _ <- processFiles(client,auth)
        } yield ()
      }.getOrThrowFiberFailure()
    }
}

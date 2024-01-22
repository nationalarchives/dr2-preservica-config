import sbt.*
object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.17"
  lazy val scalaTestMockito = "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0"
  lazy val pureConfigCats = "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.5"
  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.17.5"
  lazy val lambdaCore = "com.amazonaws" % "aws-lambda-java-core" % "1.2.3"
  lazy val lambdaEvents = "com.amazonaws" % "aws-lambda-java-events" % "3.11.4"
  lazy val preservicaClient = "uk.gov.nationalarchives" %% "preservica-client-fs2" % "0.0.40"
  lazy val s3Client = "uk.gov.nationalarchives" %% "da-s3-client" % "0.1.35"
  lazy val circeParser = "io.circe" %% "circe-parser" % "0.14.6"
  lazy val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "2.2.0"
  lazy val scalaParserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"
  lazy val jaxb = "javax.xml.bind" % "jaxb-api" % "2.3.1"
}

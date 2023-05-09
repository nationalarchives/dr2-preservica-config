import sbt.*
object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.15"
  lazy val scalaTestMockito = "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0"
  lazy val lambdaCore = "com.amazonaws" % "aws-lambda-java-core" % "1.2.2"
  lazy val preservicaClient = "uk.gov.nationalarchives" %% "preservica-client-zio" % "0.0.4-SNAPSHOT"
  lazy val zioConfig = "dev.zio" %% "zio-config" % "3.0.7"
  lazy val zioAwsSecretsManager = "dev.zio" %% "zio-aws-secretsmanager" % "5.20.22.2"
  lazy val zioJson = "dev.zio" %% "zio-json" % "0.5.0"
  lazy val zioAwsNetty = "dev.zio" %% "zio-aws-netty" % "5.20.22.2"
}

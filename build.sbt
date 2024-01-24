import Dependencies._

lazy val root = project
  .in(file("."))
  .settings(
    name := "dp-preservica-config",
    scalaVersion := "2.13.12",
    organization := "uk.gov.nationalarchives",
    scalacOptions ++= Seq("-Wunused:imports", "-Werror", "-Wconf:src=src_managed/.*:silent"),
    libraryDependencies ++= Seq(
      lambdaCore,
      lambdaEvents,
      log4jSlf4j,
      log4jCore,
      log4jTemplateJson,
      log4CatsCore,
      log4CatsSlf4j,
      preservicaClient,
      pureConfig,
      pureConfigCats,
      s3Client,
      circeParser,
      scalaXml,
      scalaParserCombinators,
      jaxb,
      scalaTest % Test,
      scalaTestMockito % Test
    )
  )
  .enablePlugins(ScalaxbPlugin)
  .settings(
    Compile / scalaxb / scalaxbXsdSource := (Compile / resourceDirectory).value / "schemas"
  )

(assembly / assemblyJarName) := "preservica-config.jar"

(assembly / assemblyMergeStrategy) := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _                                   => MergeStrategy.first
}

(Test / fork) := true
(Test / envVars) := Map("AWS_LAMBDA_FUNCTION_NAME" -> "testfunction")

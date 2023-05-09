import Dependencies.*

lazy val root = project
  .in(file("."))
  .settings(
    name := "dp-preservica-config",
    scalaVersion := "2.13.10",
    scalacOptions ++= Seq("-Wunused:imports", "-Werror"),
    libraryDependencies ++= Seq(
      lambdaCore,
      preservicaClient,
      zioConfig,
      zioAwsSecretsManager,
      zioAwsNetty,
      zioJson,
      scalaTest % Test,
      scalaTestMockito % Test
    )
  )

(assembly / assemblyJarName) := "preservica-config.jar"

(assembly / assemblyMergeStrategy) := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}

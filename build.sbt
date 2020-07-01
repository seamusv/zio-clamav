import BuildHelper._

inThisBuild(
  List(
    organization := "ca.venasse",
    homepage := Some(url("https://github.com/seamusv/zio-clamav/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("seamusv", "Seamus Venasse", "svenasse@gmail.com", url("https://github.com/seamusv"))
    ),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc"),
    scmInfo := Some(
      ScmInfo(url("https://github.com/seamusv/zio-clamav/"), "scm:git:git@github.com:seamusv/zio-clamav.git")
    )
  )
)

ThisBuild / publishTo := sonatypePublishToBundle.value

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val clamav =
  project
    .in(file("."))
    .settings(stdSettings("zio-clamav"))
    .settings(buildInfoSettings("zio.clamav"))
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio"          % zioVersion,
        "dev.zio" %% "zio-nio"      % zioNioVersion,
        "dev.zio" %% "zio-test"     % zioVersion % Test,
        "dev.zio" %% "zio-test-sbt" % zioVersion % Test
      ),
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
    )
    .enablePlugins(BuildInfoPlugin)

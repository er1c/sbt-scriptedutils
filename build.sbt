ThisBuild / version := "0.1.0-SNAPSHOT"

def scala212 = "2.12.8"
def scala210 = "2.10.7"
// Maybe todo: 0.13.x requires updated scala.reflect compat
ThisBuild / crossScalaVersions := Seq(scala212/*, scala210*/)
ThisBuild / scalaVersion := scala212

lazy val root = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-scriptedutils",
    scalacOptions := Seq("-deprecation", "-unchecked", "-Dscalac.patmat.analysisBudget=1024", "-Xfuture"),
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.10" => "0.13.18"
        case "2.12" => "1.2.8"
      }
    }
  )

inThisBuild(List(
  organization := "io.github.er1c",
  homepage := Some(url("https://github.com/er1c/sbt-scriptedutils")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  versionScheme := Some("semver-spec"),
  scmInfo := Some(ScmInfo(
    url("https://github.com/er1c/sbt-scriptedutils"),
    "scm:git@github.com:er1c/sbt-scriptedutils.git"
  )),
  developers := List(
    Developer(
      "er1c",
      "Eric Peters",
      "eric@peters.org",
      url("https://github.com/er1c")
    )
  )
))

// set up 'scripted; sbt plugin for testing sbt plugins
ThisBuild / scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
ThisBuild / scriptedBufferLog := false

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

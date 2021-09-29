sbt ScriptedPlugin Utils
========================

[![Continuous Integration](https://github.com/er1c/sbt-scriptedutils/actions/workflows/ci.yml/badge.svg)](https://github.com/er1c/sbt-scriptedutils/actions/workflows/ci.yml)

Helpful utilities for writing scripted tests

## Usage

Requires sbt 1.x

    addSbtPlugin("io.github.er1c" %% "sbt-scriptedutils" % "0.1.0") 

### Scripted usage

Example file: `src/sbt-test/<test-group>/<test-name>/test`

    > clean
    > compile
    # seen both "Done compiling" and "done compiling"
    > checkLogContains one compiling
    -> checkLogNotContains one compiling
    > clearLog
    > checkLogNotContains one compiling
    -> checkLogContains fooos

### Testing

    sbt scripted

Currently testing against sbt:

- [1.0.0](sbt-test/plugin/v1.0.0/project/build.properties)
- [1.2.8](sbt-test/plugin/v1.2.8/project/build.properties)
- [1.3.13](sbt-test/plugin/v1.3.13/project/build.properties)
- [1.4.9](sbt-test/plugin/v1.4.9/project/build.properties)
- [1.5.5](sbt-test/plugin/v1.5.5/project/build.properties)

#### CI

Update github workflows: `sbt githubWorkflowGenerate`

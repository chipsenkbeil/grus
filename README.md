# grus
(__G__)enerate (__R__)esources (__U__)sing (__S__)cala

Static site generator written in Scala using Scalatags for templates and
Flexmark for markdown.

## Running

### Via homebrew

Install via the following:

```
brew install chipsenkbeil/personal/grus
```

Then run on the command line via:

```
grus ...
```

### Via binary

Grab one of the fat jars listed below and run via `java -jar DOWNLOADED_JAR.jar`. 

- Built with Scala 2.10: [Download latest](https://github.com/chipsenkbeil/grus/releases/download/v0.1.0/grus-0.1.0-2.10.6.jar)
- Built with Scala 2.11: [Download latest](https://github.com/chipsenkbeil/grus/releases/download/v0.1.0/grus-0.1.0-2.11.8.jar)
- Built with Scala 2.12: [Download latest](https://github.com/chipsenkbeil/grus/releases/download/v0.1.0/grus-0.1.0-2.12.1.jar)

You can view help information by adding `--help` to the base jar or any of its commands. 
E.g. `java -jar DOWNLOADED_JAR.jar --help` or `java -jar DOWNLOADED_JAR.jar generate --help`.

### Via sbt

To use in sbt (0.13.x) to generate your own content, add the following plugin:

```scala
addSbtPlugin("org.senkbeil" %% "sbt-grus" % "0.1.0")
```

- `sbt grusGenerate` will generate the website and put the contents in an
  output directory
- `sbt grusServe` will generate the website and start a server to display
  it locally
- `sbt grusPublish` will publish the contents output from `grusGenerate`

You can add `--help` to any of the above commands to display help information
for the specific command. E.g. `sbt "grusGenerate --help"`.

## Building a Theme

To use the API to create a custom theme, add the following dependency:

```scala
libraryDependencies += "org.senkbeil" %% "grus-layouts" % "0.1.0"
```

## Examples

See the
[Scala Debugger docs module](https://github.com/ensime/scala-debugger/tree/master/scala-debugger-docs)
for an example of how to write a custom theme.

See the
[Scala Debugger grus.toml](https://github.com/ensime/scala-debugger/tree/master/grus.toml)
for an example of how to write a config file to fill in values.


# grus
Static site generator written in Scala using Scalatags for templates and
Flexmark for markdown.

## Running

To use in sbt (0.13.x) to generate your own content, add the following plugin:

```scala
addSbtPlugin("org.senkbeil" %% "sbt-grus" % "0.1.2")
```

- `sbt grusGenerate` will generate the website and put the contents in an
  output directory
- `sbt grusServe` will generate the website and start a server to display
  it locally
- `sbt grusPublish` will publish the contents output from `generateSite`

You can add `--help` to any of the above commands to display help information
for the specific command. E.g. `sbt "generateSite --help"`.

## Building a Theme

To use the API to create a custom theme, add the following dependency:

```scala
libraryDependencies += "org.senkbeil" %% "grus-layouts" % "0.1.2"
```

## Examples

See the
[Scala Debugger docs module](https://github.com/ensime/scala-debugger/tree/master/scala-debugger-docs)
for an example of how to write a custom theme.

See the
[Scala Debugger grus.toml](https://github.com/ensime/scala-debugger/tree/master/grus.toml)
for an example of how to write a config file to fill in values.


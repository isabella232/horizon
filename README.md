# Horizon

Current Version: 0.1.3

[View the ScalaDocs](https://paypal.github.com/horizon/api/0.1.3/index.html#com.paypal.horizon.package)

[View the Changelog](https://paypal.github.com/horizon/blob/develop/CHANGELOG.md)

Horizon is an sbt plugin designed to house common build settings for Scala applications. It's carefully designed to:

* Work well with Scala and the Typesafe libraries.
* Be well defined in its functionality.

# Usage

In **project/plugins.sbt**, add:

`addSbtPlugin("com.paypal" % "horizon" % "0.1.3")`

After re-compiling to include the dependency, in **project/Build.scala**, add:

`import com.paypal.horizon.BuildUtilities._`

This will give you access to the main build settings offered by this plugin.

# Plugin Details

## Dependencies

The build utilities plugin depends on several other sbt plugins. In addition to offering customized settings for the features of these plugins, you'll have full access to all of the plugins and settings as well. To access a plugin's settings, add the appropriate imports, which can be found via the plugins' documentation. Below are the plugins and common imports statements for access to settings:

### sbt-release

[sbt-release](https://github.com/sbt/sbt-release) provides a customizable release process for automating releases. `sbt-build-utilties` defines a `defaultReleaseProcess` that mixes standard release steps from `sbt-release` like testing and publishing artifacts with customized release steps like updating the readme and generating Scala docs. See below.

Common imports:

* import sbtrelease._
* import sbtrelease.ReleaseStateTransformations._
* import sbtrelease.ReleasePlugin._
* import sbtrelease.ReleaseKeys._

### sbt-ghpages

[sbt-ghpages](https://github.com/sbt/sbt-ghpages) provides commands to easily push a created "site" to the gh-pages branch of the repository.

The command `$ sbt gh-pages push site` will make and push your site. Our settings cause the Scala docs to get pushed to the gh-pages branch of the project's github repo. See the `generateAndPushDocs` release step, defined in `ScaladocReleaseSteps`.

Common imports:

* import com.typesafe.sbt.SbtGhPages._
* import com.typesafe.sbt.SbtGhPages.GhPagesKeys._

### sbt-site

[sbt-site](https://github.com/sbt/sbt-site) provides commands to create a project site for publishing, and includes commands for auto doc generation.

Using this plugin's settings, the command `$ sbt preview-site` will generate a temporary site of the Scala docs, useful to preview before you actually push. The command `$ sbt make site` will generate the site.

Common imports:

* import com.typesafe.sbt.SbtSite._
* import com.typesafe.sbt.SbtSite.SiteKeys._

### sbt-unidoc

[sbt-unidoc](https://github.com/sbt/sbt-unidoc) works with `sbt-site` to combine multiple sub-projects into one. This is useful so we end up with one Scala doc site instead of multiple. See docs for how to exclude projects if needed.

Common imports:

* import sbtunidoc.Plugin._

### sbt-git

[sbt-git](https://github.com/sbt/sbt-git) is mainly used to set the `gitRemoteRepo` setting, so the other doc-related plugins know the url to push to.

Common imports:

* import com.typesafe.sbt.SbtGit._
* import com.typesafe.sbt.SbtGit.GitKeys._

## BuildUtilitiesKeys

These are static keys/values used in various settings throughout the plugin. They all have defaults and can be customized as needed.

For example, to set the changelog file name from your project's settings, add:

```
changelog := "MyChangelog.txt"

```

This will overwrite the default.

[View the list of build utility keys](https://paypal.github.com/horizon/api/0.1.3/index.html#com.paypal.horizon.BuildUtilitiesKeys$)

## BuildUtilities

`BuildUtilities` is the primary plugin object used to access the main utilities.

### utilitySettings

Add this to the sequence of your project's settings. It combines `sbt-unidoc`, `sbt-site`, and the `sbt-ghpages` settings. It also sets the following additional settings:

* `gitRemoteRepo` (sbt-git) is set to current remote origin, extracted using `sbt-git`.

* `ghpagesNoJekyll` (sbt-ghpages) is set to false.

* `siteMappings` (sbt-site) is overridden to create a folder structure like `/api/$version`.

* `synchLocal` (sbt-ghpages) is overridden so older docs are not deleted

* `repository` (sbt-ghpages) is overridden to clone the repo's gh-pages branch into a directory structure of the form: `~/.sbt/ghpages/$name/$repo`

* `changelog` (key) is set to CHANGELOG.md

* `readme` (key) is set to README.md

* `readmeTemplate` (key) is set to Readme-Template.md

Customize settings in your project's build file as needed.

Add `utilitySettings` to the root project's settings, like so:

```
lazy val parent = Project("parent", file("."),
  settings = standardSettings ++ utilitySettings ++ Seq(
    name := "parent"
  ),
  aggregate = Seq(project1, project2)
)
```

### defaultReleaseProcess

Defines a set of release steps for use with the `sbt-release` plugin. To include `sbt-release` in your project:

1. Add the plugin to `plugins.sbt`, something like `addSbtPlugin("com.github.gseitz" % "sbt-release" % "$version")`
2. Include the release settings, `releaseSettings`, to your root project's build settings.
3. Set the `releaseProcess`,

```
releaseProcess := BuildUtilities.defaultReleaseProcess
```

Release Order:

The following steps are executed in order, and if any of the following steps fail, the release will stop. The methods which implement the logic are provided in paretheses.

1. Check dependencies. (checkSnapshotDependencies)
2. Get release version and determine next version. (inquireVersions)
3. Check that system properties changelog.msg and changelog.author have been set. (checkForChangelog)
4. Run tests. (runTest)
5. Set the release version (removes the SNAPSHOT part). (setReleaseVersion)
6. Commit the release version. (commitReleaseVersion - commits)
7. Update the changelog entry for this release version and commit the changes. (updateChangelog)
8. Generate `README.md` file from `Readme-Template.md`, substituting `{{key}}` with associated value according to the `readmeTemplateMappings` settings. (generateReadme)
9. Tag the release. (tagRelease)
10. Publish artifacts to specified location. (publishArtifacts)
11. Generate ScalaDocs and push to the gh-pages branch. (generateAndPushDocs)
12. Set the next snapshot version. (setNextVersion)
13. Commit the next snapshot version. (commitNextVersion)
14. Push all commits created by this process. (pushChanges)

Caveats: The `defaultReleaseProcess` depends on adding `utilitySettings` to build settings in order to complete the `generateAndPushDocs` release step. The readme release step depends on `Readme-Template.md` file, from which the README.md file is produced. Override setting if file is named differently.



### findManagedDependencies

Finds the managed dependency for use with sbt api mappings. Used in the `apiMappings` build setting.

Example:

```
apiMappings ++= {
  import BuildUtilities._
  val links = Seq(
    findManagedDependency("org.scala-lang", "scala-library").value.map(d => d -> url(s"http://www.scala-lang.org/api/$scalaVsn/")),
    findManagedDependency("com.typesafe.akka", "akka-actor").value.map(d => d -> url(s"http://doc.akka.io/api/akka/$akkaVersion/")),
    findManagedDependency("com.typesafe", "config").value.map(d => d -> url("http://typesafehub.github.io/config/latest/api/")),
    findManagedDependency("org.slf4j", "slf4j-api").value.map(d => d -> url("http://www.slf4j.org/api/")),
    findManagedDependency("com.typesafe.akka", "akka-testkit").value.map(d => d -> url(s"http://doc.akka.io/api/akka/$akkaVersion/")),
    findManagedDependency("org.specs2", "specs2").value.map(d => d -> url(s"http://etorreborre.github.io/specs2/api/SPECS2-$specs2Version/"))
  )
  links.collect { case Some(d) => d }.toMap
}
```

## GitInfo

`GitInfo` is a trait which defines the `gitProperties` method. This method pulls specific git information for the current repository into a key/value sequence. You can use these key/value pairs to extract information for the current build. The current keys include:

* git.branch - Branch name.
* git.branch.clean - Whether the current branch has uncommitted changes or not.
* git.commit.sha - Most recent commit SHA.
* git.commit.date - Date and time of most recent commit.

A good example of how this method is used can be found in [cascade-http](https://github.com/paypal/cascade/blob/develop/README.md#http), a sub-project of PayPal's bigger [Cascade](https://github.com/paypal/cascade/blob/develop/README.md) project, a collection of libraries that implement common patterns and utilities for Scala applications.
The `cascade-http` library provides the `/status` endpoint out of the box. This endpoint returns useful build information for your project, including the service name, dependencies, and git branch and commit information, which is directly provided by this plugin. See the documentation for more details.


## CustomReleaseSteps

Defines custom release steps which are included in the `defaultReleaseProcess` variable.

### ChangelogReleaseSteps

[View the Scaladocs](https://paypal.github.com/horizon/api/0.1.3/#com.paypal.horizon.ChangelogReleaseSteps$)

* `checkForChangelog` - In order for the changelog to get updated, the system properties `changelog.msg` and `changelog.author` must be defined. These are typically defined by CI environment variables, etc. This release step checks to make sure these system properties are defined.

* `updateChangelog` - Creates a changelog entry for the current release version, based on the `changelog.msg` and `changelog.author` system properties. Commits the changelog to the VCS once changes are made. An example entry looks like:

```
# 0.7.0 07/14/14 released by someuser
* Add findManagedDependency for use with apiMappings
```

### ReadmeReleaseSteps

[View the Scaladocs](https://paypal.github.com/horizon/api/0.1.3/#com.paypal.horizon.ReadmeReleaseSteps$)

* `generateReadme` - Generates the readme based on the readme template, and commits the changes to the VCS. By default, the readme file is set to `README.md` and the template file is set to `Readme-Template.md`. The template file can include placeholder words wrapped in `{{...}}` which will be filled in during generation. These placeholders are defined by the `readmeTemplateMappings` setting. It is currently defined as:

```
readmeTemplateMappings <<= (version in ThisBuild) { ver =>
  Map("version" -> ver)
}
```

This setting reads as "Any time there is 0.1.3 in the template, replace it with the current build version". You can define placeholders as needed in your project's build settings using the `<<=` operator to add additional settings to the exisiting set or the `:=` operator to define your own set.

You can generate the readme file at any time without committing by manually executing the `gen-readme` sbt task. For example,

```
$ sbt gen-readme
```

In addition, any instance of `THIS FILE WAS AUTO GENERATED BY THE README TEMPLATE. DO NOT EDIT DIRECTLY.` in the template will get replaced with the following:

`THIS FILE WAS AUTO GENERATED BY THE README TEMPLATE. DO NOT EDIT DIRECTLY.`

This can be useful to include so others changes are not overwritten if they edit the readme directly.

### ScaladocReleaseSteps

[View the Scaladocs](https://paypal.github.com/horizon/api/0.1.3/#com.paypal.horizon.ScaladocReleaseSteps$)

* `generateAndPushDocs` - This uses features from the `sbt-site`, `sbt-unidoc`, and `sbt-ghpages` plugins to generate Scaladocs and push them to the public gh-pages branch of your project's repository. This release step executes the `make-site` sbt task, followed by the `push-site` sbt task. By default, the docs will be pushed to `/api/$version`. Override the `siteMappings` setting to change.

NOTE: Make sure the gh-pages branch already exists before using this release step. See the [github documentation](https://help.github.com/articles/creating-project-pages-manually#create-a-gh-pages-branch) for more information.

# Contributing

We accept and look at all contributions to this project. If you find a bug or
want to see a new feature,
[submit an issue](https://github.com/paypal/horizon/issues).

Otherwise, if you want to contribute code,
[setup your development environment](DEVELOPMENT_SETUP.md), then
[fork this repository](https://github.com/paypal/horizon/fork) and submit a
pull request (PR) against the `develop` branch.

## Bugs

If you create a new issue for a bug, please include as many repro steps as possible.

If you create a pull request, please follow the following rules:

1. If it fixes an existing GH issue, add `Fixes #123` is in the PR description
or one (or more) of the commits in the PR. You can fix multiple bugs from
a single PR, but keep them focused to one subject.
2. If it doesn't fix an existing GH issue, describe the issue in the PR comments.

## Features

The code we've committed here solves the problems that we've seen at PayPal but
we want to add new features that solve others' problems. We're generally looking
for code that automates common (tedious) tasks, reduce or eliminate cognitive
load or context switching, or just solves some annoying problem for developers
using SBT.

We're intentionally leaving the door open for a wide variety of ideas so we (the community) can discuss
in the GH issue or PR.

If you create a PR, please follow the same process as above for referencing the
appropriate GH issue (if any).

THIS FILE WAS AUTO GENERATED BY THE README TEMPLATE. DO NOT EDIT DIRECTLY.

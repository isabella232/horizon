PayPal Stingray sbt-build-utilities
===================================

Current Version: {{version}}

[View the ScalaDocs](https://github.paypal.com/pages/Paypal-Commons-R/sbt-build-utilities/api/{{version}}/index.html#com.paypal.stingray.sbt.package)

[View the Changelog](https://github.paypal.com/Paypal-Commons-R/sbt-build-utilities/blob/develop/CHANGELOG.md)


The Build Utilities plugin was designed so common build settings can be reused via an sbt plugin rather than having to copy code into every new project's build file.

## How to Include In Project

In **project/plugins.sbt**, add:

`addSbtPlugin("com.paypal.stingray" % "sbt-build-utilities" % "{{version}}")`

After re-compiling to download the lib, in **project/Build.scala**, add:

`import com.paypal.stingray.sbt.BuildUtilities._`

This will give you access to the main build settings offered by this plugin.

## What's Included

### Plugin Dependencies

The build utilities plugin depends on several other sbt plugins. In addition to offering customized settings for the features of these plugins, you'll have full access to all of the plugins and settings as well. To access a plugin's settings, add the appropriate imports, which can be found via the plugins' documentation. Below are the plugins and common imports statements for access to settings:

#### sbt-release

[sbt-release](https://github.com/sbt/sbt-release) provides a customizable release process for automating releases. This plugin offers a `defaultReleaseProcess` that mixes standard release steps like testing and publishing artifacts with customized release steps like updating the readme and generating Scala docs. See below.

Common imports:

* import sbtrelease._
* import sbtrelease.ReleaseStateTransformations._
* import sbtrelease.ReleasePlugin._
* import sbtrelease.ReleaseKeys._

#### sbt-ghpages

[sbt-ghpages](https://github.com/sbt/sbt-ghpages) provides commands to easily push a created "site" to the gh-pages branch of the repository.

The command `$ sbt gh-pages push site` will make and push your site. Our settings cause the scala docs to get pushed to the gh-pages branch of the project's github repo.

Common imports:

* import com.typesafe.sbt.SbtGhPages._
* import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
	
#### sbt-site

[sbt-site](https://github.com/sbt/sbt-site) provides commands to create a project site for publishing, and includes commands for auto doc generation.

Using this plugin's settings, the command `$ sbt preview-site` will generate a temporary site of the scala docs, useful to preview before you actually push. The command `$ sbt make site` will generate the site. 

Common imports: 

* import com.typesafe.sbt.SbtSite._
* import com.typesafe.sbt.SbtSite.SiteKeys._

#### sbt-unidoc

[sbt-unidoc](https://github.com/sbt/sbt-unidoc) works with `sbt-site` to combine multiple sub-project documentation into one "site". See docs for how to exclude projects if needed.

Common imports:

* import sbtunidoc.Plugin._

#### sbt-git

[sbt-git](https://github.com/sbt/sbt-git) is mainly used to set the `gitRemoteRepo` setting, so the other doc-related plugins know the url to push to.

Common imports:

* import com.typesafe.sbt.SbtGit._
* import com.typesafe.sbt.SbtGit.GitKeys._
	
### BuildUtilitiesKeys

These are static keys/values used in various settings. They all have defaults and can be customized as needed.

For example, to set the changelog file name, add the project setting:

```
changelog := "MyChangelog.txt"

```

This will overwrite the default.

[View the list of build utility keys](https://github.paypal.com/pages/Paypal-Commons-R/sbt-build-utilities/api/{{version}}/index.html#com.paypal.stingray.sbt.BuildUtilitiesKeys$)

### BuildUtilities

`BuildUtilities` is the primary plugin object used to access the main utilities.

#### utilitySettings

Add this to the sequence of your project's settings val. It combines `sbt-unidoc`, `sbt-site`, and the `sbt-ghpages` settings. It also sets the following additional settings:

* `gitRemoteRepo` (sbt-git) is set to current remote origin, extracted using sbt-git.

* `ghpagesNoJekyll` (sbt-ghpages) is set to false.

* `siteMappings` (sbt-site) is overridden to create a folder structure like /api/$version.

* `synchLocal` (sbt-ghpages) is overridden so older docs are not deleted

* `repository` (sbt-ghpages) is overridden to clone the repo's gh-pages branch into a directory structure of the form: ~/.sbt/ghpages/$name/$repo

* `changelog` (key) is set to CHANGELOG.md

* `readme` (key) is set to README.md

* `readmeTemplate` (key) is set to Readme-Template.md

Customize settings in your project's build file as needed. For example, look at unidoc settings to exclude aggregate projects from the docs.

To use:

Add utilitySettings to the root project's settings. For example:

#### defaultReleaseProcess

#### findManagedDependencies




### GitInfo

### ChangelogReleaseSteps

### ReadmeReleaseSteps

### ScaladocReleaseSteps






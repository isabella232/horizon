sbt-build-utilities
===================

The Build Utilities plugin was designed so common build settings can be imported via an sbt plugin rather than having to copy code in every new project's build file.

## How to Include In Project

In <b>project/plugins.sbt</b>, add:

<code>addSbtPlugin("com.paypal.stingray" % "sbt-build-utilities" % "current-version")</code>

In <b>project/Build.scala</b>, add:

<code>import com.paypal.stingray.sbt.BuildUtilities._</code>

For details on what's included with this plugin, see wiki:

https://confluence.paypal.com/cnfl/display/stingray/Stingray+Build+Utilities

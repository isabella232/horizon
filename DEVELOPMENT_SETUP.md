# Setting Up Your Development Environment

This document describes how to set up your environment to develop Horizon code.

## Prerequisites

Horizon ships with a `Vagrantfile`, so if you have
[`Vagrant`](http://vagrantup.com) you're done. We recommend this approach.

If you don't have Vagrant, you'll need [SBT](http://www.scala-sbt.org/),
[Scala 2.10.4](http://scala-lang.org/download/2.10.4.html) and a Scala
IDE. We recommend using [IntelliJ IDEA](http://www.jetbrains.com/idea/) with
the [Scala plugin](http://plugins.jetbrains.com/plugin/1347?pr=idea).

## Setup

If you're using Vagrant, simply execute the following commands in the project
directory:

```bash
vagrant up
vagrant ssh
cd /vagrant
sbt
```

If you're not using Vagrant, make sure you have the above prerequisites and
type `sbt` in the project directory.

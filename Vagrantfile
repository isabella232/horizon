# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

#script to install openjdk 7, scala 2.11.2 and sbt 0.13.5
$script = <<SCRIPT
apt-get update
apt-get -y install git
apt-get -y install openjdk-7-jdk
wget http://www.scala-lang.org/files/archive/scala-2.11.2.deb
dpkg -i scala-2.11.2.deb
apt-get -y update
apt-get -y install scala

wget http://dl.bintray.com/sbt/debian/sbt-0.13.5.deb
dpkg -i sbt-0.13.5.deb
apt-get -y update
apt-get -y install sbt

#SBT setup for releasing

#export SBT_PGP_PLUGIN=<<EOF
#addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")
#EOF

#export SBT_SONATYPE_CREDENTIALS=<<EOF
#credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", "<your username>", "<your password>")
#EOF

#export SBT_BASE=/home/vagrant/.sbt/0.13

#mkdir -p $SBT_BASE/plugins

#touch $SBT_BASE/plugins/gpg.sbt
#echo $SBT_PGP_PLUGIN > $SBT_BASE/plugins/gpg.sbt
#chmod 777 $SBT_BASE/plugins/gpg.sbt

#touch $SBT_BASE/sonatype.sbt
#echo $SBT_SONATYPE_PLUGIN > $SBT_BASE/sonatype.sbt
#chmod 777 $SBT_BASE/sonatype.sbt

SCRIPT

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "hashicorp/precise64"
  config.vm.provider "virtualbox" do |v|
      v.memory = 2048
      v.customize ["modifyvm", :id, "--cpuexecutioncap", "50"]
  end
  config.vm.provision "shell", inline: $script
end

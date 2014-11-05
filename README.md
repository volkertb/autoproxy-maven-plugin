autoproxy-maven-plugin
======================

Tired of having to manually edit your Maven proxy settings as you move between corporate networks? Now there's a way to handle it automatically. Introducing the Autoproxy Maven plugin.

I don't know about you, but one pet peeve I've had with Maven for years is how it has no support for automatic proxy configuration. As a consequence, I find myself constantly having to edit settings.xml whenever I move from one network to the other. And with all due respect to them and their hard work, the Maven developers have shown no intentions of fixing this shortcoming.

I'm sure I'm not the only mobile Java developer who has struggled with this issue. So I took it as a nice opportunity for a little pet project. The result is this Maven plugin. Under the hood tt makes use of the excellent proxy-vole library written by Bernd Rosstauscher. Although proxy-vole has been bundled as a dependency with the autoproxy-maven-plugin code base, the original sources can also be downloaded from the developer's project site at https://code.google.com/p/proxy-vole/

I've released this plugin under the Apache 2 license, in the hope that it might possibly be adopted upstream by the Maven developers. Also, I hope to have this plugin be made available in the Central Maven Repository soon, which would make the local install step unnecessary. NOTE: proxy-vole is released under a different license. See http://opensource.org/licenses/BSD-3-Clause

Installation
------------

Before you can use the plugin, you first have to install it to your local repository by building it. (Once the plugin is available in the Central Maven Repository, this step will no longer be necessary.)

    git clone https://github.com/volkertb/autoproxy-maven-plugin.git
    cd autoproxy-maven-plugin
    mvn clean install
    
Once you've entered the above command, the plugin should now be installed in your local repository (in the .m2 folder in your home folder).

Usage
-----

You can use the plugin from the command line with any Maven project, without having to modify the POM. You can do so as follows:

    cd some-maven-project
    mvn com.buisonje:autoproxy-maven-plugin:detectProxy clean install

As you can see, the goal that you need to specify right after mvn is a bit long. The reason for this is because locally installed plugins need to be fully qualified on the command-line. Adding the plugin to your POM would make it shorter (autoproxy:detectProxy). It will also become shorter once the plugin has been admitted into the Central Maven Repository. That would also allow it to be used directly without having to build and install it locally from source first.

This is an open-source project, and this first version is still a bit limited, and possibly buggy. Also, it depends on the proxy-vole plugin, which works well as far as I can tell, but which I haven't thoroughly scrutinized. Therefore, if you're a bit handy with Java, and would like to contribute, you're absolutely welcome! All help is appreciated. :-)

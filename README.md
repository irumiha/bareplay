
BarePlay
--------

What you are looking at here is an example of a minimal approach to building a
[Playframework](https://www.playframework.com/) application.

Playframework is for some reason regarded as a heawyweight framework.
I think much of that perception comes from the use of Sbt play plugin which promotes 
unusual project structure and gives us Twirl templates, the routes file, and a 
complicated dev mode. 

However, Playframework is a very flexible piece of software. You don't need to use 
all that is prescribed by the default setup you get when checking out the 
playframework seed project. 

What happens when we remove the Play Sbt plugin?

#### Dependencies are declared explicitely.

Play Sbt plugin injects several default dependencies and allows you to include additional modules such as
`ws`, `cache` etc. These are now declared explicitely. Use Maven search to find out what is available.

#### Standard project structure.

Project structure is the usual `src/{main,test}/{scala,resources}` structure you know and love.

#### There is no code generation step. 

`routes` file is replaced with [SIRD](https://www.playframework.com/documentation/2.8.x/ScalaSirdRouter) router 
definition, and Twirl templates are replaced with [ScalaTags](https://com-lihaoyi.github.io/scalatags/).

Everything that was in the `conf` directory has moved to `src/main/resources`.

All assets are in `src/main/resources/public` 

#### There is no Dev server. 

We use `play.core.server.ProdServerStart` for both dev and prod. 
`sbt-revolver` plugin is included so just run `~ reStart` and have fun!

#### Dependency injection

Here we use compile-time dependency injection with MacWire. It should be fairly simple to continue
using Guice if you feel like it.



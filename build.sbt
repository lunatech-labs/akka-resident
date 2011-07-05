name := "Resident"

version := "0.1"

scalaVersion := "2.9.0"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies += "pircbot" % "pircbot" % "1.5.0" withSources()

libraryDependencies += "se.scalablesolutions.akka" % "akka-actor" % "1.1.2" withSources()
 
libraryDependencies += "se.scalablesolutions.akka" % "akka-remote" % "1.1.2" withSources()


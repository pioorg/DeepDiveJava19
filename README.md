# DeepDiveJava19

This is a demo project to show some new features of Java™ 19. (For previous version go to [DeepDiveJava18](https://github.com/pioorg/DeepDiveJava18).)

It uses `--enable-preview` and Maven, but you don't need to have Maven installed.

To run this project, you need to have Java™ 19 installed. You can use https://sdkman.io/ and/or https://openjdk.java.net/, https://adoptium.net/

If you'd like to run the example app, one of the options is this:

    $ MAVEN_OPTS="--enable-preview" ./mvnw exec:java -pl goodies

(If it doesn't work, you might have Java 19 not installed.)

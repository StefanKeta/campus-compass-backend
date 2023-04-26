FROM sbtscala/scala-sbt:graalvm-ce-22.3.0-b2-java17_1.8.2_3.2.2

COPY ../../. /sources

CMD sbt runGlobal
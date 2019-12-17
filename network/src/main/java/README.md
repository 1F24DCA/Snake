# Network
Java network API using Socket, communicate with String values.

If you modify this API and re-compile it, please use annotation processor. you can make annotation processor JAR file to use command below (use JDK 1.8):

[LOCAL REPOSITORY PATH]> javac kr/pe/firstfloor/annotation/processor/CompileProcessor.java & jar -cvf Processor.jar kr/pe/firstfloor/annotation/processor/CompileProcessor.class META-INF/services/javax.annotation.processing.Processor

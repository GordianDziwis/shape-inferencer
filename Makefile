all:
	sbt assembly
start-build-server:
	bloop server
continously-build:
	bloop run root -w -- -i ./src/main/resources/rdf.nt
run-fat-jar:
	cd ./target/scala-2.11 && \
	java -jar shape-inferencer-assembly-1.0.jar -i ./../../src/main/resources/rdf.nt
debug-fat-jar:
	cd ./target/scala-2.11 && \
	java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=y -jar shape-inferencer-assembly-1.0.jar -i ./../../src/main/resources/rdf.nt

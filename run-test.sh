#!/bin/sh
set -eu
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
javac -d out src/main/java/example/*.java src/test/java/example/OrderLoggingServiceTest.java
java -cp out example.OrderLoggingServiceTest

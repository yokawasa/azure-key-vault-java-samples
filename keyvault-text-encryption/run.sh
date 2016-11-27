#!/bin/sh

mvn compile

INPUT="Hello World"
mvn exec:java -Dexec.mainClass="App" -Dexec.args="-c app.conf -t '$INPUT'"

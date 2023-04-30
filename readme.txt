The project is developed and built on Ubuntu 20.04 LTS.
javac 11.0.18
openjdk version "11.0.18" 2023-01-17
OpenJDK Runtime Environment (build 11.0.18+10-post-Ubuntu-0ubuntu120.04.1)
OpenJDK 64-Bit Server VM (build 11.0.18+10-post-Ubuntu-0ubuntu120.04.1, mixed mode, sharing)


build the project with:
javac -cp ".:lib/htmlparser.jar:lib/jdbm-1.0.jar:src" src/*.java -d bin/

perform webpage crawling with:
java -cp ".:lib/htmlparser.jar:lib/jdbm-1.0.jar:bin" Spider 
Spider is rerunnable, without deleting the test.db and test.lg file.

generate spider_result.txt with:
java -cp ".:lib/htmlparser.jar:lib/jdbm-1.0.jar:bin" Testing
Suggest deleting the spider_result.txt mannually before running.


javac -cp ".:lib/htmlparser.jar:lib/jdbm-1.0.jar:src" src/core/*.java -d bin/

perform webpage crawling with:
java -cp ".:lib/htmlparser.jar:lib/jdbm-1.0.jar:bin" core.Spider <number of pages to crawl>
Spider is rerunnable, without deleting the test.db and test.lg file.

perform pagerank calculation with:
java -cp ".:lib/htmlparser.jar:lib/jdbm-1.0.jar:bin" core.PageRank
PageRank is rerunnable.

testing search function backend with:
java -cp ".:lib/htmlparser.jar:lib/jdbm-1.0.jar:bin" core.SearchEngine
Start a searchEngine cmdline tool. Will automatically perform relevance feedback using the first returned result.

building jar
jar cvfm searchengine.jar resources/manifest.txt -C bin . src 

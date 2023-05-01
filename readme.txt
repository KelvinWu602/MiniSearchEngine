javac -cp ".:lib/htmlparser.jar:lib/jdbm-1.0.jar:src" src/*.java -d bin/

perform webpage crawling with:
java -cp ".:lib/htmlparser.jar:lib/jdbm-1.0.jar:bin" Spider <number of pages to crawl>
Spider is rerunnable, without deleting the test.db and test.lg file.

perform pagerank calculation with:
java -cp ".:lib/htmlparser.jar:lib/jdbm-1.0.jar:bin" PageRank
PageRank is rerunnable.

testing search function backend with:
java -cp ".:lib/htmlparser.jar:lib/jdbm-1.0.jar:bin" SearchEngine
Modify the function argument in main method, use double quote to specify a phrase, use space to separate words.

url = http://127.0.0.1:8081/a.html

clean:
	rm *.db *.lg

run:
	javac -cp jdbm-1.0.jar HMap.java
	javac -cp htmlparser.jar:jdbm-1.0.jar:jsoup-1.15.4.jar:. Spider.java
	javac -cp htmlparser.jar:jdbm-1.0.jar:jsoup-1.15.4.jar:. SearchEngine.java
	java -cp htmlparser.jar:jdbm-1.0.jar:jsoup-1.15.4.jar:. SearchEngine $(url)

test:
	javac -cp jdbm-1.0.jar:. Tester.java
	java -cp jdbm-1.0.jar:. Tester

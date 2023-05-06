url = http://www.cse.ust.hk/

clean:
	rm *.db *.lg
fast:
	javac -cp jdbm-1.0.jar HMap.java
	javac -cp htmlparser.jar:jdbm-1.0.jar:. Spider.java
	javac -cp htmlparser.jar:jdbm-1.0.jar:. SearchEngine.java
	java -cp htmlparser.jar:jdbm-1.0.jar:. SearchEngine $(url) fast

slow:
	javac -cp jdbm-1.0.jar HMap.java
	javac -cp htmlparser.jar:jdbm-1.0.jar:. Spider.java
	javac -cp htmlparser.jar:jdbm-1.0.jar:. SearchEngine.java
	java -cp htmlparser.jar:jdbm-1.0.jar:. SearchEngine $(url) slow

test:
	javac -cp jdbm-1.0.jar:. Tester.java
	java -cp jdbm-1.0.jar:. Tester

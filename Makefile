url = http://www.cse.ust.hk

clean:
	rm *.db *.lg

run:
	javac Utilities/Porter.java
	javac Utilities/StopStem.java
	javac -cp lib/jdbm-1.0.jar HMap.java
	javac -cp lib/htmlparser.jar:lib/jdbm-1.0.jar:lib/jsoup-1.15.4.jar:. Spider.java
	javac -cp lib/htmlparser.jar:lib/jdbm-1.0.jar:lib/jsoup-1.15.4.jar:. SearchEngine.java
	java -cp lib/htmlparser.jar:lib/jdbm-1.0.jar:lib/jsoup-1.15.4.jar:. SearchEngine $(url)

test:
	javac -cp lib/jdbm-1.0.jar:. Tester.java
	java -cp lib/jdbm-1.0.jar:. Tester

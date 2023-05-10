url = https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm
user_path = /Users/wilsonthiesman/Downloads/apache-tomcat-11.0.0-M5/

clean:
	rm *.db *.lg

copy:
	cp test.txt ../../../../bin

compile:
	javac utilities/Porter.java
	javac utilities/StopStem.java
	javac -cp lib/jdbm-1.0.jar utilities/HMap.java
	javac -cp lib/jdbm-1.0.jar:. engine/Retrieval.java
	javac -cp lib/htmlparser.jar:lib/jdbm-1.0.jar:lib/jsoup-1.15.4.jar:. engine/Spider.java
	javac -cp lib/htmlparser.jar:lib/jdbm-1.0.jar:lib/jsoup-1.15.4.jar:. engine/SearchEngine.java

crawl: compile
	java -cp lib/htmlparser.jar:lib/jdbm-1.0.jar:lib/jsoup-1.15.4.jar:. engine/SearchEngine 300 $(url)

run:
	../../../../bin/shutdown.sh
	../../../../bin/catalina.sh run

first_run: crawl copy run

test:
	javac -cp lib/jdbm-1.0.jar:. engine/Tester.java
	java -cp lib/jdbm-1.0.jar:. engine/Tester

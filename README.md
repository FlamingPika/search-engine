# Prerequisite:
1. Download the zip file and unzipped it.
2. Make sure that the .jar files are inside the right directory as well as the java files
3. If there exists any .db or .lg files, clean it first using the command "make clean"
4. Refer to https://github.com/FlamingPika/search-engine for the directory skeleton
5. To run the Makefile, go to WEB-INF/classes/ directory


# File Structure

```
.
│
├── WEB-INF
│   ├── classes
│   │   ├── Makefile
│   │   ├── debug_retrieve.txt
│   │   ├── engine
│   │   │   ├── Retrieval.java
│   │   │   ├── SearchEngine.java
│   │   │   └── Spider.java
│   │   ├── lib
│   │   │   ├── htmlparser.jar
│   │   │   ├── jdbm-1.0.jar
│   │   │   └── jsoup-1.15.4.jar
│   │   ├── stopwords.txt
│   │   └── utilities
│   │       ├── HMap.java
│   │       ├── Porter.java
│   │       └── StopStem.java
│   └── lib
│       ├── htmlparser.jar
│       ├── jdbm-1.0.jar
│       └── jsoup-1.15.4.jar
├── assets
│   ├── css
│   │   ├── fontawesome-all.min.css
│   │   ├── main.css
│   │   └── styles.css
│   ├── db
│   │   ├── id_url.db
│   │   ├── ...
│   │   └── word-page.db
│   ├── images
│   │   ├── LOGO_UST_white.png
│   │   ├── cse.png
│   │   └── me.png
│   └── lg
│       ├── id_url.lg
│       ├── ...
│       └── word-page.lg
├── js
│   ├── breakpoints.min.js
│   ├── ...
│   └── util.js
├── out
│   └── production
│       └── SEProject
│           ├── Makefile
│           ├── SEProject.iml
│           ├── assets
│           └── ...
│
├── .gitignore
├── SEProject.iml
├── database design.pdf
├── example1.jsp
├── form.html
└── stopwords.txt
```
# Instruction

To build and execute the spider to crawl, you can use the existing make file:

make first_run = to perform crawling and display the user interface in http://localhost:8080/SEProject/form.html

make run = to run the interface without crawling

make compile = to compile the program

make copy = to copy all the database file into user/path/../bin/

make clean = to clean the .db and .lg files

make test = to test whether the spider can run successfully by retrieving data from the existing databases and
            print it to test.txt . Keep in mind that you need to have the databases file before running this.

<strong>[IMPORTANT]</strong> before running any of the commands above, please make sure that no .db or .lg files exist. If there exists any, clean it using the command "make clean"
<strong>[IMPORTANT]</strong> to keep on restarting the apache server if you make changes to the system
<strong>[IMPORTANT]</strong> to EMPTY out and COPY the database file into the apache bin directory, for the apache server to fetch from the database.

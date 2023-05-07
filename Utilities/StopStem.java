package Utilities;

import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;

public class StopStem
{
	private Porter porter;
	private static final String STOPWORDS_FILE = "stopwords.txt";
	private HashSet<String> stopWords;
	public StopStem(String str)
	{
		super();
		porter = new Porter();
		stopWords = new HashSet<String>();

		try {

			BufferedReader bufferedReader = new BufferedReader(new FileReader(str));
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				stopWords.add(line);
			}
			System.out.println("Total of " + stopWords.size() + " stopwords loaded.");
			bufferedReader.close();

		} catch (IOException e){
			e.printStackTrace();
		}
	}
	public boolean isStopWord(String str)
	{
		return stopWords.contains(str);
	}
	public String stem(String str)
	{
		return porter.stripAffixes(str);
	}

}

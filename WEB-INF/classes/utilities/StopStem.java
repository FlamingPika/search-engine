package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class StopStem
{
	private Porter porter;
	private final String STOPWORDS_FILE = "stopwords.txt";
	private static HashSet<String> stopWords;
	public StopStem()
	{
		super();
		porter = new Porter();
		stopWords = new HashSet<String>();

		try {

			BufferedReader bufferedReader = new BufferedReader(new FileReader(STOPWORDS_FILE));
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
	public static boolean isAlphaNum(String str)
	{
		return str != null && str.matches("^[a-zA-Z0-9]+$");
	}
	public boolean isStopWord(String str)
	{
		return stopWords.contains(str.toLowerCase());
	}
	public String stem(String str)
	{
		return porter.stripAffixes(str);
	}

}

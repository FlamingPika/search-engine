import Utilities.StopStem;
import java.io.IOException;
import java.util.*;

public class Retrieval {
    StopStem stopStem;
    List<String> queries;
    int numDocs;
    HMap word_page;
    HMap page_word;
    HMap page_title;
    HMap title_page;
    HashMap<Integer, Double> titleMaxFreq;
    HashMap<Integer, Double> bodyMaxFreq;
    HashMap<String, Integer> queryFreq;
    Retrieval(int _numDocs) {

        numDocs = _numDocs;
        stopStem = new StopStem();
        titleMaxFreq = new HashMap<>();
        bodyMaxFreq = new HashMap<>();
        queryFreq = new HashMap<>();
        queries = new ArrayList<String>();

        try {
            word_page = new HMap("word-page", "word-page");
            page_word = new HMap("page-word", "page-word");
            page_title = new HMap("page-title", "page-title");
            title_page = new HMap("title-page", "title-page");
        } catch (IOException e) {
            System.out.println("Failed in initializing..");
        }

    }
    private int findMax(int pageID, int h) {
        try {
            HashMap<String, Integer> words;
            if (h == 0) {
                words = page_title.getWords(pageID);
            } else {
                words = page_word.getWords(pageID);
            }
            if (words == null) {
                System.out.println("NO WAY!");
            }
            Set<String> keys = words.keySet();
            int maxFreq = 0;
            for (String key : keys) {
                int freq = words.get(key);
                if (freq > maxFreq)
                    maxFreq = freq;
            }
            return maxFreq;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
    private double calculateTF(double titleFrequency, double maxTitleFrequency, double bodyFrequency, double maxBodyFrequency) {
        return 0.7 * (titleFrequency/maxTitleFrequency) + 0.3 * (bodyFrequency/maxBodyFrequency);
    }
    private Double calculateIDF(double docFrequency) {
        return Math.log(1+(numDocs / docFrequency));
    }
    private double calculateTermWeight(int pageID, String query){

        try {
            System.out.println("PAGE = " + pageID);
            HashMap<Integer, Integer> pageWordsFreq = word_page.getHashTable(query);
            HashMap<Integer, Integer> pageTitleFreq = title_page.getHashTable(query);
            double titleFrequency;
            double bodyFrequency;
            if (pageWordsFreq == null) {
                bodyFrequency = 0;
                System.out.println("bodyFrequency NULL");
            } else {
                bodyFrequency = (pageWordsFreq.get(pageID) != null) ? pageWordsFreq.get(pageID) : 0;
            }
            if (pageTitleFreq == null) {
                titleFrequency = 0;
                System.out.println("titleFrequency NULL");
            } else {
                titleFrequency = (pageTitleFreq.get(pageID) != null) ? pageTitleFreq.get(pageID) : 0;
            }

            if (titleFrequency == 0 && bodyFrequency == 0) return 0;

            double maxTitleFrequency = (titleMaxFreq.get(pageID) != null) ? titleMaxFreq.get(pageID) : findMax(pageID, 0);
            if (titleMaxFreq.get(pageID) != null) titleMaxFreq.put(pageID, maxTitleFrequency);
            double maxBodyFrequency = (bodyMaxFreq.get(pageID) != null) ? bodyMaxFreq.get(pageID) : findMax(pageID, 1);
            if (bodyMaxFreq.get(pageID) != null) bodyMaxFreq.put(pageID, maxBodyFrequency);

            System.out.println("PAGEID = " + pageID + "\t QUERY = " + query);
            System.out.println(titleFrequency + "\t" + maxTitleFrequency + "\t" + bodyFrequency + "\t" + maxBodyFrequency);
            return calculateTF(titleFrequency, maxTitleFrequency, bodyFrequency, maxBodyFrequency) * calculateIDF(pageWordsFreq.size());

        } catch (IOException e) {
            System.out.println("Failed to calculate term weight for " + query + " with pageID = " + pageID);
            return -1;
        }
    }

    private double calculateSimilarity(int pageID) {
        double dotProduct = 0;
        double magnitudeD = 0;
        double magnitudeQ = 0;
        for (String query: queries) {
            System.out.println("QUERY = " + query);
            double dq = calculateTermWeight(pageID, query);
            int f = queryFreq.get(query);
            magnitudeD = magnitudeD + (dq * dq);
            magnitudeQ = magnitudeQ + (f * f);
            dotProduct = dotProduct + (dq * f);
        }
        System.out.println("RAW = " + dotProduct + " / (sqrt(" + magnitudeD + ") * sqrt(" + magnitudeQ + "))");
        return dotProduct / (Math.sqrt(magnitudeD) * Math.sqrt(magnitudeQ));

    }

    private void preprocess(String _queries) {
        List<String> rawQueries = Arrays.asList(_queries.split(" "));
        for (int i = 0; i < rawQueries.size(); ++i) {
            String word = rawQueries.get(i);
            System.out.println("PREPROCESS = " + word);
            if (stopStem.isStopWord(word)) continue;
            word = stopStem.stem(word);
            if (queryFreq.get(word) != null) {
                queryFreq.put(word, queryFreq.get(word) + 1);
            } else {
                queryFreq.put(word, 1);
            }
            System.out.println("PROCESSED = " + word);
            queries.add(word);
        }
    }

    public List<Integer> retrieve(int limit, String _queries){
        preprocess(_queries);
        PriorityQueue<Double> queue = new PriorityQueue<Double>();
        HashMap<Double, List<Integer>> simToPage = new HashMap<Double, List<Integer>>();
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0 ;i < numDocs; ++i) {
            double sim = calculateSimilarity(i);
            System.out.println("SIMILARITY = " + sim);
            if (sim != 0) {
                queue.add(sim);
                List<Integer> l;
                if (simToPage.get(sim) != null) {
                    l = simToPage.get(sim);
                } else {
                    l = new ArrayList<Integer>();
                }
                l.add(i);
                simToPage.put(sim, l);
            }
        }
        for (int i = 0; i < limit; ++i) {
            double sim = queue.poll();
            if (sim == 0) break;
            List<Integer> l = simToPage.get(sim);
            boolean full = false;
            for (int j = 0; j < l.size(); ++j) {
//                System.out.println("Iteration " + i + " with sim = " + sim);
                result.add(l.get(j));
                if (result.size() >= limit) {
                    full = true;
                    break;
                }
                if (full == true) break;
            }
        }
        queries.clear();
        return result;

    }
}

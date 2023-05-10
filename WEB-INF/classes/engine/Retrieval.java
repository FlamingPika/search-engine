package engine;
import utilities.StopStem;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.HashMap;
import utilities.HMap;

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
    HashMap<Integer, HashMap<String, Double>> bookOfKnowledge;
    Retrieval(int _numDocs) {

        numDocs = _numDocs;
        stopStem = new StopStem();
        titleMaxFreq = new HashMap<>();
        bodyMaxFreq = new HashMap<>();
        queryFreq = new HashMap<>();
        queries = new ArrayList<String>();
        bookOfKnowledge = new HashMap<Integer, HashMap<String, Double>>();

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

    private void calculateTermWeight(int pageID){
        try {
            HashMap<String, Integer> pageWordsFreq = page_word.getWords(pageID);
            HashMap<String, Integer> pageTitleFreq = page_title.getWords(pageID);
            int titleCount = pageTitleFreq.size();
            int counter = 0;
            Set<String> wordSet = pageWordsFreq.keySet();
            for(String word: wordSet) {
                double bodyFrequency;
                if (word != null) {
                    bodyFrequency = pageWordsFreq.get(word);
                } else {
                    break;
                }
                double titleFrequency;
                if (pageTitleFreq.get(word) != null) {
                    titleFrequency = pageTitleFreq.get(word);
                    counter = counter + 1;
                } else {
                    titleFrequency = 0;
                }
                double maxTitleFrequency = (titleMaxFreq.get(pageID) != null) ? titleMaxFreq.get(pageID) : findMax(pageID, 0);
                if (titleMaxFreq.get(pageID) != null) titleMaxFreq.put(pageID, maxTitleFrequency);
                double maxBodyFrequency = (bodyMaxFreq.get(pageID) != null) ? bodyMaxFreq.get(pageID) : findMax(pageID, 1);
                if (bodyMaxFreq.get(pageID) != null) bodyMaxFreq.put(pageID, maxBodyFrequency);

                double weight = calculateTF(titleFrequency, maxTitleFrequency, bodyFrequency, maxBodyFrequency) * calculateIDF(pageWordsFreq.size());
                HashMap<String, Double> h;
                if (bookOfKnowledge.get(pageID) != null) {
                    h = bookOfKnowledge.get(pageID);
                } else {
                    h = new HashMap<>();
                }
                h.put(word, weight);
                bookOfKnowledge.put(pageID, h);

            }
            if (counter < titleCount) {
                wordSet = pageTitleFreq.keySet();
                for (String word: wordSet) {
                    if (pageWordsFreq.get(word) != null) {
                        continue;
                    }
                    double bodyFrequency = 0;
                    double titleFrequency = pageTitleFreq.get(word);
                    counter = counter + 1;
                    double maxTitleFrequency = (titleMaxFreq.get(pageID) != null) ? titleMaxFreq.get(pageID) : findMax(pageID, 0);
                    if (titleMaxFreq.get(pageID) != null) titleMaxFreq.put(pageID, maxTitleFrequency);
                    double maxBodyFrequency = (bodyMaxFreq.get(pageID) != null) ? bodyMaxFreq.get(pageID) : findMax(pageID, 1);
                    if (bodyMaxFreq.get(pageID) != null) bodyMaxFreq.put(pageID, maxBodyFrequency);

                    double weight = calculateTF(titleFrequency, maxTitleFrequency, bodyFrequency, maxBodyFrequency) * calculateIDF(pageWordsFreq.size());
                    HashMap<String, Double> h;
                    if (bookOfKnowledge.get(pageID) != null) {
                        h = bookOfKnowledge.get(pageID);
                    } else {
                        h = new HashMap<>();
                    }
                    h.put(word, weight);
                    bookOfKnowledge.put(pageID, h);

                }
            }

        } catch (IOException e) {
            System.out.println("Failed to calculate term weight with pageID = " + pageID);
        }
    }

    private double calculateSimilarity(int pageID) {
        double dotProduct = 0;
        double magnitudeD = 0;
        double magnitudeQ = 0;
        calculateTermWeight(pageID);
        HashMap<String, Double> h = bookOfKnowledge.get(pageID);
        for (String query: queries) {
            int f = queryFreq.get(query);
            if (h.get(query) != null) {
                dotProduct = dotProduct + (h.get(query) * f);
            }
            magnitudeQ = magnitudeQ + (f * f);
        }
        Set<String> keys = h.keySet();
        for (String key : keys) {
            double dq = h.get(key);
            magnitudeD = magnitudeD + (dq * dq);
        }
        return dotProduct / (Math.sqrt(magnitudeD) * Math.sqrt(magnitudeQ));
    }

    private void preprocess(String _queries) {
        List<String> rawQueries = Arrays.asList(_queries.split(" "));
        for (int i = 0; i < rawQueries.size(); ++i) {
            String word = rawQueries.get(i);
            if (stopStem.isStopWord(word)) continue;
            word = stopStem.stem(word);
            if (stopStem.isStopWord(word)) continue;
            if (queryFreq.get(word) != null) {
                queryFreq.put(word, queryFreq.get(word) + 1);
            } else {
                queryFreq.put(word, 1);
            }
            queries.add(word);
        }
    }

    public List<Integer> retrieve(int limit, String _queries){
        preprocess(_queries);
        PriorityQueue<Double> queue = new PriorityQueue<Double>(Comparator.reverseOrder());
        HashMap<Double, List<Integer>> simToPage = new HashMap<Double, List<Integer>>();
        List<Integer> result = new ArrayList<Integer>();
        int temp = 0;
        for (int i = 0 ;i < numDocs; ++i) {
            double sim = calculateSimilarity(i);
            System.out.println("SIMILARITY = " + sim);
            if (sim != 0) {
                temp = temp + 1;
                queue.offer(sim);
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
        System.out.println("THERE ARE " + temp + " SIMILARITIES");
        for (int i = 0; i < limit; ++i) {
            if (queue.peek() == null) break;
            double sim = queue.poll();
            if (sim == 0) break;
            List<Integer> l = simToPage.get(sim);
            boolean full = false;
            for (int j = 0; j < l.size(); ++j) {
                result.add(l.get(j));
                if (result.size() >= limit) {
                    full = true;
                    break;
                }

            }
            if (full == true) break;
        }
        queries.clear();
        return result;

    }
}

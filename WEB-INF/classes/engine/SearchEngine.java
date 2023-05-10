package engine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import utilities.HMap;

public class SearchEngine {
    private Spider spider;
    String url;
    private Retrieval retrieval;
    private int num_of_index;

    private HMap url_id;
    private HMap id_url;
    private HMap parent_child;
    private HMap child_parent;
    private HMap word_page;
    private HMap title_page;
    private HMap page_word;
    private HMap page_title;
    private HMap page_props;

    /**
     * Constructing the SearchEngine object by initializing its spider to crawl
     * and specifying the necessary parameters of the spider.
     *
     */
    public SearchEngine(int _num_of_index, String _url) {
        System.out.println("NUM OF INDEX = " + _num_of_index);
        num_of_index = _num_of_index;
        url = _url;
        spider = new Spider(_num_of_index, "debug_retrieve.txt", "spider_retrieve.txt", "log_retrieve.txt");
        retrieval = new Retrieval(num_of_index);
        try {
            url_id = new HMap("url-id", "url-id");
            id_url = new HMap("id_url", "id_url");
            parent_child = new HMap("parent-child", "parent-child");
            child_parent = new HMap("child-parent", "child-parent");
            word_page = new HMap("word-page", "word-page");
            page_word = new HMap("page-word", "page-word");
            title_page = new HMap("title-page", "title-page");
            page_title = new HMap("page-title", "page-title");
            page_props = new HMap("page-props", "page-props");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Browse a given url by crawling it and indexing it.
     *
     */

    public void crawl() {
        spider.crawl(url);

    }

    public List<HashMap<String, String>> search(String input){
        List<Integer> l = retrieval.retrieve(50, input);

        List<HashMap<String, String>> result = new ArrayList<>();
        int magic_number = 5;
        try {
            for (int i = 0; i < l.size(); ++i) {
                HashMap<String, String> h = new HashMap<>();
                Vector<String> res = page_props.getResult(l.get(i));
                String url = id_url.getEntry(l.get(i));
                h.put("title", res.get(0));
                h.put("url", url);
                h.put("info", res.get(2));

                int counter = 0;
                PriorityQueue<Integer> queue = new PriorityQueue<Integer>(Comparator.reverseOrder());
                HashMap<String,Integer> hh = page_word.getWords(l.get(i));
                HashMap<Integer,List<String>> freqWord = new HashMap<>();
                if (hh != null) {
                    Set<String> words = hh.keySet();
                    StringBuilder freq = new StringBuilder();
                    for (String w : words) {
                        if (w == null) {
                            break;
                        }
                        List<String> lw;
                        if (freqWord.get(hh.get(w)) != null) {
                            lw = freqWord.get(hh.get(w));
                        } else {
                            lw = new ArrayList<>();
                        }
                        lw.add(w);
                        if (freqWord.get(hh.get(w)) == null) {
                            queue.offer(hh.get(w));
                        }
                        freqWord.put(hh.get(w), lw);

                    }
                    for (int k = 0; k < magic_number; ++k) {
                        if (queue.peek() == null) break;
                        int highFreq = queue.poll();
                        List<String> lw = freqWord.get(highFreq);
                        boolean flag = false;
                        System.out.println("==========");
                        for (int j = 0; j < lw.size(); ++j) {
                            System.out.println("XX " + lw.get(j));
                            if (counter >= magic_number || counter == words.size()) {
                                flag = true;
                                break;
                            }
                            freq.append(lw.get(j) + " " + highFreq + "; ");
                            counter++;
                        }
                        System.out.println("==========");
                        if (flag == true) break;
                    }
//                    System.out.println(freq.toString());
                    h.put("freq", freq.toString());
                }

                ArrayList<Integer> parent = child_parent.getID(l.get(i));

                if (parent != null) {
                    StringBuilder parentStr = new StringBuilder();
                    Iterator<Integer> numbersIterator = parent.iterator();
                    counter = 0;
                    while (numbersIterator.hasNext()) {
                        if (counter >= magic_number || counter == parent.size()) {
                            break;
                        }
                        parentStr.append(id_url.getEntry(numbersIterator.next()) + "\n");
                        counter++;
                    }
                    h.put("parent", parentStr.toString());

                }

                ArrayList<Integer> child = parent_child.getID(l.get(i));

                if (child != null) {
                    StringBuilder childStr = new StringBuilder();
                    Iterator<Integer> numbersIterator = child.iterator();
                    counter = 0;
                    while (numbersIterator.hasNext()) {
                        if (counter >= magic_number || counter == child.size()) {
                            break;
                        }
                        childStr.append(id_url.getEntry(numbersIterator.next()) + "\n");
                        counter++;
                    }
                    h.put("child", childStr.toString());

                }
                result.add(h);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }



    public static void main(String[] args) {
        System.out.println(args[0]);
        System.out.println(args[1]);
        SearchEngine se = new SearchEngine(Integer.parseInt(args[0]), args[1]);
        se.crawl();
//        String input="";
//        try {
//            do {
//                System.out.print("Search: ");
//                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//                input = in.readLine();
//                if (input.length() > 0) {
//                    se.search(input);
//                }
//            }
//            while (input.length() > 0);
//        } catch (IOException e){
//            e.printStackTrace();
//        }



    }
}
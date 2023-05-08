import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class SearchEngine {
    private Spider spider;
    private Retrieval retrieval;
    private int num_of_index;

    /**
     * Constructing the SearchEngine object by initializing its spider to crawl
     * and specifying the necessary parameters of the spider.
     *
     */
    SearchEngine(int _num_of_index) {
        System.out.println("NUM OF INDEX = " + _num_of_index);
        num_of_index = _num_of_index;
        spider = new Spider(_num_of_index, "debug_retrieve.txt", "spider_retrieve.txt", "log_retrieve.txt");
    }

    /**
     * Browse a given url by crawling it and indexing it.
     *
     * @param  url  the url link to processed
     */

    public void crawl(String url) {
        spider.crawl(url);
        retrieval = new Retrieval(num_of_index);
    }

    public void search(String input){
        List<Integer> l = retrieval.retrieve(50, input);
        for (int i = 0; i < l.size(); ++i){
            System.out.println(l.get(i));
        }
    }


    public static void main(String[] args) {
        System.out.println(args[0]);
        SearchEngine se = new SearchEngine(Integer.parseInt(args[0]));
        se.crawl(args[1]);
        String url = args[0];
        String input="";
        try {
            do {
                System.out.print("Search: ");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                input = in.readLine();
                if (input.length() > 0) {
                    se.search(input);
                }
            }
            while (input.length() > 0);
        } catch (IOException e){
            e.printStackTrace();
        }



    }
}

public class SearchEngine {
    private Spider spider;

    /**
     * Constructing the SearchEngine object by initializing its spider to crawl
     * and specifying the necessary parameters of the spider.
     *
     */
    SearchEngine() {
        spider = new Spider(30, "debug_final.txt", "spider_result_final.txt", "log_final.txt");
    }

    /**
     * Browse a given url by crawling it and indexing it.
     *
     * @param  url  the url link to processed
     */

    public void browse(String url) {
        spider.crawl(url);
    }


    public static void main(String[] args) {
        SearchEngine se = new SearchEngine();
        se.browse(args[0]);
        if (args[1].equals("slow")) {
            System.out.println("doing slow browsing...");
            se.browse(args[0]);
        } else {
            System.out.println("doing fast browsing...");
            se.fast_browse(args[0]);
        }

    }
}
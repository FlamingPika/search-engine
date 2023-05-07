
public class SearchEngine {
    private Spider spider;

    /**
     * Constructing the SearchEngine object by initializing its spider to crawl
     * and specifying the necessary parameters of the spider.
     *
     */
    SearchEngine() {
        spider = new Spider(30, "debug_test.txt", "spider_result_test.txt", "log_test.txt");
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

    }
}
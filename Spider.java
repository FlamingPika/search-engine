import java.io.*;
import java.net.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import Utilities.StopStem;
import org.htmlparser.Parser;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;
import org.htmlparser.beans.LinkBean;
import org.jsoup.HttpStatusException;


public class Spider {
    String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";
    private Queue<String> queue = new LinkedList<>();
    private HMap url_id;
    private HMap id_url;
    private HMap url_url;
    private HMap parent_child;
    private HMap child_parent;
    private HMap word_page;
    private HMap page_word;
    private HMap page_props;
    private int pageID = 0;
    private int processedLink = 0;
    private int limit;
    private BufferedWriter logger;
    private BufferedWriter writer;
    private BufferedWriter debugger;
    private StopStem stopStem;

    /**
     * The constructor of the spider object. Initializes all the
     * private members.
     *
     * @param  num_of_index  the maximum number of pages to be indexed
     * @param _debug_filename the location to store debugging output
     * @param _result_filename the location to store result output
     */
    Spider(int num_of_index, String _debug_filename, String _result_filename, String _log_filename) {
        limit = num_of_index;
        try {
            url_id = new HMap("url-id", "url-id");
            id_url = new HMap("id_url", "id_url");
            url_url = new HMap("url_url", "url_url");
            parent_child = new HMap("parent-child", "parent-child");
            child_parent = new HMap("child-parent", "child-parent");
            word_page = new HMap("word-page", "word-page");
            page_word = new HMap("page-word", "page-word");
            page_props = new HMap("page-props", "page-props");
            logger = new BufferedWriter(new FileWriter(_log_filename));
            writer = new BufferedWriter(new FileWriter(_result_filename));
            debugger = new BufferedWriter(new FileWriter(_debug_filename));
            stopStem = new StopStem();

        } catch (IOException ex) {
            System.out.println("failed to init inverted indexes");
        }
    }

    // url will never be the duplicated one as we only put new link in the queue
    public HttpURLConnection getResponse(URL url) throws HttpStatusException, IOException {

        if (url_id.checkEntry(url.toString()) == false){
            generateID(url.toString());
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", userAgent);
        conn.setInstanceFollowRedirects(false);

        return conn;

    }

    /**
     * a logging mechanism to help programmers in debugging the code.
     * The log will be outputted into the text file specified during
     * the initialization of the logger instance.
     *
     * @param  msg  the message to be logged for debugging purpose
     */
    public void logging(String msg) {
        try {
            logger.write(msg + "\n");
        } catch (IOException ex) {
            System.out.println("failed to log the following msg = \" " + msg + " \"");
        }
    }

    /**
     * Creates an ID for a given new URL that is not exist inside the url_id mapping index.
     *
     * @param  url  the url link to be checked and assigned with an ID
     * @return an unique ID as an integer
     */
    public int generateID(String url) {
        try {
            url_id.addEntry(url, pageID);
            id_url.addEntry(pageID, url);
            return pageID++;
        } catch (IOException ex) {
            System.out.println("failed to generate an ID");
            return -2;
        }
    }

    /**
     * Extracts a HTTP header properties, which for this case only the Title of the page,
     * Last-Modified tag, and Content-Length tag from a given url link. The extracted data
     * will be stored inside page_prop table.
     *
     * @param  conn  the connection of the url link to be extracted
     * @param parentID the id of the url
     */
    public void extractHTTPHeaderProp(HttpURLConnection conn, int parentID) {
        HashMap<String, String> header_response = new HashMap<String, String>();
        String url = "";
        try {
            url = id_url.getEntry(parentID);
            /* to get the content length */
            Long content_length = conn.getContentLengthLong();
            if (content_length != -1)
                header_response.put("Content-Length", Long.toString(content_length));

            /* to get the last modified date */
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            long last_modified = conn.getLastModified();
            if (last_modified == 0) {
                last_modified = conn.getDate();
            }
            String date = df.format(new Date(last_modified));
            header_response.put("Last-Modified", date);

            /* to get the title */
            int status_code = conn.getResponseCode();
            if (status_code >= 400) {
                header_response.put("Title", status_code + " " + conn.getResponseMessage());
            } else {
                URLConnection connection = new URL(url).openConnection();
                connection.setRequestProperty("User-Agent", userAgent);
                Parser parser = new Parser(connection);
                TitleTag titleTag = (TitleTag) parser.extractAllNodesThatMatch(node -> node instanceof TitleTag).elementAt(0);
                if (titleTag == null) {
                    header_response.put("Title", url);
                } else {
                    header_response.put("Title", titleTag.getTitle());
                }

                if (content_length == -1){
                    InputStream inputStream = conn.getInputStream();
                    StringBuilder html = new StringBuilder();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        html.append(new String(buffer, 0, bytesRead));
                    }
                    inputStream.close();
                    header_response.put("Content-Length", Integer.toString(html.length()));
                }


            }

        } catch (ParserException ex) {
            System.out.println("ParserException extractHTTPHeaderProp() for url " + url);
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            System.out.println("MalformedURLException extractHTTPHeaderProp() for url " + url);
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("IOException extractHTTPHeaderProp() for url" + url);
            ex.printStackTrace();
        }

        try {
            page_props.addProps(parentID, header_response);
        } catch (IOException ex) {
            System.out.println("Cannot add prop");
        }
    }

    /**
     * Extracts words inside the website page from a given url and
     * put it in the inverted index word_page with the corresponding
     * (page ID, frequency) and put it inside its corresponding
     * forward index page_word
     *
     * @param  url  the url link to be extracted
     * @param parentID the id of the url
     */
    public void extractWords(String url, int parentID) throws ParserException {
        try {
            StringBean sb = new StringBean();

            sb.setLinks(false);
            sb.setURL(url);

            StringTokenizer tokens = new StringTokenizer(sb.getStrings());
            if (tokens.hasMoreTokens()) {
                while (tokens.hasMoreTokens()) {
                    String wword = tokens.nextToken();
                    System.out.println("ORIGINAL WORD " + wword);
                    if (!StopStem.isAlphaNum(wword) || stopStem.isStopWord(wword)) continue;
                    String word = stopStem.stem(wword);
                    if (word == "" || word == " ") {
                        System.out.println("FOUND EMPTY STRING IN " + url + " WITH ORIGINAL WORD " + wword);
                    }
                    word_page.addFrequency(word, parentID);
                    page_word.addWords(parentID, word);
                }
            } else {
                page_word.addWords(parentID, null);
            }

        } catch (IOException ex) {
            System.out.println("failed to get words for url " + url);
        }

    }

    public boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean filter(String url) {
        try {
            URI uri = new URI(url);
            if (uri.isOpaque()) return false;
            return !uri.isAbsolute() || (uri.getScheme().equals("http") || uri.getScheme().equals("https"));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Extracts links inside the website page from a given url (if any) together with their
     * actual link by invoking findActualLink() method. For every new link: index it, and store it inside
     * the parent-child relationship database (vice versa) and add it to the queue to be crawled.
     *
     * @param  _url  the url link to be extracted
     * @param parentID the id of the url link
     */
    public void extractLinks(String _url, int parentID) throws ParserException, IOException {
        LinkBean lb = new LinkBean();
        lb.setURL(_url);
        URL[] URL_array = lb.getLinks();
        if (URL_array.length == 0) {
            logging("There is nothing to extract in " + _url);
            return;
        }
        HashMap<Integer, Integer> map_of_existing_child = new HashMap<>();
        for (int i = 0; i < URL_array.length; ++i) {
            String url = URL_array[i].toString();
            if (isValidUrl(url) == false || filter(url) == false) continue;
            url = url.split("#")[0];
            System.out.println(url);
            int childID;
            if (url_id.checkEntry(url) == false){
                childID = generateID(url);
                logging("adding the url = " + url + " with ID: " + childID);
                queue.add(url);
            } else {
                childID = url_id.getEntry(url);
                logging("the url = " + url + " exists in the db with ID = " + childID);
            }
            if (map_of_existing_child.get(childID) == null) {
                parent_child.addEntry(parentID, childID);
                child_parent.addEntry(childID, parentID);
                map_of_existing_child.put(childID, 1);
            }

        }
    }

    /**
     * [For Debugging Only]
     * Output all the insides of the databases into a specified txt file
     *
     */
    public void debug() {
        try {
            debugger.write("url inverted index: \n");
            Vector<String> res = url_id.sPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("id inverted index: \n");
            res = id_url.iPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("\n \nparent-child inverted index:");
            res = parent_child.iPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("\n \nchild-parent inverted index:");
            res = child_parent.iPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("\n \nword-page inverted index:");
            res = word_page.sPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("\n \npage-word inverted index:");
            res = page_word.iPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("\n \npage-props inverted index:");
            res = page_props.iPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }
            debugger.close();

        } catch (IOException ex) {
            System.out.println("Cannot debug");
        }
    }

    /**
     * Output the necessary result from crawling websites in the right order
     * according to the project description into a specified txt file
     *
     */
    public void result() {
        try {
            int magic_number = 10;
            for (int i = 0; i < processedLink; ++i) {
                Vector<String> res = page_props.getResult(i);
                String url = id_url.getEntry(i);
                for (int j = 0; j < res.size(); ++j) {
                    if (j == 1) {
                        writer.write(url + "\n");
                    } else {
                        writer.write(res.get(j) + "\n");
                    }
                }
                int counter = 0;
                HashMap<String,Boolean> h = page_word.getWords(i);
                if (h != null) {
                    Set<String> words = h.keySet();
                    for (String w : words) {
                        if (w == null) {
                            break;
                        }
                        if (counter >= magic_number || counter == words.size()) {
                            break;
                        }
                        HashMap<Integer, Integer> hmap = word_page.getHashTable(w);
                        writer.write(w + " " + hmap.get(i) + "; ");
                        counter++;

                    }
                }
                writer.write("\n");
                ArrayList<Integer> child = parent_child.getID(i);
                if (child != null) {
                    Iterator<Integer> numbersIterator = child.iterator();
                    counter = 0;
                    while (numbersIterator.hasNext()) {
                        if (counter >= magic_number || counter == child.size()) {
                            break;
                        }
                        writer.write(id_url.getEntry(numbersIterator.next()) + "\n");
                        counter++;
                    }

                } else {
                    writer.write("null \n");
                }

                writer.write("===============================\n");
            }
            writer.close();
        } catch (IOException ex) {
            System.out.println("Cannot print the result");
        }

    }

    /**
     * Crawl the given url and its children indexes and retrieve all the
     * necessary information, links, and words. The number of indexes are
     * limited by the limit parameter that is set in the Spider object constructor
     *
     * @param _url the root url to be crawled
     */
    public void crawl(String _url) {
        String original_url = _url;

        try {
            url_url.addEntry(original_url, _url);
            queue.add(_url);
            while (processedLink < limit) {
                if (queue.isEmpty()) {
                    break;
                }

                String url = queue.remove();
                System.out.println(processedLink + " = " + url);

                HttpURLConnection conn = getResponse(new URL(url));
                int id = url_id.getEntry(url);
                /* there's a redirection (status code = 3XX) */
                String actual_url = conn.getHeaderField("Location");
                if (actual_url != null) {
                    int actual_id;
                    if (url_id.checkEntry(actual_url) == false) {
                        actual_id = generateID(actual_url);
                        queue.add(actual_url);
                    } else {
                        actual_id = url_id.getEntry(actual_url);
                    }
                    parent_child.addEntry(id, actual_id);
                    child_parent.addEntry(actual_id, id);
                }
                if (actual_url == null) {
                    logging("Extracting links from parent u rl = " + url);
                    extractLinks(url, id);
                    extractWords(url, id);
                } else {
                    page_word.addWords(id, null);
                }

                extractHTTPHeaderProp(conn, id);
                logging("=-=-=-=-=-=-=-=-=-=-=-=");
                processedLink++;

            }

            debug();
            result();

            url_id.done();
            id_url.done();
            url_url.done();
            parent_child.done();
            child_parent.done();
            word_page.done();
            page_word.done();
            page_props.done();

        } catch (ParserException ex) {
            System.out.println("error in browsing");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
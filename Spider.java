import java.io.*;
import java.net.*;
import java.util.*;
import java.net.HttpURLConnection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.htmlparser.Parser;
import org.htmlparser.tags.TitleTag;

import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;
import org.htmlparser.http.HttpHeader;

import org.htmlparser.beans.LinkBean;

public class Spider {
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
    private boolean fast_browse;
    private BufferedWriter logger;
    private BufferedWriter writer;
    private BufferedWriter debugger;

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

        } catch (IOException ex) {
            System.out.println("failed to init inverted indexes");
        }
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
            if (url_id.checkEntry(url) == true) {
                return -1;
            } else {
                return pageID++;
            }
        } catch (IOException ex) {
            System.out.println("failed to generate an ID");
            return -2;
        }
    }

    /**
     * Recursively find an url link if the given url has a status code 3XX (redirectional)
     * and returns it as a string
     *
     * @param  url  the original url link to be checked if the link is indeed the proper link
     * @return an unique ID as an integer
     */
    public String findActualLinks(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            String responseBody = HttpHeader.getResponseHeader(conn);
            int status_code = conn.getResponseCode();
            logging("Status code " + status_code + " for url " + url);

            if (status_code < 300 || status_code >= 400) {
                logging("the url " + url + " is not changed");
                return url;
            } else {
                responseBody = responseBody.substring(responseBody.indexOf("ocation:") + 9);
                logging("the following url = " + url);
                url = responseBody.substring(0, responseBody.indexOf("\n"));
                logging(" has been changed into = " + url);
                url = findActualLinks(url);
            }

        } catch (MalformedURLException ex) {
            System.out.println("MalformedURLException in findActualLinks() for url " + url);
        } catch (IOException ex) {
            System.out.println("IOException in findActualLinks() for url " + url);
        }

        return url;
    }

    /**
     * Extracts a HTTP header properties, which for this case only the Title of the page,
     * Last-Modified tag, and Content-Length tag from a given url link. The extracted data
     * will be stored inside page_prop table.
     *
     * @param  url  the url link to be extracted
     */
    public void extractHTTPHeaderProp(String url) {
        logging("Extracting http header from url = " + url);
        HashMap<String, String> header_response = new HashMap<String, String>();
        int parentID = -1;
        try {
            parentID = url_id.getEntry(url);
            URL url_obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) url_obj.openConnection();
            conn.setRequestMethod("GET");
            header_response.put("Content-Length", Long.toString(conn.getContentLengthLong()));
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            long get_last_modified = conn.getLastModified();
            if (get_last_modified == 0) {
                get_last_modified = conn.getDate();
            }
            String date = df.format(new Date(get_last_modified));
            header_response.put("Last-Modified", date);
            int status_code = conn.getResponseCode();
            if (status_code >= 500) {
                header_response.put("Title", status_code + " " + conn.getResponseMessage());
            } else if (status_code >= 400) {
                header_response.put("Title", status_code + " " + conn.getResponseMessage());
            } else {
                Parser parser = new Parser(url_obj.openConnection());
                TitleTag titleTag = (TitleTag) parser.extractAllNodesThatMatch(node -> node instanceof TitleTag).elementAt(0);
                if (titleTag == null) {
                    header_response.put("Title", url);
                } else {
                    header_response.put("Title", titleTag.getTitle());
                }

            }

        } catch (MalformedURLException ex) {
            System.out.println("MalformedURLException extractHTTPHeaderProp() for url " + url);
        } catch (IOException ex) {
            System.out.println("IOException extractHTTPHeaderProp() for url" + url);
            ex.printStackTrace();
        } catch (ParserException ex) {
            System.out.println("ParserException extractHTTPHeaderProp() for url " + url);
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
     */
    public void extractWords(String url) throws ParserException {

        try {
            int parentID = url_id.getEntry(url);
            StringBean sb = new StringBean();

            sb.setLinks(false);
            sb.setURL(url);

            StringTokenizer tokens = new StringTokenizer(sb.getStrings());
            if (tokens.hasMoreTokens()) {
                while (tokens.hasMoreTokens()) {
                    String word = tokens.nextToken();
                    word_page.addFrequency(word, parentID);
                    page_word.addEntry(parentID, word);
                }
            } else {
                page_word.addEntry(parentID, null);
            }

        } catch (IOException ex) {
            System.out.println("failed to get words for url " + url);
        }

    }

    /**
     * Extracts links inside the website page from a given url (if any) together with their
     * actual link by invoking findActualLink() method. For every new link: index it, and store it inside
     * the parent-child relationship database (vice versa) and add it to the queue to be crawled.
     *
     * @param  url  the url link to be extracted
     */
    public void extractLinks(String url) throws ParserException {
        logging("Extracting links from parent url = " + url);
        LinkBean lb = new LinkBean();
        int parentID = generateID(url);

        try {
            if (parentID != -1) {
                url_id.addEntry(url, parentID);
                id_url.addEntry(parentID, url);
            } else {
                parentID = url_id.getEntry(url);
            }

        } catch (IOException ex) {
            System.out.println("failed in creating an ID for parentID");
        }

        lb.setURL(url);
        URL[] URL_array = lb.getLinks();
        try {
            if (URL_array.length == 0) {
                parent_child.addEntry(parentID, -1);
                logging("There is nothing to extract in " + url);

            } else {
                HashMap<Integer, Integer> map_of_existing_child = new HashMap<Integer, Integer>();

                for (int i = 0; i < URL_array.length; i++) {
                    if (fast_browse == false) {
                        if (url_url.checkEntry(URL_array[i].toString()) == true) {
                            url = url_url.getURL(URL_array[i].toString());
                        } else {
                            url = findActualLinks(URL_array[i].toString());
                            url_url.addEntry(URL_array[i].toString(), url);
                        }
                    } else {
                        url = URL_array[i].toString();
                    }

                    int childID = generateID(url);

                    if (childID == -1) {
                        logging("the url = " + url + " exists in the db with ID = " + url_id.getEntry(url));
                        childID = url_id.getEntry(url);
                    } else {
                        logging("adding the url = " + url + " with ID: " + childID);
                        queue.add(url);
                        url_id.addEntry(url, childID);
                        id_url.addEntry(childID, url);
                    }
                    if (map_of_existing_child.get(childID) == null) {
                        parent_child.addEntry(parentID, childID);
                        child_parent.addEntry(childID, parentID);
                        map_of_existing_child.put(childID, 1);
                    }

                }
            }
        } catch (IOException ex) {
            System.out.println("failed in adding an entry");
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
                String words = page_word.getEntry(i);
                if (words != null) {
                    String[] word_list = words.split("\\s+");
                    for (String w : word_list) {
                        if (counter >= magic_number || counter == word_list.length) {
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
    public void crawl(String _url, boolean _fast_browse) {
        String original_url = _url;
        fast_browse = _fast_browse;

        if (fast_browse == false)
            _url = findActualLinks(original_url);
        try {
            url_url.addEntry(original_url, _url);
            queue.add(_url);
            while (processedLink < limit) {
                if (queue.isEmpty()) {
                    break;
                }
                String url = queue.remove();
                System.out.println(processedLink + " = " + url);
                extractLinks(url);
                extractWords(url);
                extractHTTPHeaderProp(url);
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
            System.out.println("error in first step");
        }
    }

}
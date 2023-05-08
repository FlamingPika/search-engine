import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

import java.io.IOException;
import java.util.*;

public class HMap {
    private RecordManager recman;
    private HTree hashtable;

    HMap(String recordmanager, String objectname) throws IOException {
        recman = RecordManagerFactory.createRecordManager(recordmanager);
        long recid = recman.getNamedObject(objectname);

        if (recid != 0)
            hashtable = HTree.load(recman, recid);
        else {
            hashtable = HTree.createInstance(recman);
            recman.setNamedObject(objectname, hashtable.getRecid());
        }
    }

    /**
     * Committing and closing the record manager after it has record all
     * the necessary information.
     */
    public void done() throws IOException {
        recman.commit();
        recman.close();
    }

    /**
     * A function to check whether a given key is exists inside the database
     *
     * @param key a key to be checked
     * @return true if the key exist inside the database, false otherwise
     */
    public boolean checkEntry(String key) throws IOException {
        if (hashtable.get(key) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * A function to retrieve URL from a given URL if the given URL
     * points into another links due to redirection
     *
     * @param url a key to be checked
     * @return true if the key exist inside the database, false otherwise
     */
    public String getURL(String url) throws IOException {
        return (String) hashtable.get(url);
    }

    /**
     * A function to retrieve a value from a given key
     *
     * @param key a string key to be processed
     * @return the value (integer) inside the database if exists, else -1
     */
    public int getEntry(String key) throws IOException {
        if (hashtable.get(key) == null) {
            return -1;
        } else {
            return (int) hashtable.get(key);
        }
    }

    /**
     * A function to retrieve a value from a given key
     *
     * @param key an integer key to be processed
     * @return the value (string) inside the database if exists, else null
     */
    public String getEntry(int key) throws IOException {
        if (hashtable.get(key) == null) {
            return null;
        } else {
            return (String) hashtable.get(key);
        }
    }

    public HashMap<String,Integer> getWords(int key) throws IOException {
        if (hashtable.get(key) == null) {
            return null;
        } else {
            return (HashMap<String,Integer>) hashtable.get(key);
        }
    }

    /**
     * A function to retrieve a value from a given ID
     *
     * @param id an id of the url that will be processed
     * @return the value (array of integers) inside the database if exists, else null
     */
    public ArrayList<Integer> getID(int id) throws IOException {
        Object obj = hashtable.get(id);
        if (obj == null)
            return null;
        else
            return (ArrayList<Integer>) obj;

    }

    /**
     * A function to retrieve a value, which in this case a hash table, from a given word
     *
     * @param word a word of the url that will be processed
     * @return the value (hashmap int->int) inside the database if exists, else null
     */
    public HashMap<Integer, Integer> getHashTable(String word) throws IOException {
        if (hashtable.get(word) == null) {
            return null;
        } else {
            HashMap<Integer, Integer> obj = (HashMap<Integer, Integer>) hashtable.get(word);
            return obj;
        }

    }

    /**
     * A function to add a key value pair into the database where the key is a string
     * and the value is an integer. If the key exists, then the function will update
     * its value by concatenating it to the previous value
     *
     * @param key   the key to be stored
     * @param value the value to be stored
     */
    public void addEntry(String key, int value) throws IOException {
        try {
            if (hashtable.get(key) != null) {
                hashtable.put(key, hashtable.get(key) + " " + value);
            } else {
                hashtable.put(key, value);
            }
        } catch (IOException ex) {
            System.out.println("failed in adding an entry");
        }

    }

    /**
     * A function to add a key value pair into the database where the key is an integer
     * and the value is a string. If the key exists, then the function will update its
     * value by concatenating it to the previous value
     *
     * @param key   the key to be stored
     * @param value the value to be stored
     */
    public void addEntry(int key, String value) throws IOException {

        try {
            if (hashtable.get(key) != null) {
                hashtable.put(key, hashtable.get(key) + " " + value);
            } else {
                hashtable.put(key, value);
            }
        } catch (IOException ex) {
            System.out.println("failed in adding an entry");
        }

    }

    /**
     * A function to add a key value pair into the database where the key is an integer
     * and the value is a string. If the key exists, then the function will update its
     * value by concatenating it to the previous value
     *
     * @param key   the key to be stored
     * @param value the value to be stored
     */
    public void addWords(int key, String value) throws IOException {

        try {
            if (hashtable.get(key) != null) {
                HashMap<String, Integer> h = (HashMap<String, Integer>) hashtable.get(key);
                if (h.get(value) != null) {
                    h.put(value, (Integer)h.get(value) + 1);
                } else {
                    h.put(value, 1);
                }

                hashtable.put(key, h);
            } else {
                HashMap<String, Integer> h = new HashMap<String, Integer>();
                h.put(value, 1);
                hashtable.put(key, h);
            }
        } catch (IOException ex) {
            System.out.println("failed in adding an entry");
        }

    }

    /**
     * A function to add a key value pair into the database where the key is an integer
     * and the value is an integer. If the key exists, then the function will update its
     * value by adding the value into the list
     *
     * @param key   the key to be stored
     * @param value the value to be stored
     */
    public void addEntry(int key, int value) throws IOException {

        try {
            List<Integer> list;
            if (hashtable.get(key) != null) {
                list = (ArrayList<Integer>) hashtable.get(key);
                list.add(value);
            } else {
                list = new ArrayList<Integer>();
                list.add(value);
            }
            hashtable.put(key, list);
        } catch (IOException ex) {
            System.out.println("failed in adding an entry");
        }

    }

    /**
     * A function to add a key value pair into the database where the key is a string
     * and the value is a string. If the key exists, then the function will update its
     * value by concatenating it to the previous value
     *
     * @param key   the key to be stored
     * @param value the value to be stored
     */
    public void addEntry(String key, String value) throws IOException {
        try {
            if (hashtable.get(key) != null) {
                hashtable.put(key, hashtable.get(key) + " " + value);
            } else {
                hashtable.put(key, value);
            }
        } catch (IOException ex) {
            System.out.println("failed in adding an entry");
        }

    }

    /**
     * A function similar to addEntry() that adds a key value pair into the database where the key is
     * a string and the value. The function will store the value inside another hashmap as the
     * key and the frequency as the value, and put the hashmap inside the database. If the key exist,
     * it will retrieve the hashtable and update the corresponding value's frequency
     *
     * @param key   the key to be stored
     * @param value the value to be stored
     */
    public void addFrequency(String key, int value) throws IOException {

        try {
            HashMap<Integer, Integer> hmap;
            if (hashtable.get(key) != null) {
                hmap = (HashMap<Integer, Integer>) hashtable.get(key);
                if (hmap.get(value) == null) {
                    hmap.put(value, 1);
                } else {
                    hmap.put(value, hmap.get(value) + 1);
                }
            } else {
                hmap = new HashMap<Integer, Integer>();
                hmap.put(value, 1);
            }
            hashtable.put(key, hmap);
        } catch (IOException ex) {
            System.out.println("failed in adding an entry");
        }

    }

    /**
     * A function to add a key value pair into the database where the key is an integer
     * and the value is a hashmap of string->string.
     *
     * @param key   the key to be stored
     * @param value the value to be stored
     */
    public void addProps(int key, HashMap<String, String> value) throws IOException {
        try {
            if (hashtable.get(key) != null) {
                //checking date etc.
                hashtable.put(key, value);
            } else {
                hashtable.put(key, value);
            }
        } catch (IOException ex) {
            System.out.println("failed in adding an properties");
        }
    }

    /**
     * A function to print the insides of a database if the result is
     * in string type
     */
    public Vector<String> sPrintAll() throws IOException {
        // Print all the data in the hashtable
        Vector<String> printed = new Vector<String>();
        FastIterator iter = hashtable.keys();
        Object obj;
        while ((obj = iter.next()) != null) {
            printed.add(obj.toString() + " = " + hashtable.get(obj.toString()));
        }
        return printed;

    }

    /**
     * A function to print the insides of a database if the result is
     * in integer type
     */
    public Vector<String> iPrintAll() throws IOException {
        // Print all the data in the hashtable
        Vector<String> printed = new Vector<String>();
        FastIterator iter = hashtable.keys();
        Object obj;
        while ((obj = iter.next()) != null) {
            printed.add(obj.toString() + " = " + hashtable.get((int) obj));
        }
        return printed;

    }

    /**
     * A function to print the insides of a database if the result is
     * in integer type
     */
    public Vector<String> hPrintAll() throws IOException {
        // Print all the data in the hashtable
        Vector<String> printed = new Vector<String>();
        FastIterator iter = hashtable.keys();
        Object obj;
        while ((obj = iter.next()) != null) {
            HashMap<String, Integer> h = (HashMap<String, Integer>)hashtable.get((int) obj);
            Set<String> keys = h.keySet();
            StringBuilder s = new StringBuilder();
            for (String key : keys) {
                s.append(key).append(": ").append((Integer)h.get(key)).append(" - ");
            }
            printed.add(obj.toString() + " = " + s.toString());
        }
        return printed;

    }

    /**
     * A function to retrieve a result that is stored in hashmap string-> string type
     * from a given ID
     */
    public Vector<String> getResult(int id) throws IOException {
        // Print all the data in the hashtable
        Vector<String> printed = new Vector<String>();
        HashMap<String, String> res = (HashMap<String, String>) hashtable.get(id);
        printed.add(res.get("Title"));
        printed.add("URL_DUMMY");
        printed.add(res.get("Last-Modified") + ", " + res.get("Content-Length"));

        return printed;

    }

    public String getTitle(int id) throws IOException {
        // Print all the data in the hashtable
        HashMap<String, String> res = (HashMap<String, String>) hashtable.get(id);
        return res.get("Title");

    }

}

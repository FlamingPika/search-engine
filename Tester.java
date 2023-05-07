import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

public class Tester {
    private RecordManager recman;
    private HTree hashtable;
    private HMap url_id;
    private HMap id_url;
    private HMap url_url;
    private HMap parent_child;
    private HMap child_parent;
    private HMap word_page;
    private HMap page_word;
    private HMap page_props;

    Tester(String recordmanager, String objectname) throws IOException {
        recman = RecordManagerFactory.createRecordManager(recordmanager);
        long recid = recman.getNamedObject(objectname);

        if (recid != 0)
            hashtable = HTree.load(recman, recid);
        else {
            hashtable = HTree.createInstance(recman);
            recman.setNamedObject("ht1", hashtable.getRecid());
        }
    }

    public static void main(String[] args) {
        try {
            BufferedWriter debugger = new BufferedWriter(new FileWriter("test_test.txt"));

            HMap url_id = new HMap("url-id", "url-id");
            HMap id_url = new HMap("id_url", "id_url");
            HMap url_url = new HMap("url_url", "url_url");
            HMap parent_child = new HMap("parent-child", "parent-child");
            HMap child_parent = new HMap("child-parent", "child-parent");
            HMap word_page = new HMap("word-page", "word-page");
            HMap page_word = new HMap("page-word", "page-word");
            HMap page_props = new HMap("page-props", "page-props");

            debugger.write("url-id inverted index: \n");
            Vector<String> res = url_id.sPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("\n \nid-url inverted index: \n");
            res = id_url.iPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("\n \nparent-parent inverted index:\n");
            res = parent_child.iPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("\n \nchild-parent inverted index:\n");
            res = child_parent.iPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("\n \nword-page inverted index:\n");
            res = word_page.sPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("\n \npage-word inverted index:\n");
            res = page_word.hPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }

            debugger.write("\n \npage-props inverted index:\n");
            res = page_props.iPrintAll();
            for (int i = 0; i < res.size(); ++i) {
                debugger.write(res.get(i) + "\n");
            }
            debugger.close();
            url_id.done();
            id_url.done();
            url_url.done();
            parent_child.done();
            child_parent.done();
            word_page.done();
            page_word.done();
            page_props.done();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}

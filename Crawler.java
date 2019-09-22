package com.company;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Crawler {
    private String url;
    private int fileCount;
    private int maxDepth;
    private String cacheDir;

    public Crawler(String seedURL, int maxDepth, String cacheDir) {
        this.url = seedURL;
        this.fileCount = 0;
        this.maxDepth = maxDepth;
        this.cacheDir = cacheDir;
    }

    public int doCrawl() {
        Document doc = null;

        System.out.printf( "Crawling from seed URL %s to a max depth of %d\n", this.url, this.maxDepth);
        System.out.println("Crawled files will be saved in " + this.cacheDir);

        // hashset to keep track of visited URLs
        HashSet<String> visitedURLs = new HashSet<>();

        // queue structure for BFS
        Queue<Node> queue = new LinkedList<>();
        queue.add(new Node(0, this.url));
        visitedURLs.add(this.url);

        // BFS search for URLs starting from the seedURL
        while(!queue.isEmpty()) {
            Node node = queue.remove();
            System.out.println(node);

            // download the html from the URL
            try {
                doc = Jsoup.connect(node.getURL()).get();
            } catch (IOException e) {
                System.out.println("Skipping invalid URL " + node);
            }

            // save the html to a file. Files are numbered from 0 to n
            File file = new File (this.cacheDir + this.fileCount);
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new FileWriter(file));
                out.write(node.getURL() + "\n" + node.getValue() + "\n");
                if (doc != null) {
                    out.write(doc.text());
                }
                out.close();
                this.fileCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }

            // extract the child URLs and add to queue
            if (node.getValue()+1 <= this.maxDepth) {
                Elements links = null;
                if (doc != null) {
                    // get a list of URLs
                    links = doc.select("a[href]");
                }
                if (links != null) {
                    for (Element link : links) {
                        String childURL = link.attr("abs:href");
                        if (!visitedURLs.contains(childURL)) {
                            queue.add(new Node(node.getValue() + 1, childURL));
                            visitedURLs.add(childURL);
                        }
                    }
                }
            }
        }

        System.out.println("Finished crawling " + this.fileCount + " files");

        return 0;
    }
}

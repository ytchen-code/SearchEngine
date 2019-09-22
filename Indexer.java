package com.company;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Indexer {
    // This is a hashmap mapping a word to a hashmap mapping html file names to frequency of that word
    private HashMap<String, HashMap<String, Integer>> index;

    public Indexer() { }

    public HashMap<String, HashMap<String, Integer>> getIndex() {
        return this.index;
    }

    public int generateIndex(String cachePath) {
        System.out.println("Creating index from cache files in " + cachePath);
        File dir = new File(cachePath);
        File[] fileArray = dir.listFiles();
        this.index = new HashMap<>();

        if (fileArray != null) {
            // for each of the files in the cache directory (which contains the crawled html files)
            for (File cacheFile : fileArray) {
                if (cacheFile.isFile()) {
//                    System.out.println("File: " + cacheFile.getName());

                    File iFile = new File (cacheFile.toString());
                    BufferedReader in;
                    String url;
                    String depth;
                    String data;
                    // read file data
                    try {
                        in = new BufferedReader(new FileReader(iFile));
                        url = in.readLine();
                        depth = in.readLine();
                        data = in.readLine();
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (data == null) {continue;} /* some cache files have empty contents */
                    String[] words = data.strip().toLowerCase().split(" ");
                    for(int i=0; i<words.length; i++) {
                        // get rid of punctuations marks
                        words[i] = words[i].replaceAll("\\W", "");
                    }

                    // update the hashmap
                    for (String word: words) {
                        if (word.equals("")) {continue;}
                        if (!this.index.containsKey(word)) {
                            this.index.put(word, new HashMap<>());
                        }
                        // increment the word frequency
                        Integer count = this.index.get(word).getOrDefault(cacheFile.getName(), 0);
                        this.index.get(word).put(cacheFile.getName(), count + 1);
                    }
                }
            }
        }

        System.out.println("Finished creating index");
        return 0;
    }

    public void printIndex() {
        if (this.index == null) {
            return;
        }
        for (Map.Entry<String, HashMap<String, Integer>> wordEntry: this.index.entrySet()) {
            System.out.println("Word: " + wordEntry.getKey());
            for (Map.Entry<String, Integer> pageEntry: wordEntry.getValue().entrySet()) {
                System.out.println("\tpageID: "+pageEntry.getKey() + "\tcount: " + pageEntry.getValue());
            }
        }
    }

    // saves the index data structure to a file
    public int saveIndex(String indexFileOutPath) {
        if (this.index == null) {
            System.out.println("Create index before saving it to a file");
            return -1;
        }
        System.out.println("Saving index to file " + indexFileOutPath);
        File file = new File (indexFileOutPath);
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(file));

            for (Map.Entry<String, HashMap<String, Integer>> wordEntry: this.index.entrySet()) {
                out.write(wordEntry.getKey());
                for (Map.Entry<String, Integer> pageEntry: wordEntry.getValue().entrySet()) {
                    out.write(" " + pageEntry.getKey() + " " + pageEntry.getValue());
                }
                out.write("\n");
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Finished saving index to file");

        return 0;
    }

    // rebuild the index from the file saved by the saveIndex method
    public int rebuildIndex(String indexFileInPath) {
        System.out.println("Rebuilding index from file " + indexFileInPath);
        index = new HashMap<>();
        File iFile = new File (indexFileInPath);
        BufferedReader in;

        String line;
        String[] data;
        String word;
        String pageID;
        int count;

        try {
            in = new BufferedReader(new FileReader(iFile));

            while((line = in.readLine()) != null) {
                if (line.equals("\n")) {
                    continue;
                }
                data = line.split(" ");

                word = data[0];
                this.index.put(word, new HashMap<>());

                for(int i=1; i<data.length;) {
                    pageID = data[i++];
                    count = Integer.parseInt(data[i++]);
                    this.index.get(word).put(pageID, count);
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Finished rebuilding index");
        return 0;
    }

}

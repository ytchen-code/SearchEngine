package com.company;

public class Main {


    public static void main(String[] args) {

        String cacheDir = "/some/path/to/cache/dir/";
        String indexFile = "/some/path/to/index/dir/index.dat";
        String website = "https://en.wikipedia.org/wiki/Computer_science";

        // Testing Crawler
        Crawler crawler = new Crawler(website, 2, cacheDir);
        crawler.doCrawl();

        // Testing Indexer
        // Generate index from data saved by the Crawler
        Indexer indexer0 = new Indexer();
        indexer0.generateIndex(cacheDir);
        // Save the index data structure to a file
        indexer0.saveIndex(indexFile);

        // Testing Query Engine
        // Query Engine reconstructs the index saved by the indexer
        // so once the index is saved only the Query engine needs to be rerun to search for keywords
        // (no need to rerun the crawler and the indexer)
        Query query = new Query(indexFile, cacheDir);
        query.stdinSearch();


    }
}

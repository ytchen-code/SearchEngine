package com.company;

import java.io.*;
import java.util.*;

public class Query {
    private Indexer indexer;
    private String cacheDir;
    public Query(String indexPath, String cacheDir) {
        this.indexer = new Indexer();
        this.indexer.rebuildIndex(indexPath);
        this.indexer.getIndex();
        this.cacheDir = cacheDir;
    }

    private String normalizeQueryInput(String s) {
        String[] withoutAND = null;
        withoutAND = s.strip().replaceAll("AND"," ")
                .replaceAll("\\s+", " ")
                .split(" ");

        for (int i=0; i <withoutAND.length; i++) {
            withoutAND[i] = withoutAND[i].replaceAll("\\W", "");
        }

        if(1 < withoutAND.length) {
            ArrayList<String> withAND = new ArrayList<>();
            String nxt = null;
            for (int i = 0; i < withoutAND.length - 1; i++) {
                nxt = withoutAND[i + 1];
                if (withoutAND[i].equals("OR")) {
                    withAND.add(withoutAND[i]);
                } else {
                    withAND.add(withoutAND[i].toLowerCase());
                }
                if (!nxt.equals("OR") && !withoutAND[i].equals("OR")) {
                    withAND.add("AND");
                }
            }
            withAND.add(nxt.toLowerCase());
            String withANDStr = null;
            withANDStr = String.join(" ", withAND);
            return withANDStr;
        }
        else {
            return withoutAND[0];
        }
    }

    public ArrayList<String[]> parseQueryInput(String s) {
        String clean = normalizeQueryInput(s);
//        System.out.println("normalized: " + clean);

        ArrayList<String[]> query = new ArrayList<>();

        for (String str: clean.split(" AND ")) {
            query.add(str.split(" OR "));
        }

        return query;
    }

    /* returns reference */
    private HashMap<String, Integer> getPages(String word) {
        HashMap<String, HashMap<String, Integer>> index = this.indexer.getIndex();
        HashMap<String, Integer> res = null;
        if (null == index) {
            System.out.println("Please construct the index before doing a search");
            return null;
        }

        if (null == (res = index.get(word))) {
            return new HashMap<>();
        }
        else {
            return res;
        }
    }

    /* merge the union of two hashmaps to hm0 i.e. hm0 = hm0 + hm1 */
    private void orMerge(HashMap<String, Integer> hm0, HashMap<String, Integer> hm1) {
        for (Map.Entry<String, Integer> entry: hm1.entrySet()) {
            hm0.put(entry.getKey(), hm0.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
    }

    private void andMerge(HashMap<String, Integer> hm0, HashMap<String, Integer> hm1) {
        HashSet<String> set = new HashSet<>();

        Iterator<Map.Entry<String, Integer>> it = hm0.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();

            if (hm1.containsKey(entry.getKey())) {
                hm0.put(entry.getKey(), entry.getValue() + hm1.get(entry.getKey()));
            }
            else {
                it.remove();
            }
        }
    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public void printSortedList(HashMap<String, Integer> hm)
    {
        if (hm.isEmpty()) {
            System.out.println("No result");
            return;
        }
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(list);


        File dir = new File(this.cacheDir);
        File[] fileArray = dir.listFiles();

        if (fileArray != null) {
            // for each of the files in the cache directory (which contains the crawled html files)
            for (File cacheFile : fileArray) {
                if (cacheFile.isFile()) {
                    File iFile = new File(cacheFile.toString());
                    BufferedReader in;
                    String url;
                    // read file data
                    try {
                        in = new BufferedReader(new FileReader(iFile));
                        url = in.readLine();
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for(Map.Entry<String, Integer> entry: list) {

            File iFile = new File(this.cacheDir + entry.getKey());
            BufferedReader in;
            String url=null;
            // read file data
            try {
                in = new BufferedReader(new FileReader(iFile));
                url = in.readLine();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            System.out.println(url + "\t" + entry.getValue());
        }

    }

    public void printHashMap(HashMap<String, Integer> hm) {
        for (Map.Entry<String, Integer> entry: hm.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    public void search(String s) {
        ArrayList<String[]> query = parseQueryInput(s);

        ArrayList<String> res = new ArrayList<>();

        for (int i=0; i<query.size(); i++) {
            if (1 < query.get(i).length) {
                String str = query.get(i)[0];
                HashMap<String, Integer> hm = (HashMap<String, Integer>) getPages(str).clone();

                for (int j=1; j< query.get(i).length; j++) {
                    orMerge(hm, getPages(query.get(i)[j]));
                }
            }
        }

        HashMap<String, Integer> hm = null;
        for (int i=0; i<query.size(); i++) {
            if (0 == i) {
                hm = (HashMap<String, Integer>) getPages(query.get(i)[0]).clone();
                if (1 == query.get(i).length) {
                    // empty
                }
                else {
                    for (int j=1; j< query.get(i).length; j++) {
                        orMerge(hm, getPages(query.get(i)[j]));
                    }
                }
            }
            else {
                HashMap<String, Integer> hm_temp = (HashMap<String, Integer>)getPages(query.get(i)[0]).clone();
                if (1 == query.get(i).length) {
                    // empty
                }
                else {
                    for (int j=1; j< query.get(i).length; j++) {
                        orMerge(hm_temp, getPages(query.get(i)[j]));
                    }
                }
                andMerge(hm, hm_temp);
            }
        }

//        printHashMap(hm);
        printSortedList(hm);
    }

    void stdinSearch() {
        //Enter data using BufferReader
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));

        // Reading data using readLine
        String in = null;

        try {
            while (true) {
                System.out.println("Enter your query: ");
                in = reader.readLine();
                search(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

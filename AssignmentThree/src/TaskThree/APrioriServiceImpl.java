package TaskThree;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


class APrioriServiceImpl<T>  {
    /**
     * Sort all Set<T> of list
     * @param frequentItemSets a list of set
     * @return a list of sorted item
     */
    private List<List<T>> sortList(List<Set<T>> frequentItemSets) {

        List<List<T>> list = new ArrayList<List<T>>();
        Set<T> treeSet;
        for (Set<T> item : frequentItemSets) {
            treeSet = new TreeSet<T>(item);
            list.add(new ArrayList<T>(treeSet));
        }
        return list;
    }

    private double numTransactions;

    private int iteration;
    /**
     * Find frequent items at the first generated
     * @param transactions a list of transaction from database
     * @param minSupport min support
     * @return a map contains candidate and its support count
     */
    private Map<Set<T>, Integer> findFrequent1Itemsets(List<Set<T>> transactions, double minSupport) {

        Map<Set<T>, Integer> supportMap = new HashMap<Set<T>, Integer>();

        for (Set<T> transaction : transactions) {
            // Using Set collection to avoid duplicate items per transaction
            for (T item : transaction) {
                Set<T> temp = new HashSet<T>();
                temp.add(item);
                // Count support for each item
                if (supportMap.containsKey(temp)) {
                    supportMap.put(temp, supportMap.get(temp) + 1);
                } else {
                    supportMap.put(temp, 1);
                }
            }
        }
        // Remove non-frequent candidates basing on support count threshold.
        return eliminateNonFrequentCandidate(supportMap, minSupport);
    }

    /**
     * Eliminate candidates that are infrequent, leaving only those that are
     * frequent
     * @param candidates a map that contains candidates and its support count
     * @param minSupport
     * @return candidates and it support count >= minSupport
     */
    private Map<Set<T>, Integer> eliminateNonFrequentCandidate(Map<Set<T>, Integer> candidates, double minSupport) {
        Map<Set<T>, Integer> frequentCandidates = new HashMap<Set<T>, Integer>();
        for (Map.Entry<Set<T>, Integer> candidate : candidates.entrySet()) {
            if (candidate.getValue()/numTransactions >= minSupport) {
                frequentCandidates.put(candidate.getKey(), candidate.getValue());
            }
        }
        return frequentCandidates;
    }

    /**
     * Generate frequent item sets
     * @param transactionList a list of transactions from database
     * @param minSupport minimum support
     * @return candidates satisfy minimum support
     */
    Map<Set<T>, Integer> generateFrequentItemSets(List<Set<T>> transactionList, double minSupport) {

        Map<Set<T>, Integer> supportCountMap = new HashMap<Set<T>, Integer>();
        // Find all frequent 1-item sets
        Map<Set<T>, Integer> frequent1ItemMap = findFrequent1Itemsets(transactionList, minSupport);
        List<Set<T>> frequentItemList = new ArrayList<Set<T>>(frequent1ItemMap.keySet());

        Map<Integer, List<Set<T>>> map = new HashMap<Integer, List<Set<T>>>();
        map.put(1, frequentItemList);

        for (int k = 2; !map.get(k - 1).isEmpty() && iteration>0; k++) {

            // First generate the candidates.
            List<Set<T>> candidateList = aprioriGenerate(map.get(k - 1));
            // Scan D for counts
            for (Set<T> transaction : transactionList) {
                // Get the subsets of t that are candidates
                List<Set<T>> candidateList2 = subSets(candidateList, transaction);

                for (Set<T> itemset : candidateList2) {
                    // Increase support count
                    int count = supportCountMap.get(itemset) == null ? 1 : supportCountMap.get(itemset) + 1;
                    supportCountMap.put(itemset, count);
                }
            }
            // Generate next possible frequent candidates
            map.put(k, extractNextFrequentCandidates(candidateList, supportCountMap, minSupport));
            --iteration;
        }
        return getFrequentItemsets(map, supportCountMap, frequent1ItemMap);
    }
    /**
     * Generate rules with minimum confidence
     * @param frequentItemCounts candidates and its support counts
     * @param minConf minimum confidence
     */
    void ruleGeneration(Map<Set<T>, Integer> frequentItemCounts, double minConf) {
        for (Set<T> itemsets : frequentItemCounts.keySet()) {
            // Generate for frequent k-itemset >= 2
            if (itemsets.size() >= 2) {
                Map<Set<T>, Set<T>> rules = new HashMap<Set<T>, Set<T>>();
                apGenrules(itemsets, itemsets, frequentItemCounts, rules, minConf);
            }
        }
    }
    /**
     * Generate possible rules that have minimum confidence threshold
     * @param fk item set
     * @param hm item set
     * @param frequentItemCounts a map contains candidates and its support count
     * @param rules a list of rules need to be found
     * @param minConf minimum confidence
     */
    private void apGenrules(Set<T> fk, Set<T> hm, Map<Set<T>, Integer> frequentItemCounts, Map<Set<T>, Set<T>> rules,
                            double minConf) {
        Combination<T> c = new Combination<T>();
        List<T> list = new ArrayList<T>(fk);

        int k = fk.size() - 1;
        if (k > 0) {
            // Generate all nonempty subsets of fk
            Set<List<T>> subsets = c.combination(list, k);
            for (List<T> subset : subsets) {
                // For every nonempty subset s of fk, output the rule s->fk-s
                Set<T> s = new HashSet<T>(subset);
                Set<T> ls = new HashSet<T>(hm);
                ls.removeAll(subset);
                // Avoid duplicated generate rule
                if (!rules.containsKey(s)) {
                    double conf = frequentItemCounts.get(fk) / (double) frequentItemCounts.get(s);
                    // Check support of the minimum confidence threshold
                    if (conf > minConf) {
                        System.out.println(subset + "->" + ls + " confidence = " + conf);
                    }
                    // Keep tracking the existing rules generated
                    rules.put(s, ls);
                    subsets.removeAll(s);
                    // Call apGenrules recursive
                    apGenrules(s, hm, frequentItemCounts, rules, minConf);
                }
            }
        }
    }
    /**
     * Get frequent items set from the first generated and support count map
     * @param map contains iterator index and its list of items
     * @param supportCountMap  support count map
     * @param frequent1ItemMap frequent item set getting from the first generated
     * @return a map that key is set of items and value is support countmap
     */
    private Map<Set<T>, Integer> getFrequentItemsets(Map<Integer, List<Set<T>>> map, Map<Set<T>, Integer> supportCountMap, Map<Set<T>, Integer> frequent1ItemMap) {

        Map<Set<T>, Integer> temp = new HashMap<Set<T>, Integer>();
        temp.putAll(frequent1ItemMap);
        for (List<Set<T>> itemsetList : map.values()) {
            for (Set<T> itemset : itemsetList) {
                if (supportCountMap.containsKey(itemset)) {
                    temp.put(itemset, supportCountMap.get(itemset));
                }
            }
        }
        return temp;
    }
    /**
     * Extract next frequent candidates
     * @param candidateList
     * @param supportCountMap  support count map
     * @param support a minimum support
     * @return a list of unique items
     */
    private List<Set<T>> extractNextFrequentCandidates(List<Set<T>> candidateList, Map<Set<T>, Integer> supportCountMap, double support) {

        List<Set<T>> rs = new ArrayList<Set<T>>();

        for (Set<T> itemset : candidateList) {
            if (supportCountMap.containsKey(itemset)) {
                int supportCount = supportCountMap.get(itemset);
                if (supportCount/numTransactions > support) {
                    rs.add(itemset);
                }
            }
        }
        return rs;
    }
    /**
     * Get subset that contains in transaction
     * @param candidateList
     * @param transaction a set of transaction from database
     * @return List<Set<T>> a subset
     */
    private List<Set<T>> subSets(List<Set<T>> candidateList, Set<T> transaction) {
        List<Set<T>> rs = new ArrayList<Set<T>>();
        for (Set<T> candidate : candidateList) {
            List<T> temp = new ArrayList<T>(candidate);
            if (transaction.containsAll(temp)) {
                rs.add(candidate);
            }
        }
        return rs;
    }
    /**
     * Main process of apriori generated candidate
     * @param frequentItemSets a list of items
     * @return A list of item without duplicated
     */
    private List<Set<T>> aprioriGenerate(List<Set<T>> frequentItemSets) {

        List<Set<T>> candidatesGen = new ArrayList<Set<T>>();
        // Make sure that items within a transaction or itemset are sorted in
        // lexicographic order
        List<List<T>> sortedList = sortList(frequentItemSets);
        // Generate itemSet from L(k-1)
        for (int i = 0; i < sortedList.size(); ++i) {
            for (int j = i + 1; j < sortedList.size(); ++j) {
                // Check condition L(k-1) joining with itself
                if (isJoinable(sortedList.get(i), sortedList.get(j))) {
                    // join step: generate candidates
                    Set<T> candidate = tryJoinItemSets(sortedList.get(i), sortedList.get(j));
                    if (hasFrequentSubSet(candidate, frequentItemSets)) {
                        // Add this candidate to C(k)
                        candidatesGen.add(candidate);
                    }
                }
            }
        }
        return candidatesGen;
    }
    /**
     *
     * @param candidate a list of item
     * @param frequentItemSets set of frequent items
     * @return true if candidate has subset of frequent Item set, whereas false
     */
    private boolean hasFrequentSubSet(Set<T> candidate, List<Set<T>> frequentItemSets) {
        Combination<T> c = new Combination<T>();
        List<T> list = new ArrayList<T>(candidate);
        int k = candidate.size() - 1;
        boolean whatAboutIt = true;
        // Generate subset s of c candidate
        Set<List<T>> subsets = c.combination(list, k);
        for (List<T> s : subsets) {
            Set<T> temp = new HashSet<T>(s);
            if (!frequentItemSets.contains(temp)) {
                whatAboutIt = false;
                break;
            }
        }
        return whatAboutIt;
    }
    /**
     * Try to join two list of items
     * @param itemSet1 a list of items
     * @param itemSet2 a list of items
     * @return Set<T> a set of item (no duplicated)
     */
    private Set<T> tryJoinItemSets(List<T> itemSet1, List<T> itemSet2) {

        Set<T> joinItemSets = new TreeSet<T>();
        int size = itemSet1.size();
        for (int i = 0; i < size - 1; ++i) {
            joinItemSets.add(itemSet1.get(i));
        }
        joinItemSets.add(itemSet1.get(size - 1));
        joinItemSets.add(itemSet2.get(size - 1));

        return joinItemSets;

    }
    /**
     * Check condition to join two list of items
     * @param list1  a list of item
     * @param list2  a list of item
     * @return true if being able to join, otherwise false
     */
    private boolean isJoinable(List<T> list1, List<T> list2) {
        int length = list1.size();
        // Make sure that size of two lists are equal
        if (list1.size() != list2.size())
            return false;
        // Check condition list1[k-1] < list2[k-1] simply ensures that no
        // duplicates are generated
        if (list1.get(length - 1).equals(list2.get(length - 1))) {
            return false;
        }
        // Check members of list1 and list2 are joined if condition list1[k-2] =
        // list2[k-2]
        for (int k = 0; k < length - 1; k++) {
            if (!list1.get(k).equals(list2.get(k))) {
                return false;
            }
        }
        return true;
    }
    /**
     * Print out screen all frequent items
     * @param frequentItemSets a set of frequent items
     */
    private void printCandidates(Map<Set<T>, Integer> frequentItemSets) {
        for (Map.Entry<Set<T>, Integer> candidate : frequentItemSets.entrySet()) {
            System.out.println(candidate.getKey() + " " + candidate.getValue());
        }
    }
    /**
     * Read transaction data from file
     * @param datasource directory file name
     * @return List<Set<String>> a list of set transaction
     */
    List<Set<String>> readTransactions(String datasource) {
        String basket = "";
        List<Set<String>> transactions = new ArrayList<Set<String>>();
        try {
            FileReader fileReader = new FileReader(datasource);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while ((basket = bufferedReader.readLine()) != null) {
                String items[] = basket.split(" ");
                ++numTransactions;
                transactions.add(new HashSet<String>(Arrays.asList(items)));
            }
            // Always close files.
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + datasource + "'");
        } catch (IOException ex) {
            System.out.println("Error reading file '" + datasource + "'");
        }
        return transactions;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

}
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class A1_G11_t2 {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java t2 <csv_file_path> <min_support>");
            return;
        }

        String filePath = args[0];
        double minSupport = Double.parseDouble(args[1]);
        double numTransactions = 0.0;  //variable for check total number of transactions.

        List<List<String>> transactions = new ArrayList<>();
        Map<String, Integer> items = new HashMap<>();

        // long start = System.currentTimeMillis();  //for check run time

        // read .csv dataset divide with line and ","
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> transaction = new ArrayList<>();
                String[] sets = line.split(",");
                for (String item : sets) {
                    String a = item.trim();
                    transaction.add(a);
                    items.put(a, items.getOrDefault(a, 0) + 1);
                }
                numTransactions += 1.0;  // to get total number of transactions
                transactions.add(transaction);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        //get frequent Items with minSupport val
        List<String> frequentItems = new ArrayList<>();
        List<String> infrequentItems = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            double freq = entry.getValue() / numTransactions ;
            if (freq >= minSupport) {
                frequentItems.add(entry.getKey());
            }
            else {
                infrequentItems.add(entry.getKey());
            }
        }

        /* for debugging
        //print frequent Items
        System.out.print("frequent Items: ");
        for (String freI : frequentItems){
            System.out.print(freI+", ");
        }
        System.out.println();
        */

        /* for debugging
        //print infrequent Items
        System.out.print("Infrequent Items: ");
        for (String infreI : infrequentItems){
            System.out.print(infreI+", ");
        }
        System.out.println();
        */

        // delete Infrequent Items, build frequent transacions
        List<List<String>> frequentTransaction = new ArrayList<>();
        for (List<String> transac : transactions){
            for (String infrequentItem : infrequentItems) {
                transac.remove(infrequentItem);
            }
            if (!transac.isEmpty()) frequentTransaction.add(transac);
        }
        
        // sort items in frequentTransactions by support val
        for (List<String> transaction : frequentTransaction) {
            transaction.sort(Comparator.comparingDouble(items::get).reversed());
        }
        
        /*
        // Print frequent transactions
        for (List<String> transaction : frequentTransaction) {
            for (String item : transaction) {
                System.out.print(item + ",");
            }
            System.out.println(":");
        }
        */ 
        

        Map<String, TreeNode> headerTable = new HashMap<>();
        TreeNode fptree = buildFPTree(frequentTransaction, headerTable);

        //printFPTree(fptree);  //for debugging, print fptree consit of node
        //printHeaderTable(headerTable);  //for debugging, print header table, and path that each node have


        // headertable relate to support value
        Map<String, Double> headerSup = headertableSup(headerTable, numTransactions);
        
        //printHeaderSup(headerSup);  //for debugging

        Map<List<String>, Double> generateFrequentPatterns = new HashMap<>(); 

        // add FrequentPattern that consist of one element by headerSup(headertable relate to support val)
        for (Map.Entry<String, Double> entry : headerSup.entrySet()) {
            String item = entry.getKey();
            double sup = entry.getValue();
            List<String> oneElementPattern = new ArrayList<>();
            oneElementPattern.add(item);
            generateFrequentPatterns.put(oneElementPattern, sup);
        }

        // Build conditional FP_tree and get frequent patterns
        for (Map.Entry<String, TreeNode> item : headerTable.entrySet()) {
            TreeNode node = item.getValue();
            for (Map.Entry<List<String>, Double> generateEntry : generateFrequentPattern(node, numTransactions, minSupport).entrySet()){
                generateFrequentPatterns.put(generateEntry.getKey(),generateEntry.getValue());
            }
        }

        // sorting frequentpattenrs and prints
        sortAndprint(generateFrequentPatterns);
        //System.out.println("Run time : " + (System.currentTimeMillis() - start) + " ms"); //for check run time
    }

    // take final frequent patterns as a argument, and sort it by value and print
    private static void sortAndprint(Map<List<String>,Double> map){
        List<Map.Entry<List<String>, Double>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, Map.Entry.comparingByValue());
        Map<List<String>, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<List<String>, Double> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        sortedMap.forEach((key, value) -> System.out.printf(key + " : " + "%.8f\n",value)); // output format
    }

    // generateFrequent pattern method
    // reuse buildFPTree(), headertableSup() to generate Conditional fp-tree
    // in method, the 'Con' before the variable means 'conditional'
    private static Map<List<String>,Double> generateFrequentPattern (TreeNode item, double numTransactions, double minSupport){
        Map<List<String>, Double> generateFrequentPatterns = new HashMap<>();

        // frequent patterns for each items saved on frequentPatterns;
        List<List<String>> frequentPatterns = frequentPatterns(item);

        // make conditional frequentpattern-tree
        Map<String, TreeNode> ConheaderTable = new HashMap<>();
        TreeNode Confptree = buildFPTree(frequentPatterns, ConheaderTable);

        //printFPTree(Confptree); //for debugging

        Map<String, Double> ConheaderSup = headertableSup(ConheaderTable, numTransactions);
        
        //printHeaderTable(ConheaderTable); //for debugging
        //printHeaderSup(ConheaderSup); //for debugging

        List<String> frequentPattern = new ArrayList<>();
        Map<String, Double> itemSupports = new HashMap<>(); //to save frequentpattern support value
    
        for (Map.Entry<String, Double> frequentItem : ConheaderSup.entrySet()) {
            if (frequentItem.getValue() >= minSupport) {
                frequentPattern.add(frequentItem.getKey());
                itemSupports.put(frequentItem.getKey(), frequentItem.getValue());
            }
        }
        
        // when frequentPattern find(not empty frequentpattern)
        // use subsetgenerator() make possible subset with item that run on this method
        // count frequentsubset patterns support value
        // put subset and support value in generateFrequnetPatterns
        if (!frequentPattern.isEmpty()) {
            List<List<String>> frequentSubset = Subsetgenerator(frequentPattern, item.getItemName());
            for (List<String> subset : frequentSubset) {
                Double supportValue = calculateSupportValueForSubset(subset, itemSupports, numTransactions);
                generateFrequentPatterns.put(subset, supportValue);
            }
        }
    
        return generateFrequentPatterns;
    }

    // to count frequentsubset patterns support value
    private static Double calculateSupportValueForSubset(List<String> subset, Map<String, Double> itemSupports, double numTransactions) {
        double sumSupport = 0;
        for (String item : subset) {
            sumSupport += itemSupports.getOrDefault(item, 0.0);
        }
        return sumSupport;
    }

    //subset generator
    private static List<List<String>> Subsetgenerator(List<String> set, String element) {
        List<List<String>> subsets = new ArrayList<>();
        int setSize = set.size();

        //make all subset that can possible
        for (int i = 0; i < (1 << setSize); i++) {
            List<String> subset = new ArrayList<>();
            for (int j = 0; j < setSize; j++) {
                if ((i & (1 << j)) != 0) {
                    subset.add(set.get(j));
                }
            }
            // edd element on not empty subset
            if (!subset.isEmpty()) {
                subset.add(element);
                subsets.add(subset);
            }
        }

        //clean up with new arraylist
        List<String> fullSubset = new ArrayList<>(set);
        fullSubset.add(element);
        subsets.add(fullSubset);

        return subsets;
    }

    // this method need when build Conditional pattern fp-tree to find frequentpatterns
    private static List<List<String>> frequentPatterns(TreeNode node) {
        List<List<String>> frequentPatterns = new ArrayList<>();
            while (node != null) {
                for (int i = 0 ; i<node.count; i++){
                    frequentPatterns.add(findPathToRoot(node)); // add more by node.count
                }
                node = node.getNextNode();
            }
        return frequentPatterns;
    }

    //find tree-path for each node
    private static List<String> findPathToRoot(TreeNode node) {
        List<String> path = new ArrayList<>();
        TreeNode currentNode = node.getParent();
        
        while (currentNode != null && currentNode.getParent() != null) {
            path.add(currentNode.getItemName());
            currentNode = currentNode.getParent();
        }
        
        Collections.reverse(path);
        return path;
    }

    // use headertable to make new headertable relate with support value
    private static Map<String, Double> headertableSup(Map<String, TreeNode> headerTable, double numTransactions){
        Map<String, Double> headerSup = new HashMap<>();
        for (Map.Entry<String, TreeNode> entry : headerTable.entrySet()) {
            String item = entry.getKey();
            TreeNode node = entry.getValue();
            double count=0.0;
            while (node != null) {
                count += node.getCount();
                node = node.getNextNode();
            }
            count /= numTransactions;
            headerSup.put(item, count);
        }
        return headerSup;
    }

    /*  for debugging to check Headersup
    private static void printHeaderSup(Map<String, Double> headerCount){
        System.out.println("Header Sup:");
        for (Map.Entry<String, Double> entry : headerCount.entrySet()) {
            String item = entry.getKey();
            double count = entry.getValue();
            System.out.print("Item: " + item + " -> ");
            System.out.print("Sup:" + count);
            System.out.println();
        }
    }
    */

    // build fp tree with frequent transactions or build conditional fp-tree with frequnet patterns
    private static TreeNode buildFPTree(List<List<String>> frequentTransactions, Map<String, TreeNode> headerTable) {
        TreeNode root = new TreeNode(null, null, 0);
        for (List<String> transaction : frequentTransactions) {
            TreeNode currentNode = root;
            for (String item : transaction) {
                if (currentNode.hasChild(item)) {
                    currentNode = currentNode.getChild(item);
                    currentNode.incrementCount();
                } else {
                    TreeNode newNode = new TreeNode(item, currentNode, 1);
                    currentNode.addChild(newNode);
                    currentNode = newNode;

                    // Update header table
                    if (headerTable.containsKey(item)) {
                        TreeNode lastNode = headerTable.get(item);
                        while (lastNode.getNextNode() != null) {
                            lastNode = lastNode.getNextNode();
                        }
                        lastNode.setNextNode(newNode);
                    } else {
                        headerTable.put(item, newNode);
                    }
                }
            }
        }

        return root;
    }

    /* for debugging to check fptree
    private static void printFPTree(TreeNode node) {
        if (node == null) {
            return;
        }
        System.out.println("Node: " + node.getItemName() + ", Count: " + node.getCount());
        List<TreeNode> children = node.getChildren();
        if (children != null) {
            for (TreeNode child : children) {
                printFPTree(child);
            }
        }
    }
    */
    
    /*  for debugging to check headertable
    private static void printHeaderTable(Map<String, TreeNode> headerTable) {
        System.out.println("Header Table:");
        for (Map.Entry<String, TreeNode> entry : headerTable.entrySet()) {
            String item = entry.getKey();
            TreeNode node = entry.getValue();
            System.out.print("Item: " + item + " -> ");
            
            while (node != null) {
                System.out.print("Node(" + node.getItemName() + ", " + node.getCount() + ") ");
                System.out.println("Path : " + findPathToRoot(node));
                node = node.getNextNode();
            }
            System.out.println();
        }
    }
    */
    
    // TreeNode class to use fp tree
    public static class TreeNode {
        private String itemName;
        private int count;
        private TreeNode parent;
        private List<TreeNode> children;
        private TreeNode nextNode;
    
        public TreeNode(String itemName, TreeNode parent, int count) {
            this.itemName = itemName;
            this.parent = parent;
            this.count = count;
            this.children = new ArrayList<>();
            this.nextNode = null;
        }

        public String getItemName() {
            return itemName;
        }
    
        public int getCount() {
            return count;
        }
    
        public TreeNode getParent() {
            return parent;
        }
    
        public List<TreeNode> getChildren() {
            return children;
        }
    
        public TreeNode getNextNode() {
            return nextNode;
        }
        
        public void setParent(TreeNode parent) {
            this.parent = parent;
        }
    
        public void setNextNode(TreeNode nextNode) {
            this.nextNode = nextNode;
        }

        public boolean hasChild(String itemName) {
            for (TreeNode child : children) {
                if (child.getItemName().equals(itemName)) {
                    return true;
                }
            }
            return false;
        }
    
        public TreeNode getChild(String itemName) {
            for (TreeNode child : children) {
                if (child.getItemName().equals(itemName)) {
                    return child;
                }
            }
            return null;
        }
    
        public void addChild(TreeNode childNode) {
            children.add(childNode);
        }
    
        public void incrementCount() {
            this.count++;
        }
    }
}
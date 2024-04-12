
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class A1_G11_t1 {
    public static double minSupport = 2;
    public static List<String> combinationList = new ArrayList<>();
    public static HashMap<String, Integer> solve = new HashMap<>();

    public static void main(String[] args) {

        String csvFile = args[0];
        double number = Double.parseDouble(args[1]);// get the line that have path, and minsup value


        List<List<String>> itemList = readCSV(csvFile);//Get the csv file
        minSupport = itemList.size() * number;

        HashMap<String, Integer> firstItemset = Make_First(itemList);//For easy to using, traslate csv file to hashmap

        HashMap<String, Integer> currentItemset = firstItemset; 
        int depth = 1;

        while (!currentItemset.isEmpty()) {
            currentItemset = Filter(currentItemset, minSupport);//filtring if itemset's value is less than min suppport
            for (Map.Entry<String ,Integer> entry : currentItemset.entrySet()) {
                solve.put(entry.getKey(), entry.getValue());//After flitering, that Itemset is we want, so for return we put the answer to new hashmap
                
            }

            if (currentItemset.isEmpty()) break;//If current itemset is empty, stop the loop

            currentItemset = Combination(currentItemset, depth, itemList);//if current itemset is not empty, we get the itemset and make new item set using combination
            depth++;

        }

        List<Map.Entry<String, Integer>> entryList = new LinkedList<>(solve.entrySet());
        
        entryList.sort(Map.Entry.comparingByValue());//Organize the keys we stored in solve in ascending order of value

        for(Map.Entry<String, Integer> entry : entryList){
            System.out.println(entry.getKey() + " " + String.format("%.8f", (double) entry.getValue()/itemList.size()) );
        }


    }
    public static List<List<String>> readCSV(String csvFile) {//Used to get a csv file
        List<List<String>> itemList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                List<String> lineData = new ArrayList<>();
                for (String datum : data) {
                    lineData.add(datum);
                }
                itemList.add(lineData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return itemList;
    }
    public static HashMap<String, Integer> Make_First(List<List<String>> itemList) {//Preprocessed the data from the csv file to make it easier for us to use.
        HashMap<String, Integer> itemset = new HashMap<>();
        for (List<String> line : itemList) {
            for (String item : line) {
                itemset.merge(item, 1, Integer::sum);
            }
        }
        return itemset;
    }

    public static HashMap<String, Integer> Filter(HashMap<String, Integer> itemset, double minSupport) {//Removes all values that have a value less than MINSUPPORT
        itemset.entrySet().removeIf(entry -> entry.getValue() < minSupport);
        return itemset;
    }

    public static HashMap<String, Integer> Combination(HashMap<String, Integer> currentMap, int depth, List<List<String>> itemList) {//Takes the filtered hashmap, finds every single item in it, and rehashes it into a hashmap of combinations of those items
        HashSet<String> uniqueItems = new HashSet<>();
        currentMap.keySet().forEach(key -> uniqueItems.addAll(Arrays.asList(key.split(","))));//Separate comma-separated itemsets
        String[] itemsArray = uniqueItems.toArray(new String[0]);

        combinationList.clear();
        Make_Comb(itemsArray, new boolean[itemsArray.length], 0, depth + 1);

        HashMap<String, Integer> newMap = new HashMap<>();//Make new hash map using new combination
        for (String combination : combinationList) {
            int count = Counting(combination, itemList);
            if (count >= minSupport) {
                newMap.put(combination, count);
            }
        }
        return newMap;
    }

    public static void Make_Comb(String[] array, boolean[] visited, int start, int r) {//Functions that only calculate the combination itself
        if (r == 0) {
            Add_Comb(array, visited);
            return;
        }

        for (int i = start; i < array.length; i++) {//Implemented by recursively checking for one place to visit
            visited[i] = true;
            Make_Comb(array, visited, i + 1, r - 1);
            visited[i] = false;
        }
    }

    private static void Add_Comb(String[] array, boolean[] visited) {//Used to store string combinations
        StringBuilder combination = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (visited[i]) {
                if (combination.length() > 0) combination.append(",");
                combination.append(array[i]);
            }
        }
        if (combination.length() > 0) {
            combinationList.add(combination.toString());
        }
    }

    public static int Counting(String combination, List<List<String>> itemList) {//Compared to minsup when creating a new hashmap
        List<String> combinationItems = Arrays.asList(combination.split(","));
        int count = 0;
        for (List<String> items : itemList) {
            if (items.containsAll(combinationItems)) count++;
        }
        return count;
    }
}

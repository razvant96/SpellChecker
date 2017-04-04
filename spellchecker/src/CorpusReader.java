import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CorpusReader 
{
    final static String CNTFILE_LOC = "samplecnt.txt";
    final static String VOCFILE_LOC = "samplevoc.txt";
    
    private HashMap<String,Integer> ngrams;
    private Set<String> vocabulary;
    private HashMap<String, Integer> biChars;  // keeps count of occurences of two characters xy in the whole corpus
    private int corpusSize;
    private int biGramCount;
    private HashMap<String,Integer> biGram2; // count 2nd word in biGram
    private HashMap<String,Integer> biGram1; // count first word in biGram
        
    public CorpusReader() throws IOException
    {  
        readNGrams();
        readVocabulary();
    }
    
    /**
     * Returns the n-gram count of <NGram> in the corpus
     * 
     * @param nGram : space-separated list of words, e.g. "adopted by him"
     * @return count of <NGram> in corpus
     */
     public int getNGramCount(String nGram) throws  IllegalArgumentException
    {
        if(nGram == null || nGram.length() == 0)
        {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        return ngrams.getOrDefault(nGram, 0);
    }
    
    private void readNGrams() throws 
            FileNotFoundException, IOException, NumberFormatException
    {
        ngrams = new HashMap<>();
        biChars = new HashMap<>();
        biGram1 = new HashMap<>();
        biGram2 = new HashMap<>();
        corpusSize=0;
        biGramCount = 0;

        FileInputStream fis;
        fis = new FileInputStream(CNTFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));

        while (in.ready()) {
            String phrase = in.readLine().trim();
            int j = phrase.indexOf(" ");
            String s1 = phrase.substring(0, j);
            String s2 = phrase.substring(j + 1, phrase.length());
            
            try {
                int count = Integer.parseInt(s1);
                ngrams.put(s2, count);
                if (!s2.contains(" ")) { // unigram
                    corpusSize += count;
                    addBiChars(s2,count);
                } else {
                    int space = s2.indexOf(' ');
                    String w1 = s2.substring(0,space);
                    String w2 = s2.substring(space+1);
                    
                    biGram1.put(w1, biGram1.getOrDefault(w1, 0)+1);
                    biGram2.put(w2, biGram2.getOrDefault(w2, 0)+1);
                    
                    biGramCount++;
                }
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("NumberformatError: " + s1);
            }
        }
    }
    
    private void addBiChars(String word, int count) {
        word = " "+word;
        for(int i=0;i<word.length();i++) {
            if (i<word.length()-1) {
                String bichar = word.substring(i,i+2);
                biChars.put(bichar, count + biChars.getOrDefault(bichar,0));
            }
            String uniChar = word.substring(i,i+1);
            biChars.put(uniChar, count + biChars.getOrDefault(uniChar,0));
        }
    }
    
    private void readVocabulary() throws FileNotFoundException, IOException {
        vocabulary = new HashSet<>();
        
        FileInputStream fis = new FileInputStream(VOCFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));
        
        while(in.ready())
        {
            String line = in.readLine();
            vocabulary.add(line);
        }
    }
    
    /**
     * Returns the number of unique words in the corpus
     * 
     * @return the number of unique words in the corpus
     */
    public int getVocabularySize() 
    {
        return vocabulary.size();
    }
    
    /**
     * Returns the number of words in the corpus.
     * This is counted using the sum of all unigram counts.
     */
    public int getCorpusSize() 
    {
        return corpusSize;
    }
       
    /**
     * Returns a subset of words in set that are also in the vocabulary
     * 
     * @param set
     * @return intersection of set and vocabulary
     */
    public HashSet<String> inVocabulary(Set<String> set) 
    {
        HashSet<String> h = new HashSet<>(set);
        h.retainAll(vocabulary);
        return h;
    }
    
    /**
     * Returns whether or not word appears in the vocabulary.
     * @param word
     * @return 
     */
    public boolean inVocabulary(String word) 
    {
       return vocabulary.contains(word);
    }

    /**
     * returns biChars value
     * 
     * @param chars the two chars
     * @return 
     */
    public int getBiCharsValue(String chars) {
        return biChars.get(chars);
    }
    
    
    public double getKneserNaySmoothingCount(String NGram) {
        if(NGram == null || NGram.length() == 0)
        {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }

        double smoothedCount = 0.0;
        double lambda = 0.0;
        //initialize the discounts
        double distribution2 = 0.75;
        double distribution = 0.75;
        
        //change discounts for low counts
        if(getNGramCount(NGram) == 1){
            distribution = 0.5;
            distribution2 = 0.5;
        }
        if(getNGramCount(NGram) == 2){
            distribution = 0.65;
            distribution2 = 0.65;
        }

        if(!NGram.contains(" ")) { //uniGram
            lambda = getLambda("", distribution2);

            smoothedCount = ((getNGramCount(NGram) - distribution2) / getCorpusSize()) + lambda / getVocabularySize();
        }
        else { //biGram
            String s1;
            String s2;
            int j = NGram.indexOf(" ");
            
            //get the first and second word
            s1 = NGram.substring(0, j);
            s2 = NGram.substring(j+1, NGram.length());
            lambda = getLambda(s1, distribution);
            
            //compute smoothed count
            smoothedCount = (Math.max((getNGramCount(NGram) - distribution), 0.0) / getNGramCount(s1)) + lambda * continuationProb(s2);
        }

        return smoothedCount;
    }
    private double getLambda(String word, double distribution) {
        if(word.equals("")) {
            return distribution / getCorpusSize() * getVocabularySize();
        }
        else {
            double previousWordCounter = 0.0;
            for(Map.Entry<String, Integer> ngram : ngrams.entrySet()) {
                if(ngram.getKey().contains(" ")) {
                    int j = ngram.getKey().indexOf(" ");
                    String previousWord = ngram.getKey().substring(0, j);
                    if(previousWord.equals(word)) {
                        previousWordCounter += ngram.getValue();
                    }
                }
            }
            double lambda = distribution * biGram1.getOrDefault(word, 0);
            if(lambda != 0) {
                return distribution * biGram1.getOrDefault(word, 0) / previousWordCounter;
            }
            else{
                return distribution/ getNGramCount(word);
            }
        }
    }
    private double continuationProb(String word) {
        //double total = 0;
        double continuationCount;
        continuationCount = biGram2.getOrDefault(word,0);
        double x = biGramCount;
        return continuationCount / x;
    }
}

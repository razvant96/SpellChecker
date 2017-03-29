import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
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
    
    public double getSmoothedCount(String NGram)
    {
        if(NGram == null || NGram.length() == 0)
        {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        
        double smoothedCount = 0.0;
        
        if(!NGram.contains(" ")) { //unigram
            smoothedCount = ((double) (getNGramCount(NGram) + 1)) / (getCorpusSize() + getVocabularySize()) * getCorpusSize(); 
            // to get count multiply by getCorpusSize() or N = number of tokens
        }
        else { //bigram
            String s1;
            int j = NGram.indexOf(" ");

            s1 = NGram.substring(0, j);
            smoothedCount = ((double) (getNGramCount(NGram) + 1)) / (getSmoothedCount(s1) + getVocabularySize()) * getSmoothedCount(s1); 
                                                                            // to get count multiply by getNGramCount(s1)
                                                         //possibly use getSmoothedCount(s1)                               here too maybe
        }
        
        return smoothedCount;        
    }
}

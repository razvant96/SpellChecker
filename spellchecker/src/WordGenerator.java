import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author huub
 */
public class WordGenerator {
    private final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    
    private final CorpusReader cr;
    private final ConfusionMatrixReader cmr;
    
    public WordGenerator(CorpusReader cr, ConfusionMatrixReader cmr) {
        this.cr = cr; this.cmr = cmr;
    }
    
    /**
     * returns conditional probability Pr(t|c), the probability on typo t where 
     * the correct string is c. Should use confusion count in its implementation.
     * @return conditional probability Pr(t|c)
     */
    private double probability(String t, String c) {
        // ToDo SpellChecker Assignment: Implement this!
        return 1.0;
    }
    
    protected void addInsertions(HashMap<String, Double> result, String typo) {
        typo = " "+typo;
        for (int i = 0; i < typo.length()-1; i++) {
            // x in correct word written as xy in typo
            String x  = typo.substring(i,i+1);
            String xy = typo.substring(i,i+2);
            String correct = typo.substring(0, i) + x + typo.substring(i+2);
            correct = correct.substring(1); // strip additional char at beginning
            if (cr.inVocabulary(correct)) {
                collect(result,correct, probability(xy,x));
            }
        }
    }
    
    protected void addTranspositions(HashMap<String, Double> result, String typo) {
        for (int i = 0; i < typo.length()-1; i++) {
            // xy in correct word written as yx in typo
            String yx = "" + typo.charAt(i) + typo.charAt(i + 1);    // yx in typo
            String xy = "" + typo.charAt(i + 1) + typo.charAt(i);    // xy in correct word
            if (!yx.equals(xy)) {
                String correct = typo.substring(0, i) + xy + typo.substring(i + 2);
                if (cr.inVocabulary(correct)) {
//                    collect(result,correct, probability(yx,xy));
                    collect(result,correct, probability(xy,yx)); // actually should be (yx,xy) but Kernighan's confusion matrix is wrongly converted to our model
                }
            }
        }
    }
    
    protected void addDeletions(HashMap<String, Double> result, String typo) {
        typo =" "+typo; // allow deletion at the beginning;
        int length = typo.length();
        for (char deletion : ALPHABET) {
            for (int i = 0; i < length; i++) {
                // xy in correct word, x in typo
                String x = typo.substring(i, i + 1);
                String xy = x + deletion ;
                String correct = typo.substring(0, i) + xy  + typo.substring(i+1);
                correct = correct.trim();
                if (cr.inVocabulary(correct)) {
                    collect(result,correct, probability(x,xy));
                }
            }
        }
    }
    
    protected void addSubstitutions(HashMap<String, Double> result, String typo) {
        for (char substitution : ALPHABET) {
            for (int i = 0; i < typo.length(); i++) {
                // x in corrrect word, y in typo
                String y = typo.substring(i, i + 1);
                String x = "" + substitution;
                if (!y.equals(x)) {
                    String correct = typo.substring(0, i) + x + typo.substring(i + 1);
                    if (cr.inVocabulary(correct)) {
                        collect(result,correct, probability(y,x));
                    }
                }
            }
        }
    }
    
    /** returns the possible corrections for typo with their probability.
     * @param typo
     * @return hashmap with as keys the candidate corrections for typo, and as values the probability to
     *         write typo when key was intended.
     **/
    public Map<String, Double> getCandidateCorrections(String typo) {
        HashMap<String, Double> words = new HashMap<>();
        addDeletions(words,typo);
        addInsertions(words,typo);
        addTranspositions(words,typo);
        addSubstitutions(words,typo);
        return words;
    }
    
    private void collect(Map<String,Double> m, String key, Double value) {
        m.put(key, value+m.getOrDefault(key,0.0));
    }
}
import java.util.Map;
import sun.nio.cs.KOI8_R;

public class SpellCorrector {
    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;
    
    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) 
    {
        this.cr = cr;
        this.cmr = cmr;
    }
    
    public String correctPhrase(String phrase)
    {
        if(phrase == null || phrase.length() == 0)
        {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }
        phrase = "SoS " + phrase;
            
        String[] words = phrase.split(" ");
        String finalSuggestion = "";
        /** CODE TO BE ADDED **/
        
        int nrOfMistakes = 0;
        int i;
        for(i = 1; i < words.length; i++) {
            if(nrOfMistakes == 2) {
                break;
            }
            Map<String, Double> canditateWords = getCandidateWords(words[i]);
            double maxProbability = (double) Double.NEGATIVE_INFINITY;
            String correctWord = "";
            if(cr.inVocabulary(words[i])) {
                canditateWords.put(words[i], 1.0);
            }
            //System.out.println(canditateWords);
            
            for(Map.Entry<String,Double> entry : canditateWords.entrySet()){
                double prior = Math.log10(cr.getSmoothedCount(words[i-1] + " " + entry.getKey()) / cr.getSmoothedCount(words[i-1]));
                double channel = Math.log10(entry.getValue());
                double probability = channel + prior;
                System.out.println(probability + " " + entry.getKey() + " " + words[i- 1] + " " + channel + " " + prior);
                
                if(probability > maxProbability) {
                    maxProbability = probability;
                    correctWord = entry.getKey();
                }
            }
            
            if(maxProbability < Math.log10(cr.getSmoothedCount(words[i]) / cr.totalCount()) / Math.log10(2)) {
                correctWord = words[i];
            }
            
            finalSuggestion = finalSuggestion + " " + correctWord;
            if(!correctWord.equals(words[i])) {
                nrOfMistakes++;
                if(i < words.length - 1) {
                    finalSuggestion = finalSuggestion + " " + words[i + 1];
                }
                i++;
            }
        }
        for(int k = i; k < words.length; k++) {
            finalSuggestion = finalSuggestion + " " + words[k];
        }
        return finalSuggestion.trim();
    }    
      
    /** returns a map with candidate words and their noisy channel probability. **/
    public Map<String,Double> getCandidateWords(String typo)
    {
        return new WordGenerator(cr,cmr).getCandidateCorrections(typo);
    }            
}
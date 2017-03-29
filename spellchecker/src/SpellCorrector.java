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
        phrase = "SoS " + phrase + " EoS";
            
        String[] words = phrase.split(" ");
        String finalSuggestion = "";
        
        int nrOfMistakes = 0;
        int i;
        for(i = 1; i < words.length - 1; i++) {
            if(nrOfMistakes == 2) {
                break;
            }
            Map<String, Double> canditateWords = getCandidateWords(words[i]);
            double maxProbability = (double) Double.NEGATIVE_INFINITY;
            String correctWord = "";
            if(cr.inVocabulary(words[i])) {
                canditateWords.put(words[i], 0.95);
            }
            
            for(Map.Entry<String,Double> entry : canditateWords.entrySet()){
                double prior = Math.log10(cr.getSmoothedCount(words[i-1] + " " + entry.getKey()) / cr.getSmoothedCount(words[i-1]))
                                            + Math.log10(cr.getSmoothedCount(entry.getKey() + " " + words[i+1]) / cr.getSmoothedCount(entry.getKey()));
                double channel = Math.log10(entry.getValue());
                double probability = channel + prior;
                //System.out.println(probability + " " + words[i-1] + " " + entry.getKey() + " " + words[i+1] + " " + channel + " " + prior);
                
                if(probability > maxProbability) {
                    maxProbability = probability;
                    correctWord = entry.getKey();
                }
            }
            
            finalSuggestion = finalSuggestion + " " + correctWord;
            if(!correctWord.equals(words[i])) {
                nrOfMistakes++;
                if(i < words.length - 2) {
                    finalSuggestion = finalSuggestion + " " + words[i + 1];
                }
                i++;
            }
        }
        for(int k = i; k < words.length - 1; k++) {
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
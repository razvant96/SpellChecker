import java.util.Map;

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
            
        String[] words = phrase.split(" ");
        String finalSuggestion = "";
        /** CODE TO BE ADDED **/
        
        int nrOfMistakes = 0;
        int j = 1;
        
        if(!cr.inVocabulary(words[0])) {
            nrOfMistakes++;
            j++;
            Map<String, Double> canditateWords = getCandidateWords(words[0]);
            double maxProbability = (double) Double.NEGATIVE_INFINITY;
            String correctWord = "";
            double prior = Math.log10(cr.getSmoothedCount(words[0]) / cr.totalCount())
                        / Math.log10(2);
            
            for(Map.Entry<String,Double> entry : canditateWords.entrySet()){
                double channel = Math.log10(entry.getValue()) / Math.log10(2);
                double probability = channel + prior;
                //System.out.println(probability + " " + entry.getKey() + " " + channel + " " + prior);
                
                if(probability > maxProbability) {
                    maxProbability = probability;
                    correctWord = entry.getKey();
                }
            }
            
            finalSuggestion = finalSuggestion + correctWord;
        }
        else {
            finalSuggestion = finalSuggestion + words[0];
        }
        int i;
        for(i = j; i < words.length; i++) {
            if(nrOfMistakes == 2) {
                break;
            }
            Map<String, Double> canditateWords = getCandidateWords(words[i]);
            double maxProbability = (double) Double.NEGATIVE_INFINITY;
            String correctWord = "";
            double prior = Math.log10(cr.getSmoothedCount(words[i-1] + " " + words[i]) / cr.getNGramCount(words[i-1]))
                        / Math.log10(2);
            
            for(Map.Entry<String,Double> entry : canditateWords.entrySet()){
                double channel = Math.log10(entry.getValue()) / Math.log10(2);
                double probability = channel + prior;
                //System.out.println(probability + " " + entry.getKey() + " " + channel + " " + prior);
                
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
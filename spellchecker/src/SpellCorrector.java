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
        /*
         * attach Start of Sentence and End of Sentence
         * to the phrase to be corrected
         */
        phrase = "SoS " + phrase + " EoS";

        // split the phrase into words array
        String[] words = phrase.split(" ");
        //initialize final suggestion
        String finalSuggestion = "";
        
        int nrOfMistakes = 0; //the number of mistakes found so far
        int i; // initialize i here to use it later
        double lambdaBi = 0.7;
        double lambdaUni = 0.3;
        double lambda  = 0.5;

        // iterate over the actual words in the phrase
        //  without SoS and EoS
        for(i = 1; i < words.length - 1; i++) {
            //we have found 2 mistakes
            if(nrOfMistakes == 2) {
                break; // so break
            }

            // get the candidate words for this word
            Map<String, Double> canditateWords = getCandidateWords(words[i]);

            // initialize the best probability found so far
            double maxProbability = Double.NEGATIVE_INFINITY;
            // initialize the correct word
            String correctWord = "";
            /*
             * if the word is in vocabulary
             * we must consider it a likely candidate
             * so it is introduced with a channel probability of 0.95
             */
            if(cr.inVocabulary(words[i])) {
                canditateWords.put(words[i], 0.95);
            }
            // iterate over the candidate words
            for(Map.Entry<String,Double> entry : canditateWords.entrySet()){
                // get the prior probability(or language model probability) in logarithm
                // we are using biGram probabilities with the previous and next word
                double prior = Math.log10(lambdaBi * cr.getKneserNaySmoothingCount(words[i-1] + " " + entry.getKey()) + // +
                                    lambdaUni * cr.getKneserNaySmoothingCount(entry.getKey()));
                //double prior = Math.log10(cr.getKneserNaySmoothingCount(words[i-1] + " " + entry.getKey()))
                        //+ Math.log10(cr.getKneserNaySmoothingCount(entry.getKey() + " " + words[i+1]));
                // get the channel probability in logarithm
                double channel = lambda *Math.log10(entry.getValue());
                // add the two together to get the final probability for this candidate word
                double probability = channel + prior;
                

                // if we found a better probability update max probability and the correct word
                if(probability > maxProbability) {
                    maxProbability = probability;
                    correctWord = entry.getKey();
                }
            }

            //add the correct word to the final phrase
            finalSuggestion = finalSuggestion + " " + correctWord;
            /*
             * if the correct word found is not equal to the initial word
             * that means that we have found a mistake so we increment nrOfMistakes
             * and also increment i to skip past the next word since we now for sure
             * that is not a mistake and append the next word to the final suggestion
             */
            if(!correctWord.equals(words[i])) {
                nrOfMistakes++;
                if(i < words.length - 2) {
                    finalSuggestion = finalSuggestion + " " + words[i + 1];
                }
                i++;
            }
        }
        // finalize we suggestion if we found two mistakes prior to the end of sentence
        for(int k = i; k < words.length - 1; k++) {
            finalSuggestion = finalSuggestion + " " + words[k];
        }
        // return the final suggestion
        return finalSuggestion.trim();
    }    
      
    /** returns a map with candidate words and their noisy channel probability. **/
    public Map<String,Double> getCandidateWords(String typo)
    {
        return new WordGenerator(cr,cmr).getCandidateCorrections(typo);
    }            
}
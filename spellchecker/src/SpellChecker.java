import java.io.IOException;
import java.util.Scanner;

public class SpellChecker {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        boolean inPeach = true; // set this to true if you submit to peach!!
        
        try {
            CorpusReader cr = new CorpusReader();
            ConfusionMatrixReader cmr = new ConfusionMatrixReader();
            SpellCorrector sc = new SpellCorrector(cr, cmr);
            if (inPeach) {
                peachTest(sc);
            } else {
                nonPeachTest(sc);
            }
        } catch (IOException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }
    
    static void nonPeachTest(SpellCorrector sc) throws IOException { 
            String[] sentences = {
                //"at the hme locations there were traces of water",
                //"at the hoome locations there were traces of water",
                //"at the home locasions there were traces of water",
                //"this assay allowed us to measure a wide variety of conditions",
                //"this assay allowed us to measure a wide variety of conitions",
                //"this assay allowed us to meassure a wide variety of conitions",
                //"this assay allowed us to measure a wide vareity of conditions",
                //"the development of diabetes is present in moce that carry a transgen",
                //"the development of idabetes is present in mice that carry a transgen",
                "the development of diabetes us present in mice that harry a transgen",
                    "she still refers to me has a friend but i fel i am treated quite badly",
                    "boxing glowes shield the knockles not the head",
                    "boxing loves shield the knuckles nots the head",
                    "this advise is taking into consideration the fact that the govenrment bans"
            };
            
            for(String sentence: sentences) {
                System.out.println("Input : " + sentence);
                String result=sc.correctPhrase(sentence);
                System.out.println("Answer: " +result);
                System.out.println();
            }
    }
    
    static void peachTest(SpellCorrector sc) throws IOException {
            Scanner input = new Scanner(System.in);
            String sentence = input.nextLine();
            //System.out.println("Input : " + sentence);
            System.out.println("Answer: " + sc.correctPhrase(sentence)); 
            //System.out.println();
    } 
}
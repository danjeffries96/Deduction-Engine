import java.util.ArrayList;
import java.util.Scanner;

/**
* Example deduction
*/
public class DeductionMain
{
    // ≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡
    // ⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃⊃
    
    public static void main(String args[]) throws Exception
    {
        Scanner kb = new Scanner(System.in);
        
        System.out.println("Enter premises: ");
        ArrayList<Sentence> pr = new ArrayList<Sentence>();

        String premise = "";
        for (;;) {
          premise = kb.nextLine();
          if (premise.equals(""))
            break;
          pr.add(new Sentence(premise));
        }

        System.out.println("Enter conclusion: ");
        String concl = kb.nextLine();
        Sentence goal = new Sentence(concl);
        Deduction ded = new Deduction(pr, goal);

        ded.nextLine();
        
        Deduction.printLines();
    }
}

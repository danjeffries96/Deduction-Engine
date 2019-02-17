import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

public class Deduction
{
    private ArrayList<Sentence> known;
    private Stack<Sentence> goals;
    public ArrayList<Sentence> premises;
    private Sentence concl;

    public static ArrayList<Line> lines = new ArrayList<Line>();
    public static ArrayList<Line> equivLines = new ArrayList<Line>();

    
    //fields to keep track of how deduction is going,
    //i.e. if any statements are being added or
    //goal is changing
    private int lastKBSize;
    private Sentence lastGoal;

    private static int count = 0;
    private static String indent = "";


    /**
     * Constructor for Deduction class.
     * Initializes fields and pushes goal g to goal stack.
     * @throws Exception
     */
    public Deduction(ArrayList<Sentence> pr, Sentence conclusion) throws Exception
    {
        known = new ArrayList<Sentence>();
        goals = new Stack<Sentence>();
        known = pr;
        premises = (ArrayList<Sentence>) pr.clone();

        //if conclusion does not logically follow from the premises,
        //stop the program
        if (!followsFromPremises(conclusion, premises))
        {
            System.out.println(indent + conclusion + " does not follow from premises.");
            System.exit(0);
        }        
        
        if (count == 0)
        {
            for (Sentence p : premises)
            {
                addLine(indent, p, "premise");
            }
        }


        concl = conclusion;
        goals.push(conclusion);
    }
    
    /**
     * Attempts to derive a new line from known
     * sentences. First looks at knowledge base
     * and generates logically equivalent sentences.
     * If goal is found, it is popped off the goal stack.
     * Then checks if knowledge base
     * contains current goal or both its conjuncts
     * or one of its disjuncts (if it has two parts).
     * 
     * If the goal still remains
     * 
     * @throws Exception
     */
    public void nextLine() throws Exception
    {
        Sentence goal = goals.peek();       

        //stopping program after 25 iterations
        if (count > 25)
        {
            System.exit(0);
        }
        count++;

        lastKBSize = known.size();
        lastGoal = goal;

        //displaying known and goals
        displayKnown(goal);

        //updating known statements
        updateKnown();

        Sentence left = null;
        Sentence right = null;

        try
        {
            left = goal.getLeftJunct();
            right = goal.getRightJunct();
        }
        catch (Exception e)
        {
            //sentence does not have two parts
        }


        //main checking loop
        //wrapped in try/catch because last remaining goal
        //might be popped
        try
        {
            //checking for conclusion
            if (known.contains(concl))
            {
                for (Line equiv : equivLines)
                {
                    if (equiv.getSentence().equals(concl))
                    {
                        String eRule = equiv.getRule();
                        Sentence[] cited = equiv.getCited();
                        addLine(indent, concl, eRule, cited);
                    }
                }
                int s = goals.size();
                for (int i = 0; i < s; i++)
                {
                    goals.pop();
                }
                nextLine();
            }
            //if known statements contain goal
            if (known.contains(goal))
            {
                System.out.println(indent + "DONE: " + goal.toString());

                for (Line equiv : equivLines)
                {
                    if (equiv.getSentence().equals(goal))
                    {
                        String eRule = equiv.getRule();
                        Sentence[] cited = equiv.getCited();
                        
                        for (Sentence s : cited)
                        {
                            System.out.print(s + ", ");
                        }
                        System.out.println();
                        
                        addLine(indent, goal, eRule, cited);
                    }
                }
                
                goals.pop();
                nextLine();
            } 
            else if (left != null && right != null)
            {
                //conjunction
                if (goal.getMainOperator().equals("&"))
                {
                    checkConjunction(goal);
                }
                //disjunction
                else if (goal.getMainOperator().equals("v"))
                {
                    checkDisjunction(goal);
                }
            }


            //if goal is unchanged
            if (goal.equals(lastGoal))
            {
                ArrayList<Sentence> kb = (ArrayList<Sentence>) known.clone();

                //if goal has two parts, push each part to goal stack
                //if it follows from premises
                if (goal.getMainOperator() != null)
                {
                    left = goal.getLeftJunct();
                    right = goal.getRightJunct();

                    if (!known.contains(left)
                        && followsFromPremises(left, premises))
                    {
                        if (!goals.contains(left))
                        {
                            goals.push(left);
                        }
                    }
                    if (!known.contains(right)
                        && followsFromPremises(right, premises))
                    {
                        if (!goals.contains(right))
                        {
                            goals.push(right);
                        }
                    }

                }

                //interacting all statements in knowledge 
                //base with each other
                interact(kb, kb);


            }

            //if last iteration did nothing
            if (goal.equals(goals.peek())
                && known.size() == lastKBSize)
            {
                System.out.println(indent + "nothing gained\n*****\n");
                Sentence rg = relatedGoal(goal);

                //if related goal can be found
                if (rg != null
                    && !goals.contains(rg))
                {
                        goals.push(rg);
                }
                else if (relatedGoal(negate(goal)) != null
                        && !goals.contains(relatedGoal(negate(goal))))
                {
                    //looking for goals related to negation of goal
                    rg = relatedGoal(negate(goal));
                    goals.push(rg);
                }
                //if related goal of negation can't be found
                else
                {
                    //try conditional proof
                    if (goal.getMainOperator() != null
                        && goal.getMainOperator().equals("⊃")
                        && !premises.contains(goal.getLeftJunct()))
                    {
                        conditionalProof(goal);
                    }

                    //check for new goals by looking at logically
                    //equivalent sentences
                    ArrayList<Sentence> leToGoal = logicalEquiv(goal);
                    for (Sentence s : leToGoal)
                    {
                        if (followsFromPremises(s, premises))
                        {
                            if (!goals.contains(s)
                                && !known.contains(s))
                            {
                                goals.push(s);
                            }
                        }
                    }
                    
                    //if goal is still same
                    if (goal.equals(goals.peek()))
                    {
                        conjoinThings(goal);
                        conjoinThings(negate(goal));
                        conjoinThings(doubleNeg(goal));
                        if (!known.contains(add(goal, negate(goal))));
                            known.add(add(goal, negate(goal)));
                        goals.pop();
                    }

                }
            }


            nextLine();
        }

        catch (EmptyStackException e)
        {
            //last goal popped from stack
            
            //System.out.println(indent + e.toString());
        }


    }
    
    
    /**
     * Adds new line to ArrayList of lines.
     * 
     * @param indent - current indent
     * @param newline - actual sentence being added
     * @param rule - rule for adding sentence 
     * @param from - lines cited by rule
     */
    public void addLine(String indent, Sentence newSentence, String rule, Sentence... from)
    {
        Line newLine = new Line(indent, newSentence, rule, from);
        
        ArrayList<Sentence> justSentences = new ArrayList<Sentence>();
        for (Line l : lines)
        {
            justSentences.add(l.getSentence());
        }
        
        //checking for duplicates
        if (justSentences.contains(newSentence))
        {
            return;
        }
        
        //check if cited sentences need to be added first
        for (Sentence f : from)
        {

            if (!justSentences.contains(f))
            {
                for (Line equiv : equivLines)
                {
                    if (equiv.getSentence().equals(f))
                    {
                        String eRule = equiv.getRule();
                        Sentence[] cited = equiv.getCited();
                        addLine(indent, f, eRule, cited);
                    }
                }
            }
        }
        
        
        lines.add(newLine);
    }
    
    /**
     * Keeps track of lines added from logical equivs.
     * @param newEquiv
     * @param rule
     * @param from
     */
    public void addEquiv(Sentence newEquiv, String rule, Sentence... from)
    {
        Line newLine = new Line("", newEquiv, rule, from);

        for (Line l : equivLines)
        {
            if (l.getSentence().equals(newEquiv))
            {
                return;
            }
        }
        
      
        
        equivLines.add(newLine);
    }
    
   

    /**
     * Updates known statements with all the logical equivalents
     * of previously known statements. Checks for duplicates.
     * 
     * @throws Exception
     */
    public void updateKnown() throws Exception
    {
        ArrayList<Sentence> toAdd = new ArrayList<Sentence>();
        for (Sentence s : known)
        {
            ArrayList<Sentence> equivs = logicalEquiv(s);

            for (Sentence e : equivs)
            {
                if (!known.contains(e))
                {
                    toAdd.add(e);
                }
            }
        }

        ArrayList<Sentence> remove = (ArrayList<Sentence>) toAdd.clone();

        for (Sentence r : remove)
        {
            if (known.contains(r))
            {
                toAdd.remove(r);
            }
        }


        known.addAll(toAdd);
    }
    
    /**
     * Prints known statements and goals.
     * 
     * @param goal - current goal
     */
    public void displayKnown(Sentence goal)
    {
        System.out.println(indent);
        System.out.println(indent + "Goal = " + goal);
        for (Sentence s : goals)
        {
            if (!goal.equals(s))
                System.out.println(indent + "    other goal: " + s);
        }
        System.out.println(indent);


        for (Sentence s : known)
        {
            System.out.println(indent + "    known = " + s);
        }
        System.out.println(indent);
    }

    /**
     * Prints lines.
     */
    public static void printLines()
    {
        for (int i = 0; i < lines.size(); i++)
        {
            System.out.println((i+1) +": " + lines.get(i));
        }
    }
    
    /**
     * Prints equivLines.
     */
    public static void printEquivs()
    {
        for (int i = 0; i < equivLines.size(); i++)
        {
            System.out.print((i+1) +": " + equivLines.get(i));
            System.out.println("    from: " + equivLines.get(i).getCited()[0]);
        }
    }
    
    /**
     * Takes two ArrayLists of Sentences and iterates through both
     * checking if standard rules of inference can be used.
     * 
     * @param a
     * @param b
     * @throws Exception 
     */
    public void interact(ArrayList<Sentence> a, ArrayList<Sentence> b) throws Exception
    {

        for (Sentence s1 : a)
        {
            for (Sentence s2 : b)
            {
                //if sentences are the same, skip
                if (s1.toString().equals(s2.toString()))
                {
                    continue;
                }

                Sentence DS = applyDS(s1, s2);

                if (DS != null)
                {
                    if (!known.contains(DS))
                    {
                        System.out.println(indent + "NEW SENTENCE FROM DISJUNCTIVE SYLLOGISM: " + DS);
                        known.add(DS);
                        addEquiv(DS, "DS", s1, s2);
                    }
                }

                Sentence HS = applyHS(s1, s2);

                if (HS != null)
                {
                    if (!known.contains(HS))
                    {
                        System.out.println(indent + "NEW SENTENCE FROM HYPOTHETICAL SYLLOGISM: " + HS);
                        System.out.println(indent + "using " + s1 + " and " + s2);
                        known.add(HS);
                        addEquiv(HS, "HS", s1, s2);

                    }              
                }

                Sentence MP = applyMP(s1, s2);

                if (MP != null)
                {
                    if (!known.contains(MP))
                    {
                        System.out.println(indent + "NEW SENTENCE FROM MODUS PONENS: " + MP);
                        known.add(MP);
                        addEquiv(MP, "MP", s1, s2);

                    }
                }

                Sentence MT = applyMT(s1, s2);

                if (MT != null)
                {
                    if (!known.contains(MT))
                    {
                        System.out.println(indent + "NEW SENTENCE FROM MODUS TOLLENS: " + MT);
                        known.add(MT);
                        addEquiv(MT, "MT", s1, s2);

                    }
                }
            }
        }

    }
    
    /**
     * Conjoins sentences together somewhat blindly.
     * Last resort when goal can't be found or split into
     * subgoals.
     * 
     * @param goal
     * @throws Exception
     */
    public void conjoinThings(Sentence goal) throws Exception
    {
        ArrayList<Sentence> containGoal = containInPart(goal);

        for (Sentence p : containGoal)
        {
            for (Sentence q : containGoal)
            {           
                if (Sentence.countOperators(p.toString()) < 3
                    && Sentence.countOperators(q.toString()) < 3)
                {
                    if (!known.contains(conj(p, q))
                        && !known.contains(conj(q, p)))
                        known.add(conj(p, q));
                }
                
            }
        }
        
    
    }


    public static Sentence negate(Sentence s) throws Exception
    {
        if (isNegation(s))
        {
            String n = s.toString().substring(1);
            n = Sentence.trimParens(n);
            return new Sentence(n);
        }
        else
        {
            return new Sentence("~" + addParens(s));
        }

    }

    /**
     * Checks a conjunction to see if both its conjuncts are
     * contained in the knowledge base. If true, adds s
     * to knowledge base.
     * 
     * 
     * @param s - a Sentence in the form of a conjunction
     * @throws Exception 
     */
    public void checkConjunction(Sentence s) throws Exception
    {
        Sentence left = s.getLeftJunct();
        Sentence right = s.getRightJunct();
        if (known.contains(left)
            && known.contains(right))
        {
            Sentence conj = conj(left, right);
            System.out.println(indent + "conj = " + conj);
            known.add(conj);
            addLine(indent, conj, "conj", left, right);
        }

    }

    /**
     * Checks a disjunction to see either one of its disjuncts
     * is contained in the knowledge base. If true, adds s
     * to knowledge base.
     * 
     * Also checks if the sentence is in the form of 
     * "law of the excluded middle" e.g., "A v ~A"
     * 
     * 
     * @param s
     * @throws Exception
     */
    public void checkDisjunction(Sentence s) throws Exception
    {
        Sentence left = s.getLeftJunct();
        Sentence right = s.getRightJunct();

        //add
        if (known.contains(left))
        {
            Sentence add = add(left, right);
            System.out.println(indent + "add = " + add);
            known.add(add);
            addLine(indent, add, "add", left);
        }
        //add
        else if (known.contains(right))
        {
            Sentence add = add(right, left);
            System.out.println(indent + "add = " + add);
            known.add(add);
            addLine(indent, add, "add", right);
        }
        else if (excludedMiddle(s))
        {
            known.add(s);
            addLine(indent, s, "EMI");
        }    
        else if (applyCD(s))
        {
            known.add(s);
        }
    }


    /** Note: this is data-driven.
     *
     * If goal is of the form:
     * r v s
     * and r and s are the consequents in 
     * two known implications,
     * p ⊃ r
     * q ⊃ s
     * and p v q is known,
     * return r v s
     * else null
     * @throws Exception 
     * 
     */
    public boolean applyCD(Sentence goal) throws Exception
    {
        Sentence r = goal.getLeftJunct();
        Sentence s = goal.getRightJunct();

        Sentence p = null;
        Sentence q = null;
        
        Sentence pthenq = null;
        Sentence rthens = null;

        for (Sentence k : known)
        {
            if (k.getMainOperator() != null)
            {
                if (k.getMainOperator().equals("⊃"))
                {
                    Sentence cons = k.getRightJunct();
                    if (cons.equals(r))
                    {
                        p = k.getLeftJunct();
                        pthenq = k;
                    }
                    else if (cons.equals(s))
                    {
                        q = k.getLeftJunct();
                        rthens = k;
                    }
                }
            }
        }

        if (!(p == null || q == null))
        {
            Sentence pvq = add(p, q);
            if (known.contains(pvq))
            {
                System.out.println(indent + "NEW SENTENCE FROM CONSTRUCTIVE DILEMMA: " + goal);
                addLine(indent, goal, "CD", pvq, pthenq, rthens);
                return true;
            }
        }
        return false;
    }


    /** 
     * If pvq and notp are of the form:
     * p v q
     * ~p 
     * 
     * applyDS returns q. Else returns null;
     *  
     * @param goal
     * @return
     * @throws Exception
     */
    public static Sentence applyDS(Sentence pvq, Sentence notp) throws Exception
    {
        if (pvq.getMainOperator() == null)
        {
            return null;
        }
        if (pvq.getMainOperator().equals("v")
            && negate(pvq.getLeftJunct()).equals(notp))
        {
            return pvq.getRightJunct();
        }
        return null;
    }

    /**
     * If pthenq and qthenr are of the form:
     * p ⊃ q
     * q ⊃ r
     * 
     * returns p ⊃ r. Else returns null.
     * @throws Exception 
     */
    public static Sentence applyHS(Sentence pthenq, Sentence qthenr) throws Exception
    {
        if (pthenq.getMainOperator() == null || qthenr.getMainOperator() == null)
        {
            return null;
        }

        if (pthenq.getMainOperator().equals("⊃")
            && qthenr.getMainOperator().equals("⊃"))
        {
            Sentence p = pthenq.getLeftJunct();
            Sentence q = pthenq.getRightJunct();
            Sentence r = qthenr.getRightJunct();

            //would give result p ⊃ p
            if (p.equals(r))
            {
                return null;
            }
            
            if (qthenr.getLeftJunct().equals(q))
            {
                return imply(p, r);
            }
        }

        return null;
    }

    /**
     * If pthenq and p are of the form:
     * p ⊃ q
     * p
     * 
     * returns q. Else returns null
     * @param pthenq
     * @param p
     * @return q
     * @throws Exception 
     */
    public static Sentence applyMP(Sentence pthenq, Sentence p) throws Exception
    {
        if (pthenq.getMainOperator() == null || !pthenq.getMainOperator().equals("⊃"))
        {
            return null;
        }
        else
        {
            if (pthenq.getLeftJunct().equals(p))
            {
                return pthenq.getRightJunct();
            }
        }

        return null;
    }

    /**
     * if pthenq and notq are of the form:
     * p ⊃ q
     * ~q
     * returns ~p. Else returns null;
     * @throws Exception 
     */
    public static Sentence applyMT(Sentence pthenq, Sentence notq) throws Exception
    {
        if (pthenq.getMainOperator() == null || !pthenq.getMainOperator().equals("⊃"))
        {
            return null;
        }
        else
        {
            if (notq.equals(negate(pthenq.getRightJunct())))
            {
                return negate(pthenq.getLeftJunct());
            }
        }

        return null;
    }

    /**
     * Returns implication of the form "p ⊃ q"
     * @param p
     * @param q
     * @return p ⊃ q
     * @throws Exception
     */
    public static Sentence imply(Sentence p, Sentence q) throws Exception
    {
        String pString = addParens(p);
        String qString = addParens(q);


        String s = pString + " ⊃ " + qString;

        Sentence sent = new Sentence(s);
        return sent;
    }


    /**
     * Addition.
     * @param p
     * @param q
     * @return p v q
     * @throws Exception 
     */
    public static Sentence add(Sentence p, Sentence q) throws Exception
    {
        String pString = addParens(p);
        String qString = addParens(q);

        String s = pString + " v " + qString;

        Sentence sent = new Sentence(s);
        return sent;
    }

    /**
     * Conjunction.
     * @param p
     * @param q
     * @return p & q
     * @throws Exception
     */
    public static Sentence conj(Sentence p, Sentence q) throws Exception
    {
        String pString = addParens(p);
        String qString = addParens(q);

        String s = pString + " & " + qString;

        Sentence sent = new Sentence(s);
        return sent;
    }

    /**
     * Double negation.
     * if p is of the form
     * ~~s
     * returns s
     * else returns ~~p
     * @param p
     * @return ~~p
     * @throws Exception
     */
    public static Sentence doubleNeg(Sentence p) throws Exception
    {
        String pString = p.toString();

        if (pString.length() > 2
            && pString.substring(0, 2).equals("~~"))
        {
            if (pString.charAt(2) == '(')
            {
                pString = pString.substring(3, pString.length() - 1);
            }
            else
            {
                pString = pString.substring(2);
            }
        }
        else
        {
            pString = addParens(p);
            pString = "~~" + pString;
        }

        Sentence s = null;
        try
        {
            s = new Sentence(pString);
        }
        catch (Exception e)
        {
            return null;
        }

        return s;
    }

    /**
     * Returns deMorgan'd sentence if 
     * sentence p fits form
     * ~(p v q), ~(p & q),
     * ~p v ~q, or ~p & ~q
     * else returns null.
     * @param p
     * @return demorgan'd form or null
     * @throws Exception
     */
    public static Sentence deMorgans(Sentence p) throws Exception
    {
        String pString = p.toString();

        //true if of the form ~(p v q), etc
        if (pString.charAt(0) == '~' && pString.charAt(1) == '(' && p.getMainOperator() == null)
        {

            String innerString = pString.substring(2, pString.length()-1);
            Sentence inner = new Sentence(innerString);
            String mlo = inner.getMainOperator();

            Sentence leftJ = inner.getLeftJunct();
            Sentence rightJ = inner.getRightJunct();

            String leftString = addParens(leftJ).toString();
            String rightString = addParens(rightJ).toString();

            String otherOperator = mlo.equals("&") ? "v" : "&";

            String deM = "~" + leftString + " " + otherOperator + " ~" + rightString;
            Sentence s = new Sentence(deM);

            return s;
        }
        //of the form ~p v ~q
        else if (p.getMainOperator() != null
                &&  (p.getMainOperator().equals("v")
                    || p.getMainOperator().equals("&"))
                && isNegation(p.getLeftJunct())
                && isNegation(p.getRightJunct()))
        {
            //trimming tildes
            String leftJ = p.getLeftJunct().toString().substring(1);
            String rightJ = p.getRightJunct().toString().substring(1);

            String mlo = p.getMainOperator();
            String otherOperator = mlo.equals("&") ? "v" : "&";

            String deM = "~(" + leftJ + " " + otherOperator + " " + rightJ + ")";
            Sentence s = new Sentence(deM);

            return s;  
        }
        else
        {
            return null;
        }

    }
    

    /**
     * Returns true if p is of the form
     * p v ~p
     * false otherwise.
     * @param p
     * @return
     * @throws Exception
     */
    public static boolean excludedMiddle(Sentence p) throws Exception
    {
       return p.getRightJunct().equals(negate(p.getLeftJunct()))
               || p.getRightJunct().equals(new Sentence("~"+p.getLeftJunct()));
    }

    /**
     * Commutation.
     * If sentence p is of the form
     * p v q OR p & q
     * returns q v p OR q & p.
     * @param p
     * @return commutation of p
     * @throws Exception
     */
    public static Sentence commute(Sentence p) throws Exception
    {
        String leftJ = addParens(p.getLeftJunct());
        String rightJ = addParens(p.getRightJunct());
        String op = p.getMainOperator();

        Sentence s = new Sentence(rightJ + " " + op + " " + leftJ);

        return s;
    }

    /**
     * Simplification.
     * If sentence p is of the form
     * p & q
     * returns p
     * @param p
     * @return left conjuct of p
     * @throws Exception
     */
    public static Sentence simp(Sentence p) throws Exception
    {
        if (p.getMainOperator().equals("&"))
        {
            return p.getLeftJunct();
        }
        else
        {
            return null;
        }
    }


    /**
     * Adds parentheses to a statement that would need them
     * when being compounded with another sentence.
     * E.g., A & B would become (A & B) so the negation
     * ~(A & B) can be formed.
     * 
     * @param p
     * @return
     */
    public static String addParens(Sentence p)
    {
        String pString = p.toString();

        if (pString.length() > 2 && Sentence.countOperators(pString) > 0)
        {
            pString = "(" + p + ")";
        }

        return pString;
    }


    /**
     * Returns ArrayList of Sentences that contain Sentence s in part.
     * E.g., if s = "T v S", sentence "~F & (T v S)" contains s in part
     * @param s
     * @return
     * @throws Exception 
     */
    public ArrayList<Sentence> containInPart(Sentence s) throws Exception
    {
        ArrayList<Sentence> theseContain = new ArrayList<Sentence>();
        ArrayList<Sentence> equivs = logicalEquiv(s);
        ArrayList<Sentence> knownAndEquiv = (ArrayList<Sentence>) known.clone();

        knownAndEquiv.addAll(equivs);

        //iterating through known sentences
        for (Sentence p : known)
        {
            //iterating through sub-sentences of each p
            for (Sentence q : p.getStatements())
            {
                if (q.getStatements().contains(s))
                {
                    if (!theseContain.contains(p))
                    {
                        System.out.println(indent + p + " contains " + s);
                        theseContain.add(p);
                    }
                }
            }
        }
                
        return theseContain;

    }


    /**
     * Looks at left and right half of 
     * Returns a sentence that will hopefully lead to the given goal.
     * Only called when a deduction iteration yields no new
     * sentences or goals.
     * 
     * @param goal
     * @return
     * @throws Exception
     */
    public Sentence relatedGoal(Sentence goal) throws Exception
    {
        ArrayList<Sentence> containGoal = containInPart(goal);

        for (Sentence s : containGoal)
        {
            if (s.getMainOperator() != null)
            {
                Sentence left = s.getLeftJunct();
                Sentence right = s.getRightJunct();
                Sentence negLeft = negate(left);
                Sentence negRight = negate(right);
                ArrayList<Sentence> checkThese = new ArrayList<Sentence>();
                checkThese.add(left);
                checkThese.add(right);
                checkThese.add(negLeft);
                checkThese.add(negRight);

                //checking left and right components
                //of sentence s as well as their negations
                for (Sentence p : checkThese)
                {
                    if (followsFromPremises(p, premises)
                        && !p.equals(goal)
                        && !known.contains(p))
                    {
                        return p;
                    }
                }

            }
        }

        return null;
    }
    /**
     * Applies implication as rule of replacement.
     * If s is of the form 
     * p ⊃ q
     * returns ~p v q
     * and vice versa
     * 
     * @param s
     * @return
     * @throws Exception 
     */
    public static Sentence implication(Sentence s) throws Exception
    {
        if (s.getMainOperator() == null)
        {
            return null;
        }
        if (s.getMainOperator().equals("⊃"))
        {
            return add(negate(s.getLeftJunct()), s.getRightJunct());
        }
        else if (s.getMainOperator().equals("v"))
        {
            return imply(negate(s.getLeftJunct()), s.getRightJunct());
        }
        
        return null;
    }
    /**
     * Converts biconditional to equivalent statements.
     * If s is of the form
     * p ≡ q,
     * 
     * returns (p ⊃ q) & (q ⊃ p)
     * and vice versa
     * 
     * if s is of the form
     * (p & q) v (~p & ~q),
     * 
     * returns p ≡ q
     * 
     * else returns null
     */
    public static Sentence equivalence(Sentence s) throws Exception
    {

        if (s.getMainOperator().equals("≡"))
        {
            Sentence p = s.getLeftJunct();
            Sentence q = s.getRightJunct();    
            return conj(imply(p, q), imply(q, p));
        }
        else if (s.getMainOperator().equals("&")
            || s.getMainOperator().equals("v"))
        {
            Sentence left = s.getLeftJunct();
            Sentence right = s.getRightJunct();

            if (left.getMainOperator() != null
                && right.getMainOperator() != null)
            {
                Sentence leftLeft = left.getLeftJunct();
                Sentence leftRight = left.getRightJunct();
                Sentence rightLeft = right.getLeftJunct();
                Sentence rightRight = right.getRightJunct();

                //form of (p ⊃ q) & (q ⊃ p)
                if (s.getMainOperator().equals("&"))
                {
                    if (left.getMainOperator().equals("⊃")
                        && right.getMainOperator().equals("⊃"))
                    {
                        if (leftLeft.equals(rightRight)
                            && rightLeft.equals(leftRight))
                        {
                            return new Sentence(addParens(leftLeft) 
                                + " ≡ " + addParens(leftRight));
                        }

                    }
                }
                //form of (p & q) v (~p & ~q)
                else if (s.getMainOperator().equals("v"))
                {
                    if (left.getMainOperator().equals("&")
                        && right.getMainOperator().equals("&"))
                    {
                        if (leftLeft.equals(negate(rightLeft))
                            && leftRight.equals(negate(rightRight)))
                        {
                            return new Sentence(addParens(leftLeft) 
                                + " ≡ " + addParens(rightRight));
                        }
                    }
                }
            }
        }


        return null;
    }

    /**
     * Applies distribution as rule of replacement.
     * If s is of the form:
     * (p v q) & (p v r)
     * 
     * returns p v (q & r)
     * and vice versa
     * 
     * else returns null
     * 
     * @param s
     * @return
     * @throws Exception
     */
    public static Sentence distr(Sentence s) throws Exception
    {
        if (s.getMainOperator() == null
            || !(s.getMainOperator().equals("v"))
                    || s.getMainOperator().equals("&"))
        {
            return null;
        }
        
        String operator = s.getMainOperator();
        String otherOperator = operator.equals("&") ? "v" : "&";
        
        Sentence left = s.getLeftJunct();
        Sentence right = s.getRightJunct();
        
        //of the form (p v q) & (p v r)
        if (left.getMainOperator() != null
            && right.getMainOperator() != null)
        {
            if (left.getMainOperator().equals(otherOperator)
                && right.getMainOperator().equals(otherOperator))
            {
                Sentence p = left.getLeftJunct();
                if (p.equals(right.getLeftJunct()))
                {
                    //major operator becomes minor
                    Sentence newRight = new Sentence(
                        addParens(left.getRightJunct()) + " " + operator
                            + " " + addParens(right.getRightJunct()));
                    
                    //minor operator becomes major
                    String distr = addParens(p) + " " + otherOperator
                        + " " + addParens(newRight);
                    return new Sentence(distr);
                }
            }
        }
        //of the form p v (q & r)
        else if (right.getMainOperator() != null)
        {
            if (right.getMainOperator().equals(otherOperator))
            {
                String newLeft = addParens(left) + " " + operator
                               + " " + addParens(right.getLeftJunct());
                String newRight = addParens(left) + " " + operator
                               + " " + addParens(right.getRightJunct());
                Sentence l = new Sentence(newLeft);
                Sentence r = new Sentence(newRight);

                String full = addParens(l) + " " + otherOperator
                               + " " + addParens(r);
                return new Sentence(full);
            }
        }
        
        return null;
        
    }

    
    /**
     * Returns ArrayList of Sentences that are logically
     * equivalent to Sentence s
     * @param s - sentence of propositional logic
     * @return sentences equivalent to s
     * @throws Exception
     */
    public ArrayList<Sentence> logicalEquiv(Sentence s) throws Exception
    {
        ArrayList<Sentence> equivs = new ArrayList<Sentence>();
        String string = s.toString();

        if (string.length() == 1)
        {
            equivs.add(doubleNeg(s));
            addEquiv(doubleNeg(s), "DN", s);
        }

        if (isNegation(s)
            && string.substring(0, 2).equals("~~"))
        {
            Sentence dn = doubleNeg(s);
            if (dn != null
                && !dn.equals(s))
            {
                addEquiv(dn, "DN", s);

                equivs.add(dn);
            }
        }

        //form for deMorgans
        if (deMorgans(s) != null)
        {
            addEquiv(deMorgans(s), "DeM", s);
            equivs.add(deMorgans(s));

        }

        //form for commutation
        if (s.getMainOperator() != null)
        {
            if (s.getMainOperator().equals("&")
                ||s.getMainOperator().equals("v"))
            {
                addEquiv(commute(s), "comm", s);
                equivs.add(commute(s));
                if (equivalence(s) != null)
                {
                    addEquiv(equivalence(s),"equiv", s);
                    equivs.add(equivalence(s));
                }

            }

            //form for simplification
            if (s.getMainOperator().equals("&"))
            {
                addEquiv(simp(s), "simp", s);
                equivs.add(simp(s));
            }

            //form for equivalence
            if (s.getMainOperator().equals("≡"))
            {
                addEquiv(equivalence(s), "equiv", s);
                equivs.add(equivalence(s));
            }

            //replacing left and right half by double negation
            if (convertDN(s) != null
                && !convertDN(s).equals(s))
            {
                addEquiv(convertDN(s), "DN", s);
                equivs.add(convertDN(s));
            }
            
            //form for distribution
            if (distr(s) != null)
            {
                addEquiv(distr(s), "distr", s);
                equivs.add(distr(s));
            }
            
            //form for implication
            if (implication(s) != null)
            {
                addEquiv(implication(s), "impl", s);
                equivs.add(implication(s));
            }
        }

        return equivs;
    }


    /**
     * Converts both or either half of a sentence
     * to it's double-negated form.
     * 
     * @param s
     * @return
     * @throws Exception
     */
    public static Sentence convertDN(Sentence s) throws Exception
    {
        Sentence left = s.getLeftJunct();
        Sentence right = s.getRightJunct();
        String operator = s.getMainOperator();

        if (isNegation(left)
            && left.toString().substring(0, 2).equals("~~"))
        {
            Sentence dn = doubleNeg(right);
            if (dn != null)
            {
                left = dn;
            }
        }
        if (isNegation(right)
            && right.toString().substring(0, 2).equals("~~"))
        {
            Sentence dn = doubleNeg(right);
            if (dn != null)
            {
                right = dn;
            }
        }

        return new Sentence(addParens(left) + " " + operator + " " + addParens(right));


    }
    public static boolean isNegation(Sentence p)
    {
        if (p.getMainOperator() == null && p.toString().charAt(0) == '~')
        {
            return true;
        }
        else 
        {
            return false;
        }
    }

    /**
     * Checking if goal Sentence follows from premises
     * using truth table verification.
     * 
     * @param goal
     * @return
     * @throws Exception
     */
    public static boolean followsFromPremises(Sentence goal, ArrayList<Sentence> pr) throws Exception
    {      
        Sentence full = null;
        if (pr.size() == 0)
        {
            full = goal;
        }
        else
        {
            Sentence conjOfPremises = pr.get(0);

            for (int i = 1; i < pr.size(); i++)
            {
                conjOfPremises = conj(conjOfPremises, pr.get(i));
            }

            full = imply(conjOfPremises, goal);

        }
        Table t = new Table(full);
        for (Boolean[] row : t.getCells())
        {
            if (!row[row.length-1].booleanValue())
                return false;
        }

        return true;
    }

    /**
     * Attempts to do a conditional proof.
     * Assumes premise p and attempts to deduce q.
     * Once complete adds p ⊃ q to knowledge base.
     * 
     * @param pthenq - goal conditional
     * @throws Exception
     */
    public void conditionalProof(Sentence pthenq) throws Exception
    {
        Sentence assumption = pthenq.getLeftJunct();
        Sentence goal = pthenq.getRightJunct();
        ArrayList<Sentence> pr = (ArrayList<Sentence>) known.clone();
        pr.add(assumption);

        if (followsFromPremises(goal, pr))
        {
            String lastIndent = indent;
            indent += "     ";
            addLine(indent, assumption, "assumption");
          
            Deduction cp = new Deduction(pr, goal);
            System.out.println(indent + "ASSUMPTION = " + assumption);
            cp.nextLine();

            Sentence cproven = imply(assumption, goal);
            known.add(cproven);
            indent = lastIndent;

            addLine(indent, cproven, "CP");
        }
        else
        {
            System.out.println(indent + "tried cp but " + goal + " does not follow from " + assumption);
        }

    }
    
    
    /**
     * Inner class for line.
     * Used to keep track of deduction and
     * which lines are cited for other lines.
     * 
     * @author Daniel Jeffries
     *
     */
    private static class Line
    {
        private Sentence sent;
        private String rule;
        private Sentence[] cited;
        private String indent;
        
        /**
         * Initializes line object with a sentence,
         * a rule for its derivation, and a list of
         * sentences which allow for its derivation.
         * 
         * @param indent - current indent
         * @param s - sentence
         * @param r - rule of inference/replacement
         * @param from - cited lines
         */
        public Line(String indent, Sentence s, String r, Sentence... from)
        {
            sent = s;
            rule = r;
            this.indent  = indent;
            cited = from.clone();
        }
        
        public Sentence getSentence()
        {
            return sent;
        }
        
        public String getRule()
        {
            return rule;
        }
        
        public Sentence[] getCited()
        {
           return cited;
        }
        
        
        @Override
        public String toString()
        {
            String toString = this.indent + sent.toString() + ";  ";
            
            for (int i = 0; i < cited.length; i++)
            {
                int lineNum = 0;

                //iterate through known lines to find
                //cited line number
                for (Line l : lines)
                {
                    if (l.getSentence().equals(cited[i]))
                    {
                        lineNum = lines.indexOf(l) + 1;
                    }
                }
                toString += lineNum;
                
                if (i < cited.length - 1)
                {
                    toString += ", ";
                }
            }
            if (cited.length > 0)
            {
                toString += "; ";
            }
            
            toString += rule;
            
            return toString;
            
        }
    }
}

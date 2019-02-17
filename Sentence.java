import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Sentence
{
    
    private String sentence;
    public LinkedHashMap<String, CompoundBool> statements;
    public ArrayList<String> letters;
    private static String indent = "";
    
    public Sentence(String s) throws Exception
    {
        this.sentence = s;
        statements = new LinkedHashMap<String, CompoundBool>();
        letters = new ArrayList<String>();
        countLetters();
        
        if (countOperators(s) > 1
            && countOperators(s) - 1 > countParens(s) / 2)
        {
            System.out.println("Sentence is not well formed");
            System.out.println();
            throw new Exception();
        }
        
        parseSentence(s);
    }
    
    /**
     * Returns an ArrayList filled with the keys of
     * LinkedHashMap statements with letters and
     * their negations added first.
     * 
     * @return
     */
    public ArrayList<String> getKeys()
    {
        ArrayList<String> keys = new ArrayList<String>();
        
        for (String key : statements.keySet())
        {
            if (key.length() <= 2)
            {
                keys.add(key);
            }
        }
        for (String key : statements.keySet())
        {
            if (!keys.contains(key))
            {
                keys.add(key);
            }
        }
        return keys;
    }
    
    
    /**
     * Returns ArrayList of sub-sentences
     * @return ArrayList of sentences
     * @throws Exception
     */
    public ArrayList<Sentence> getStatements() throws Exception
    {
        ArrayList<Sentence> sentences = new ArrayList<Sentence>();
        
        ArrayList<String> keys = getKeys();
        
        for (String key : keys)
        {
            sentences.add(new Sentence(key));
        }
        
        return sentences;

    }
    
    /**
     * Increments indent by amount i
     * @param i - amount of increment
     */
    private static void indent(int i)
    {
        if (i < 0 && indent.length() + i >= 0)
        {
            indent = indent.substring(0, indent.length() + i);
        }
        else
        {
            for (int j = 0; j < i; j++)
            {
                indent += " ";
            }
        }
    }
    
    
    /**
     * Checks to see if a character is a logical operator
     * @param c - char being checked
     * @return whether char is operator
     */
    private static boolean isOperator(char c)
    {
        return c == '&'
            || c == 'v'
            || c == 'V'
            || c == '\u2283'
            || c == '\u2261';
    }
    
    /**
     * Counts the number of distinct letters in a sentence
     * and places them in a HashMap<String, Boolean> with
     * an initial truth value of false. Also places them in letters
     * ArrayList.
     * 
     * @return the number of distinct letters
     * 
     */
    public int countLetters()
    {
        int count = 0;
        
        for (int i = 0; i < sentence.length(); i++)
        {
          String c = sentence.substring(i, i + 1);
          int ascii = (int) sentence.charAt(i);
          if (!letters.contains(c)
              &&
                  ((ascii >= 65 && ascii <= 90)
                      ||(ascii >= 97 & ascii <= 122))
                  && ascii != 118)
          {
              letters.add(c);
              statements.put(c, new CompoundBool(false));
              count++;
          }
          
        }
        
      
        return count;
    }
    
    /**
     * Counts the number of logical operators in a sentence
     * 
     * @param s - sentence of formal logic in String form
     * @return the number of logical operators in s
     */
    public static int countOperators(String s)
    {
        int count = 0;
        
        for (int i = 0; i < s.length(); i++)
        {
            if (isOperator(s.charAt(i)))
            {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Trims parentheses from a string
     * @param s - sentence of formal logic in String form
     * @return the trimmed string
     */
    public static String trimParens(String s)
    {
        if (s.charAt(0) == '(')
        {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }
    
    /**
     * Checks to see if sentence is contained by two parentheses
     * @param s - sentence of formal logic in String form
     * @return true if only parentheses are at boundaries
     */
    private static boolean containedByParens(String s)
    {
        return s.charAt(0) == '('
            && s.charAt(s.length() - 1) == ')';
    }
    
    
    /**
     * Counts the number of parentheses in a string
     * @param s - sentence of formal logic in String form
     * @return the number of parentheses in s
     */
    private static int countParens(String s)
    {
        int count = 0;
        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) == '(' || s.charAt(i) == ')')
                count++;
        }
        
        return count;
    }
    
    /**
     * Finds the main logical operator in a sentence
     * 
     * @param s - a sentence of logic in String form
     * 
     * @return the index of the main logical operator or -1 if not found
     */
    public static int findMainOperator(String s)
    {
        int index;
        
        for (index = 0; index < s.length(); index++)
        {
            if (isOperator(s.charAt(index)) 
                && 
                (openParenCount(s, index) == 0
                    || 
                     (containedByParens(s))
                         && countParens(s) == 2))
            {
                return index;
            }
        }
        
        //nothing found
        return -1;
    }
    
    /**
     * Getter method for mlo in string form
     * @return the main logical operator or null if not found
     */
    public String getMainOperator()
    {
        int index;
        
        for (index = 0; index < sentence.length(); index++)
        {
            if (isOperator(sentence.charAt(index)) 
                && 
                (openParenCount(sentence, index) == 0
                    || 
                     (containedByParens(sentence))
                     && countParens(sentence) == 2))
            {
                return sentence.substring(index, index+1);
            }
        }
        
        return null;
    }
    
    /**
     * Getter method for left half of statement
     * @return left half of this sentence
     * @throws Exception 
     */
    public Sentence getLeftJunct() throws Exception
    {
        int mloIndex = findMainOperator(sentence);
        
        return new Sentence(trimParens(sentence.substring(0, mloIndex).trim()));
    }
    
    /**
     * Getter method for right half of statement
     * @param s - a sentence of formal logic in String form
     * @return right half of sentence s
     * @throws Exception 
     */
    public Sentence getRightJunct() throws Exception
    {
        int mloIndex = findMainOperator(sentence);
        
        return new Sentence(trimParens(sentence.substring(mloIndex + 1).trim()));
    }

    
    /**
     * Returns the number of "open parentheses" at the given index,
     * meaning the number of left parentheses minus the number of right
     * parentheses.
     * 
     * @param s - a sentence of logic in String form
     * @param index - the index up to which is being checked
     * 
     * @return the number of open parentheses preceding the index
     */
    private static int openParenCount(String s, int index)
    {
       int count = 0;
       
       for (int i = 0; i < index; i++)
       {
           if (s.charAt(i) == '(')
           {
               count++;
           }
           else if (s.charAt(i) == ')')
           {
               count--;
           }
       }
       
       return count;
        
    }
    
    /**
     * Adds a logical sentence to the statements LinkedHashMap
     * 
     * @param left - left half of the sentence
     * @param operator - logical operator
     * @param right - right half of the sentence
     */
    private void addStatement(String left, String operator, String right)
    {
        CompoundBool leftbool = new CompoundBool(false);
        CompoundBool rightbool = new CompoundBool(false);
        
        try
        {
            leftbool = statements.get(left);
        }
        catch (NullPointerException e)
        {
            //system.out.println("NullPointerException: cannot find " + left);
            e.printStackTrace();
            System.exit(0);
        }
        try
        {
            rightbool = statements.get(right);
        }
        catch (NullPointerException e)
        {
            //system.out.println("NullPointerException: cannot find " + right);
            e.printStackTrace();
            System.exit(0);
        }

        
        //true unless simple sentence or negation
        if (findMainOperator(left) != -1)
        {
            left = "(" + left + ")";
        }
        
        //true unless simple sentence or negation
        if (findMainOperator(right) != -1)
        {
            right = "(" + right + ")";
        }
        
        String key = left + " " + operator + " " + right;
        //system.out.println(indent + "adding " + key);
        
        
        statements.put(key, new CompoundBool(operator, leftbool, rightbool));
        
        indent(-8);
        //system.out.println();
    }
    
    
    /**
     * Adds a negation to the statements LinkedHashMap
     * @param s - sentence of formal logic in String form
     */
    private void addNegation(String s)
    {
        //trim '~' from front of s
        String substring = s.substring(1);
        //trim parentheses
        substring = trimParens(substring);
        
        CompoundBool subbool = statements.get(substring);
        
        //system.out.println(indent + "adding negation " + s);
        
        statements.put(s, new CompoundBool("~", subbool));
        indent(-8);
        //system.out.println();
    }
    
    

    /**
     * Checks if String s is contained in LinkedHashMap and if not adds it
     * 
     * @param s - sentence of formal logic in String form
     */
    private void checkStatements(String s)
    {
        if (!statements.containsKey(s))
        {
            indent(8);

            //system.out.println();
            //system.out.println(indent + "\"" + s + "\" not found in statements");
            parseSentence(s);
        }
        else
        {
            //system.out.println(indent + "\"" + s + "\"  found");
        }
    }
    
    /**
     * Checks whether sentence s is a negation and parses if it is
     * @param s - sentence of formal logic in String form
     */
    private void checkNegation(String s)
    {
        if (s.charAt(0) == '~')
        {
            //findMainOperator(s) returns -1 when
            //there is no operator outside of parentheses
            //e.g. "~(A v B)"
            if (findMainOperator(s) == -1)
            {
                parseNegation(s);
            }
        }
    }
    
    /**
     * Takes a sentence of formal logic and parses
     * sub-sentences recursively.
     * 
     * @param sentence - sentence of formal logic in String form
     */
    private void parseSentence(String s)
    {
        //system.out.println(indent + "~~~~~~Parsing sentence \"" + s + "\"~~~~~~");
        
        //if entire sentence is a negation, check negation before assigning
        //leftjunct and rightjunct
        checkNegation(s);
        
        //if entire sentence is a negation, parsing may be done here
        if (!statements.containsKey(s))
        {
            //main logical operator
            int mloIndex = findMainOperator(s);
            String mlo = s.substring(mloIndex, mloIndex + 1);
            //system.out.println(indent + "mlo = " + mlo + ", " + mloIndex);

            String leftjunct = s.substring(0, mloIndex).trim();
            leftjunct = trimParens(leftjunct);
            //system.out.println(indent + "leftjunct = " + leftjunct);

            String rightjunct = s.substring(mloIndex + 1).trim();
            rightjunct = trimParens(rightjunct);
            //system.out.println(indent + "rightjunct = " + rightjunct);

            checkStatements(leftjunct);
            
            checkStatements(rightjunct);

            addStatement(leftjunct, mlo, rightjunct);
        }
    }
    
    /**
     * Parses a negation and adds the sub-sentence
     * as well as the full negation to the LinkedHashMap
     * @param s - sentence of formal logic in String form
     */
    private void parseNegation(String s)
    {
        //system.out.println(indent + "~~~~~~Parsing negation \"" + s + "\"~~~~~~");

        String substring = s.substring(1);
        substring = trimParens(substring);
        
        if (!statements.containsKey(substring))
        {
            indent(8);
            parseSentence(substring);
        }
        
        addNegation(s);
    }
    
    
    /**
     * toString method for Sentence
     * 
     * @return - String version of this sentence
     */
    public String toString()
    {
        String s = "";
        
        
        //s will take value of last key, i.e. the full sentence
        for (String key : statements.keySet())
        {
            s = key;
        }
        
        return s;
    }
    
    /**
     * Sentences are equal if String forms are equal
     * 
     * @param obj - object being compared
     * @return true if sentence Strings are equal, false otherwise
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof Sentence)
        {
            Sentence s = (Sentence) obj;
            
            if (s.toString().equals(toString()))
            {
                return true;
            }
            
            return false;
        }
        else
        {
            return false;
        }
    }
    
        
   
    
   
}

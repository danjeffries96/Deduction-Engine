
import java.util.ArrayList;
import java.util.LinkedHashMap;



/**
 * Table class for truth tables.
 * 
 * @author Daniel Jeffries
 *
 */
class Table
{
    private int rows;
    private int columns;
    private LinkedHashMap<String, CompoundBool> statements;
    private ArrayList<String> letters;
    private ArrayList<String> keys;
    private Boolean [][] cells;

    /**
     * Table constructor.
     * @param sentence - Sentence object being used for truth table
     */
    public Table(Sentence s)
    {
        this.statements = s.statements;
        this.letters = s.letters;
        this.keys = s.getKeys();
        sortLettersandKeys(letters, keys);
        this.columns = s.statements.size();
        this.rows = 1 + (int) Math.pow(2, s.letters.size());
        this.cells = new Boolean [rows][columns];
        setCells();

    }

    /**
     * Sorts letters and keys and their negations alphabetically
     * @param keys - ArrayList to sort
     */
    private void sortLettersandKeys(ArrayList<String> letters, ArrayList<String> keys)
    {
        //sorting letters alphabetically
        for (int i = 0; i < letters.size()-1; i++)
        {
            for (int j = i + 1; j < letters.size(); j++)
            {  

                if (letters.get(i).charAt(letters.get(i).length()-1) - '0' 
                    > letters.get(j).charAt(letters.get(j).length()-1) - '0')
                {

                    String temp = letters.get(i);
                    letters.set(i, letters.get(j));
                    letters.set(j, temp);
                }

            }

        }

        //sorting keys alphabetically
        for (int i = 0; i < keys.size()-1; i++)
        {
            for (int j = i + 1; j < keys.size(); j++)
            {                    
                if (keys.get(i).length() < 2 && keys.get(j).length() < 2)
                {
                    if (keys.get(i).charAt(keys.get(i).length()-1) - '0' 
                        > keys.get(j).charAt(keys.get(j).length()-1) - '0')
                    {

                        String temp = keys.get(i);
                        keys.set(i, keys.get(j));
                        keys.set(j, temp);
                    }
                }
                else if (keys.get(i).length() == 2 && keys.get(j).length() == 2)
                {
                    if (keys.get(i).charAt(keys.get(i).length()-1) - '0' 
                        > keys.get(j).charAt(keys.get(j).length()-1) - '0')
                    {

                        String temp = keys.get(i);
                        keys.set(i, keys.get(j));
                        keys.set(j, temp);
                    }
                }
            }
        }

    }

    /**
     * Iterates through columns and rows and sets the
     * truth value for each cell.
     */
    public void setCells()
    {
        //iterating down rows
        for (int r = 0; r < rows; r++)
        {
            //iterating across columns
            for (int c = 0; c < keys.size(); c++)
            {
                //assigning letter values for each row
                if (c < letters.size())
                {
                    boolean truth = (r % ((rows - 1) / Math.pow(2,c)) + 1) 
                        <= (((rows - 1) / Math.pow(2,c)) / 2);

                    //                        System.out.println("row = " + r + ", column = " + c + "; " 
                    //                            + "math = " + (r) + " % " + (rows - 1) / Math.pow(2, c)
                    //                                + " = " + ((r % ((rows - 1) / Math.pow(2, c))) + 1)
                    //                            + " <= " + (rows / (c + 1)) / 2
                    //                            + " = " + truth);

                    statements.get(letters.get(c)).setValue(truth);
                }
                cells[r][c] = statements.get(keys.get(c)).getValue();
            }
        }
    }
    
    public Boolean[][] getCells()
    {
        return cells;
    }

}
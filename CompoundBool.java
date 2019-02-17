
public class CompoundBool
{
    private CompoundBool leftRef;
    private CompoundBool rightRef;
    private Boolean value;
    private String operator;

    private final String HORSESHOE = "\u2283";
    private final String TRIPLEBAR = "\u2261";    
    
    
    /**
     * Constructor for simple logical sentence.
     * @param bool - boolean being passed to value
     */
    public CompoundBool(boolean bool)
    {
        this.value = bool;
    }
    
    /**
     * Constructor for compound sentences. Accepts multiple arguments
     * to support negations (where only one sub-sentence is passed).
     * 
     * @param operator - logical operator in String form
     * @param bools - sub-sentences being passed
     */
    public CompoundBool(String operator, CompoundBool... bools)
    {
        this.leftRef = bools[0];
        if (bools.length == 2)
        {
            this.rightRef = bools[1];
        }
        this.operator = operator;
    }
    
    /**
     * Get method for operator
     * @return - operator in String form
     */
    public String getOperator()
    {
        return operator;
    }
    
    
    /**
     * Getter method for value. Returns values for compound
     * sentences recursively.
     * 
     * @return value
     */
    public Boolean getValue()
    {
        if (operator != null)
        {
            if (operator.equals("&"))
            {
                return leftRef.getValue() && rightRef.getValue();
            }
            else if (operator.equals("v"))
            {
                return leftRef.getValue() || rightRef.getValue();
            }
            else if (operator.equals(HORSESHOE))
            {
                return !leftRef.getValue() || rightRef.getValue();
            }
            else if (operator.equals(TRIPLEBAR))
            {
                return (leftRef.getValue() && rightRef.getValue())
                        || (!leftRef.getValue() && !rightRef.getValue());
            }
            else if (operator.equals("~"))
            {
                return !leftRef.getValue();
            }
            //default case
            else
            {
                return false;
            }
        }
        //simple sentence (no operator)
        else
        {
            return value;
        }
    }
    
    /**
     * Setter method for field value.
     * @param b - boolean to which value is set
     */
    public void setValue(Boolean b)
    {
        value = b;
    }


}

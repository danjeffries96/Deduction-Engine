import java.awt.BorderLayout;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


/**
 * A class to draw logical truth tables
 * 
 * @author Daniel Jeffries
 *
 */
public class TruthTableFrame
{
    
    private final int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    private final int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    private final int FRAME_WIDTH = 700;
    private final int FRAME_HEIGHT = 500;
    private final int TOP_PANEL_SIZE = 100;
    private JFrame frame;
    private JPanel tablePanel;
    private JLabel errorLabel;
    private JTextField input;
    private JButton ampersand;
    private JButton wedge;
    private JButton horseshoe;
    private JButton tripleBar;
    private JButton compute;
    
    /**
     *  Constructor calls init method
     */
    public TruthTableFrame()
    {
        init();
    }
    
    /**
     * Initializes components
     */
    private void init()
    {
        frame = new JFrame("Truth Table");
        frame.setLocation((SCREEN_WIDTH - FRAME_WIDTH) / 2, (SCREEN_HEIGHT - FRAME_HEIGHT) / 2);
        frame.setSize(FRAME_WIDTH ,FRAME_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        ActionListener buttonHandler = new ButtonHandler();
        Font f = new Font("Monospaced", Font.PLAIN, 20);

        JLabel instructions = new JLabel("Enter a sentence of formal logic. "
                                            + "Example syntax: ~((A v ~B) v C)");
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.red);
        
        instructions.setFont(new Font("Garamond", Font.PLAIN, 12));
        input = new JTextField();
        
        ampersand = new JButton("&");
        ampersand.setFont(f);
        ampersand.addActionListener(buttonHandler);
        
        wedge = new JButton("v");
        wedge.setFont(f);
        wedge.addActionListener(buttonHandler);
        
        
        horseshoe = new JButton("\u2283");
        horseshoe.setFont(f);
        horseshoe.addActionListener(buttonHandler);
        
        tripleBar = new JButton("\u2261");
        tripleBar.setFont(f);
        tripleBar.addActionListener(buttonHandler);
        
        compute = new JButton("Compute");
        compute.setFont(new Font("Garamond", Font.BOLD, 14));
        compute.addActionListener(buttonHandler);

        JPanel inputPanel = new JPanel();
        GridLayout grid = new GridLayout(2, 3);
        inputPanel.setLayout(grid);
        grid.setHgap(45);
        inputPanel.add(ampersand);
        inputPanel.add(input);
        inputPanel.add(horseshoe);
        inputPanel.add(wedge);
        inputPanel.add(compute);
        inputPanel.add(tripleBar);
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        
        instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(instructions);
        topPanel.add(errorLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(inputPanel);
        
        frame.add(topPanel, BorderLayout.NORTH);
                             
        frame.setVisible(true);

    }
   
    
    /**
     * Adds tablePanel to frame or if one is already there, removes the existing one
     * adds the updated tablePanel.
     * 
     * @param t - updated table
     */
    public void updateTable(Table t)
    {
        tablePanel = t;
        
        tablePanel.setPreferredSize(new Dimension(tablePanel.getWidth() + 50, tablePanel.getHeight()));
        JScrollPane scrollPane = new JScrollPane(tablePanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        if (frame.getContentPane().getComponentCount() > 1)
        {
            frame.getContentPane().remove(1);
            frame.revalidate();
            frame.repaint();
        }
                
        frame.add(scrollPane);
        frame.validate();
        frame.repaint();
        
        }
    
    
    /**
     * Table class used to draw the actual truth table.
     * 
     * @author Daniel Jeffries
     *
     */
    class Table extends JPanel
    {
        private int width;
        private int height;
        private int rows;
        private int columns;
        private double rowScale;
        private double [] columnPoints;
        private double columnScale;
        public int xoffset;
        public int yoffset;
        private int lastcolumn;
        private LinkedHashMap<String, CompoundBool> statements;
        private ArrayList<String> letters;
        private ArrayList<String> keys;
        private Boolean [][] cells;
        private Color darkGreen = new Color(34, 139, 34);
        private Color darkRed = new Color(220, 20, 60);
        
        
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
            
            this.height = rows * 25;

            this.width = statements.size()*100;
            
            this.setSize(this.width + 100, this.height + 50);
            
            setColumnsandRows();
            
            checkSize();
            
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

                        System.out.println("swapping " + letters.get(i) + " and " + letters.get(j));
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

                            System.out.println("swapping " + keys.get(i) + " and " + keys.get(j));
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

                            System.out.println("swapping " + keys.get(i) + " and " + keys.get(j));
                            String temp = keys.get(i);
                            keys.set(i, keys.get(j));
                            keys.set(j, temp);
                        }
                    }
                }
            }
            
        }
        
        /**
         * Checks if table is larger than frame and resizes accordingly.
         */
        public void checkSize()
        {
            if (frame.getWidth() < lastcolumn)
            {
                frame.setSize(SCREEN_WIDTH - 100, frame.getHeight());
            }
            
            //if rows don't fit in frame
            if (frame.getHeight() - TOP_PANEL_SIZE < height)
            {
                //if rows fit in screen, resize frame and account for topPanel
                if (height < SCREEN_HEIGHT - 100)
                {
                    frame.setSize(frame.getWidth(), height + 150);
                }
                else
                {
                    frame.setSize(frame.getWidth(), SCREEN_HEIGHT - 100);
                }
                
               
            }
            
            frame.setLocation((SCREEN_WIDTH - frame.getWidth()) / 2, 
                (SCREEN_HEIGHT - frame.getHeight()) / 2);

        }
        
        /**
         * Iterates through ArrayList keys and determines how wide
         * each column should be. Also sets row scale based on number of rows.
         */
        public void setColumnsandRows()
        {
            
            int totalLength = 0;
            columnPoints = new double[keys.size() + 1];
            rowScale = height / (double) rows;
            
            columnPoints[0] = 0;
            for (int i = 1; i <= keys.size(); i++)
            {
                //even out spacing for smaller columns
                if (keys.get(i-1).length() <= 2)
                {
                    columnPoints[i] = totalLength + keys.get(i-1).length() + 2;
                    totalLength += keys.get(i-1).length() + 2;
                }
              
                else
                {
                    columnPoints[i] = totalLength + keys.get(i-1).length();
                    totalLength += keys.get(i-1).length();
                }
            }
            
            if (totalLength > 100)
            {
                width = totalLength * 10;
            }

            columnScale = width/totalLength;
            lastcolumn = (int) (columnScale*columnPoints[keys.size()]);
            
            xoffset = (frame.getWidth() - lastcolumn) / 2;
            yoffset = 25;
            
            //if columns don't fit on screen;
            if (frame.getWidth() < lastcolumn)
            {
                width = lastcolumn + 50;
                xoffset = (width - lastcolumn) / 2;
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
        
      
        /**
         * Overrides paintComponent method for JPanel. Draws truth table.
         */
        @Override
        public void paintComponent(Graphics g)
        {
            setColumnsandRows();
            
            //setting preferred size for scrollpane functionality
            this.setPreferredSize(new Dimension(width + xoffset, height + yoffset + 25));
                        
            //drawing first column line
            g.drawLine(xoffset, yoffset, xoffset, yoffset + height);
            
            //not drawing final column line because
            //it wont be accurate with cast to int
            for (int c = 0; c < columns; c++)
            {
                g.drawLine((int)(xoffset + columnScale*columnPoints[c]), yoffset, 
                    xoffset + (int) (columnScale*columnPoints[c]), yoffset + height);
            }
            //final column line
            g.drawLine(xoffset + lastcolumn, yoffset, xoffset + lastcolumn, yoffset + height);
            
            //draw row lines
            for (int r = 0; r < rows; r++)
            {
                g.drawLine(xoffset, yoffset + (int) (r*rowScale), lastcolumn + xoffset, yoffset + (int) (r*rowScale));
            }
            g.drawLine(xoffset, yoffset + height, lastcolumn + xoffset, yoffset + height);
            
            
            //draw cell values
           for (int c = 0; c < columns; c++)
           {
               int stringWidth = g.getFontMetrics().stringWidth("T");

               int cellOffset = (int) (int) ((((int) columnScale*columnPoints[c+1]
                   - (int) columnScale*columnPoints[c])
                   - stringWidth) / 2);
               
               for (int r = 0; r < rows; r++)
               {
                   if (r == 0)
                   {
                       g.setColor(Color.black);
                       g.setFont(new Font("Monospaced", Font.BOLD, 12));

                       int headerWidth = g.getFontMetrics().stringWidth(keys.get(c));
                       int columnHeaderOffset = (int) ((((int) columnScale*columnPoints[c+1]
                                           - (int) columnScale*columnPoints[c])
                                           - headerWidth) / 2);
                                           
                       int headerXPosition = (int) (xoffset + columnHeaderOffset 
                           + (int)columnScale*columnPoints[c]);
                       g.drawString(keys.get(c), headerXPosition, yoffset + (int) (5 + rowScale/2));
                   }
                   else
                   {
                       g.setColor(cells[r-1][c] ? darkGreen : darkRed);
                       int cellXPosition = (int) (xoffset + cellOffset 
                           + (int)columnScale*columnPoints[c]);
                       g.drawString(cells[r-1][c] ? "T" : "F", cellXPosition, yoffset + (int) (5 + r*rowScale + rowScale/2));
                   }
                   
               }
           }
           
        }
        
    }
    
    class ButtonHandler implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource().equals(horseshoe))
            {
                input.setText(input.getText() + "\u2283");
            }
            else if (e.getSource().equals(tripleBar))
            {
                input.setText(input.getText() + "\u2261");
            }
            else if (e.getSource().equals(wedge))
            {
                input.setText(input.getText() + "v");
            }
            else if (e.getSource().equals(ampersand))
            {
                input.setText(input.getText() + "&");
            }
            else if (e.getSource().equals(compute))
            {
                try
                {
                    updateTable(new Table(new Sentence(input.getText())));
                    if (errorLabel.getText().contains("Error"))
                    {
                        errorLabel.setText("");
                    }
                }
                catch (Exception ex)
                {
                    errorLabel.setText("Error: Sentence is not well formed");
                    ex.printStackTrace();
                }
            }
        }
        
    }
    
    
    public static void main(String args[])
    {        
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                TruthTableFrame frame = new TruthTableFrame();
            }
        });
    }
}

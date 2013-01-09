package jip.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create simple string table representations. Call {@link #toString()} to
 * get the string representation.
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class SimpleTablePrinter {
    /**
     * The table header
     */
    private final List<String> header;

    /**
     * Number of columns
     */
    private int columns;
    /**
     * Column width
     */
    private int[] columnWidths;

    /**
     * The rows
     */
    private final List<List<Object>> rows;

    /**
     * Print the header
     */
    private final boolean printHeader;

    /**
     * Print borders
     */
    private final boolean printBorders;
    /**
     * Print footer
     */
    private final boolean printFooter;

    /**
     * Create a new printer without header
     */
    public SimpleTablePrinter() {
        this(null);
    }

    /**
     * Create a new table printer. If the header list is not null, a header is printed
     *
     * @param header
     */
    public SimpleTablePrinter(final List<String> header) {
        this(header, true, true, false);
    }

    /**
     * Create a new table printer
     *
     * @param header the header
     * @param printHeader print the header
     * @param printBorders print borders
     * @param printFooter print the footer
     */
    public SimpleTablePrinter(final List<String> header, final boolean printHeader, final boolean printBorders, final boolean printFooter) {
        this.header = header;
        this.printFooter = printFooter;
        this.rows = new ArrayList<List<Object>>();
        this.printHeader = printHeader;
        this.printBorders = printBorders;
        if(header != null){
            columns = header.size();
            columnWidths = new int[columns];
            updateColumnsWidths(header);
        }
    }

    /**
     * Update the column width based on the
     * given element list
     *
     * @param elements the elements
     */
    protected void updateColumnsWidths(final List elements) {
        for (int i = 0; i < columnWidths.length; i++) {
            Object s = elements.get(i);
            if(s == null){
                s = "";
            }
            columnWidths[i] = Math.max(columnWidths[i], s.toString().length());
        }
    }

    /**
     * Add a row
     *
     * @param elements the row elements
     */
    public void addRow(final Object...elements){
        addRow(Arrays.asList(elements));
    }
    /**
     * Add a row
     *
     * @param elements the row elements
     */
    public void addRow(final List<Object> elements){
        if(elements == null) throw new NullPointerException("NULL row elements not permitted");
        if(columns == 0) columns = elements.size();
        if(elements.size() > columns) throw new IllegalArgumentException(elements.size()+" "+elements + " but only " + columns + " columns defined");
        this.rows.add(elements);
        updateColumnsWidths(elements);
    }

    /**
     * Create the string representation of the table
     *
     * @return string the table
     */
    public String toString(){
        StringBuilder b = new StringBuilder();
        int SPACE = 2;
        int length = 0;
        StringBuilder line = new StringBuilder();
        if(printHeader && header != null){
            if(printBorders){
                b.append("| ");
            }
            for (int i = 0; i < columnWidths.length; i++) {
                String s = header.get(i);
                String columnHeader = String.format("%-" + Math.max(SPACE, columnWidths[i]) + "s" + (printBorders ? " | " : ""), s);
                length+=columnHeader.length();
                b.append(columnHeader);
            }
            b.append("\n");
            if(printBorders)line.append("|");
            for (int i = 0; i < columnWidths.length; i++) {
                for(int j=0; j < columnWidths[i]+SPACE; j++){
                    line.append("-");
                }
                if(i+1 <columnWidths.length){
                    line.append("+");
                }
            }
            if(printBorders)line.append("|");
            line.append("\n");
            b.append(line);
        }
        for (List<Object> row : rows) {
            if(printBorders){
                b.append("| ");
            }
            for (int i = 0; i < columnWidths.length; i++) {
                Object o = row.get(i);
                String s = o == null ? ""  : o.toString();
                b.append(String.format("%-"+Math.max(SPACE, columnWidths[i])+"s" + (printBorders ? " | " : ""), s));
            }
            b.append("\n");
        }

        if(printFooter){
            b.append(line);
            int l = length;
            if(printBorders){
                b.append("| ");
                l -= 2;
            }
            int size = rows.size();
            String w = size+" Element"+(size>1? "s":"");
            String formatString = "%-" + Math.max(SPACE, l) + "s";
            b.append(String.format(formatString, w));
            if(printBorders){b.append("|");}
            b.append("\n");
            b.append(line);
        }
        return b.toString();
    }


}

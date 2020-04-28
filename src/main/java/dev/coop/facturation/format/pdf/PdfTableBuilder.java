package dev.coop.facturation.format.pdf;

import dev.coop.facturation.format.Coord;
import dev.coop.facturation.format.Style;
import dev.coop.facturation.format.Style.Align;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lfo
 */
public class PdfTableBuilder {

    private static final int CELL_BORDER = 2;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final PdfBuilder pdf;
 
    private final int[] colsWidth;
    private int currentColIndex;
    private int currentHeightRow;
    
    private Coord startRowCoord;
    private Coord currentCellCoord;
    

    public PdfTableBuilder(PdfBuilder pdf, int[] colsWidth) {
        this.pdf = pdf;
        this.colsWidth = colsWidth;
        this.startRowCoord = pdf.coord().copy();
        this.currentCellCoord = pdf.coord();
        this.currentHeightRow = currentCellCoord.getY();
    }

    public PdfTableBuilder printCell(String cellContent) {
        return printCell(cellContent, Align.LEFT);
    }
    
    
    public PdfTableBuilder printCell(String cellContent,  Align align) {
        try {

            Coord bottomLeft = currentCellCoord.copy();
            int width = getWidth(currentColIndex);
            final Style style = pdf.style();
            List<String> contentLines = PdfBuilder.wrapLines(style, cellContent, width);
            
            contentLines.stream().forEach((_item) -> {
                bottomLeft.decrY(style);
            });
            bottomLeft.incrY(style);
           
            Coord topRight = currentCellCoord.copy().incrX(width).incrY(style);
            
            pdf.setCoord(currentCellCoord);
            pdf.printWrapped(cellContent, width, align);
            
            currentCellCoord = getNextCurrentCellCoord(bottomLeft, topRight, style);
            
            
            logger.debug(bottomLeft.toString() +", "+ width);
        } catch (IOException ex) {
            // 
        }
        return this;
    }

    public PdfBuilder getBuilder() {
        return pdf;
    }
    
    private int getWidth(int colIndex) {
        return colsWidth[colIndex];
    }

    private Coord getNextCurrentCellCoord(Coord bottomLeft, Coord topRight, Style style) {
       if (currentColIndex < colsWidth.length -1) {
           final Coord toReturn = new Coord(topRight.getX(),startRowCoord.getY());
           currentColIndex++;
           currentHeightRow = Integer.min(currentHeightRow, bottomLeft.getY());
           return toReturn;
       } else {
           currentColIndex = 0;
           startRowCoord = new Coord(startRowCoord.getX(),currentHeightRow).decrY(style);
           return startRowCoord;
       }
    }
    
   
}

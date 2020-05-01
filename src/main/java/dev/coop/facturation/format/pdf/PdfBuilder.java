package dev.coop.facturation.format.pdf;

import dev.coop.facturation.format.Coord;
import dev.coop.facturation.format.FormatException;
import dev.coop.facturation.format.Style;
import dev.coop.facturation.format.Style.Align;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/ullenboom/jrtf
 *
 * @author lfo
 */
public class PdfBuilder implements Style.StyleEventListener {

    private static final int DEFAULT_USER_SPACE_UNIT_DPI = 72;
    private static final float MM_TO_UNITS = 1 / (10 * 2.54f) * DEFAULT_USER_SPACE_UNIT_DPI;

    private static final String EURO = "€";

    private final PDDocument document;
    private final PDPage page;
    private final PDPageContentStream contentStream;
    private Coord coord;
    private Style style;

    public PdfBuilder(Coord size, Style style) {
        try {
            this.coord = size;
            document = new PDDocument();
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            setStyle(style);
        } catch (IOException ex) {
            throw new FormatException(ex);
        }
    }

    public Coord coord() {
        return coord;
    }

    public PdfBuilder setCoord(Coord coord) {
        this.coord = coord;
        return this;
    }

    public Style style() {
        return style;
    }

    public final PdfBuilder setStyle(Style style) {
        this.style = style;
        style.register(this);
        return this;
    }

    public PdfBuilder putImage(byte[] input, int width, int height, final String name) {
        if (input == null) {
            return this;
        }

        PDImageXObject image;
        try {
            image = PDImageXObject.createFromByteArray(document, input, name);
        } catch (IOException e) {
            throw new FormatException(e);
        }

        try {
            Dimension scaledDim = getScaledDimension(new Dimension(image.getWidth(), image.getHeight()), new Dimension((int) toUnits(width), (int) toUnits(height)));
            contentStream.drawImage(image, toUnits(coord.getX()), toUnits(coord.getY()), scaledDim.width, scaledDim.height);
        } catch (IOException ex) {
//            throw new FormatException(ex);
        }
        return this;
    }

    public PdfBuilder println(String text) {
        print(text).println();
        return this;
    }

    public PdfBuilder println() {
        coord.decrY(style);
        return this;
    }

    private PdfBuilder print(String text) {
        return print(text, 0, Align.LEFT);
    }

    private PdfBuilder print(String text, int width, Align align) {
        if (text == null) {
            text = "";
        }
        try {
            final float boxWidth = toUnits(width);
            final float textWidth = getStringWidth(style, text);
            final float boxHeight = Coord.computeHeight(style) * 4;
            final float x = toUnits(coord.getX());
            final float y = toUnits(coord.getY());
            final int borderSize = 1;

            if (style.getBackgroundColor() != null && !style.getBackgroundColor().equals(Color.WHITE)) {
                contentStream.setNonStrokingColor(style.getBackgroundColor());
                contentStream.addRect(x, y - Coord.computeHeight(style), boxWidth - borderSize, boxHeight - borderSize);
                contentStream.setNonStrokingColor(style.getColor());
            }

            contentStream.beginText();

            switch (align) {
                case LEFT:
                    contentStream.newLineAtOffset(x, y);
                    break;
                case RIGHT:
                    contentStream.newLineAtOffset(x + boxWidth - textWidth - borderSize, y);
                    break;
                case CENTER:
                    contentStream.newLineAtOffset(x + (boxWidth - textWidth) / 2, y);
                    break;
            }
            contentStream.showText(text.replace("\u00A0", " "));
            contentStream.endText();

        } catch (IOException ex) {
//            throw new FormatException(ex);
        }
        return this;
    }

    public PdfBuilder printlnIfNotNull(String text) {
        if (text != null) {
            PdfBuilder.this.println(text);
        }
        return this;
    }

    public PdfBuilder printWrapped(String text, int width, Align align) {
        try {
            for (String split : wrapLines(style, text, width)) {
                PdfBuilder.this.print(split, width, align);
                this.println();
//                coord.decrY(style);
            }
            coord.incrY(style);
        } catch (IOException ex) {
        }
        return this;
    }

    public PdfBuilder drawLine(Coord start, Coord end) {
        try {
            contentStream.moveTo(toUnits(start.getX()), toUnits(start.getY()));
            contentStream.lineTo(toUnits(end.getX()), toUnits(end.getY()));
        } catch (IOException ex) {
        }
        return this;
    }

    public PdfTableBuilder createTableBuilder(int[] colsWidth) {
        return new PdfTableBuilder(this, colsWidth);
    }

    @Override
    public void onEvent(Style.StyleEvent event, Style style) {
        try {
            switch (event) {
                case CHANGE_COLOR:
                    contentStream.setNonStrokingColor(style.getColor());
                    break;
                case CHANGE_FONT:
                    contentStream.setFont(style.getPDFont(), style.getSize());
                    break;
                case CHANGE_SIZE:
                    contentStream.setFont(style.getPDFont(), style.getSize());
                    break;
                case CHANGE_BACKGROUND_COLOR:
                    break;

            }
        } catch (IOException exception) {

        }
    }

    public PDDocument toDocument() {
        try {
            contentStream.close();
            return document;
        } catch (IOException ex) {
            throw new FormatException(ex);
        }
    }

    public static float toUnits(int mm) {
        return mm * MM_TO_UNITS;
    }

    public static List<String> wrapLines(Style style, String text, int width) throws IOException {
        List<String> result = new ArrayList();

        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {

            String[] split = line.split("(?<=\\W)");
            int[] possibleWrapPoints = new int[split.length];
            possibleWrapPoints[0] = split[0].length();
            for (int i = 1; i < split.length; i++) {
                possibleWrapPoints[i] = possibleWrapPoints[i - 1] + split[i].length();
            }

            int start = 0;
            int end = 0;
            for (int i : possibleWrapPoints) {
                final String substring = line.substring(start, i);
                float currentWidth = getStringWidth(style, substring);
                if (start < end && currentWidth > toUnits(width)) {
                    result.add(line.substring(start, end));
                    start = end;
                }
                end = i;
            }
            // Last piece of text
            result.add(line.substring(start));
        }
        return result;
    }

    private static float getStringWidth(Style style, final String string) throws IOException {
        return style.getPDFont().getStringWidth(string.replace("\u00A0", " ")) / 1000 * style.getSize();
    }

    private static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {
        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }

        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }

        return new Dimension(new_width, new_height);
    }
}

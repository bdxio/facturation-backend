package dev.coop.facturation.format;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class Style {

    private int size;
    private boolean isBold;
    private boolean isOblique;

    private final Font font;
    private Color color = Color.BLACK;
    private Color backgroundColor;
    private final List<StyleEventListener> listeners = new ArrayList<>();

    private Style(Font font) {
        this.font = font;
        this.size = Size.NORMAL.size;
    }

    public static Style createHelvetica() {
        return new Style(Font.HELVETICA);
    }

    public int getSize() {
        return size;
    }

    public Font getFont() {
        return font;
    }

    public PDFont getPDFont() {
        if (isBold && isOblique) {
            return font.getBoldOblique();
        } else if (isBold) {
            return font.getBold();
        } else if (isOblique) {
            return font.getOblique();
        }
        return font.getNormal();
    }

    public int getFontHeight() {
        return Math.round(getPDFont().getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * size);
    }

    public Style bold() {
        isBold = true;
        fireEvent(StyleEvent.CHANGE_FONT);
        return this;
    }

    public Style italic() {
        isOblique = true;
        fireEvent(StyleEvent.CHANGE_FONT);
        return this;
    }

    public Style unbold() {
        isBold = false;
        fireEvent(StyleEvent.CHANGE_FONT);
        return this;
    }

    public Style unitalic() {
        isOblique = false;
        fireEvent(StyleEvent.CHANGE_FONT);
        return this;
    }

    public Style small() {
        return setSize(Size.SMALL);
    }

    public Style normal() {
        return setSize(Size.NORMAL);
    }

    public Style large() {
        return setSize(Size.LARGE);
    }

    public Style huge() {
        return setSize(Size.HUGE);
    }

    public Style veryHuge() {
        return setSize(Size.VERY_HUGE);
    }

    public Style setColor(Color color) {
        this.color = color;
        fireEvent(StyleEvent.CHANGE_COLOR);
        return this;
    }

    public Style reset() {
        setColor(Color.BLACK);
        setBackgroundColor(Color.WHITE);
        setSize(Size.NORMAL);
        unbold();
        unitalic();
        return this;
    }
    public Style setBackgroundColor(Color color) {
        this.backgroundColor = color;
        fireEvent(StyleEvent.CHANGE_BACKGROUND_COLOR);
        return this;
    }

    public Color getColor() {
        return color;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }
    public void register(StyleEventListener listener) {
        listeners.add(listener);
    }

    private void fireEvent(StyleEvent event) {
        listeners.stream().forEach((listener) -> {
            listener.onEvent(event, this);
        });
    }

    private Style setSize(Size size) {
        this.size = size.size;
        fireEvent(StyleEvent.CHANGE_SIZE);
        return this;
    }

    public enum Align {

        LEFT, CENTER, RIGHT;
    }

    public enum Size {

        SMALL(8), NORMAL(10), LARGE(13), HUGE(15), VERY_HUGE(18);

        private Size(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }

        private final int size;
    }

    public enum Font {

        HELVETICA(PDType1Font.HELVETICA, PDType1Font.HELVETICA_BOLD, PDType1Font.HELVETICA_OBLIQUE, PDType1Font.HELVETICA_BOLD_OBLIQUE);

        private final PDType1Font normal;
        private final PDType1Font bold;
        private final PDType1Font oblique;
        private final PDType1Font boldOblique;

        private Font(PDType1Font normal, PDType1Font bold, PDType1Font oblique, PDType1Font boldOblique) {
            this.normal = normal;
            this.bold = bold;
            this.oblique = oblique;
            this.boldOblique = boldOblique;
        }

        private PDType1Font getNormal() {
            return normal;
        }

        private PDType1Font getBold() {
            return bold;
        }

        private PDType1Font getOblique() {
            return oblique;
        }

        private PDType1Font getBoldOblique() {
            return boldOblique;
        }

    }

    public enum StyleEvent {

        CHANGE_COLOR, CHANGE_SIZE, CHANGE_FONT, CHANGE_BACKGROUND_COLOR;
    }

    public interface StyleEventListener {

        public void onEvent(StyleEvent event, Style style);
    }

}

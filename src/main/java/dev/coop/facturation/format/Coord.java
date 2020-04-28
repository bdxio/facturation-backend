package dev.coop.facturation.format;

public class Coord {

    protected int x;
    protected int y;

    private int maxX = Integer.MAX_VALUE;
    private int maxY = Integer.MAX_VALUE;

    public Coord() {
    }

    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Coord setX(int x) {
        this.x = x;
        return this;
    }

    public Coord setY(int y) {
        this.y = y;
        return this;
    }

    public Coord incrX(int incr) {
        x += incr;
        return this;
    }

    public Coord decrX(int decr) {
        x -=decr;
        return this;
    }

    public Coord decrY(int decr) {
        y -= decr;
        return this;
    }

    public Coord decrY(Style style) {
        y -= computeHeight(style);
        return this;
    }

    public Coord incrY(Style style) {
        y += computeHeight(style);
        return this;
    }

    public static int computeHeight(Style style) {
        return style.getSize() / 2;
    }

    public Coord copy() {
        final Coord toReturn = new Coord(getX(), getY()).setMaxX(this.maxX).setMaxY(this.maxY);
        return toReturn;
    }

    @Override
    public String toString() {
        return "[" + x + ',' + y + ']';
    }

    private Coord setMaxX(int maxX) {
        this.maxX = maxX;
        return this;
    }

    private Coord setMaxY(int maxY) {
        this.maxY = maxY;
        return this;
    }

    public static Coord createA4() {
        return new Coord(0, A4_HEIGHT).setMaxX(A4_WIDTH).setMaxY(A4_HEIGHT);
    }

    public final static int A4_WIDTH = 210;
    public final static int A4_HEIGHT = 297;

    public Coord immutable() {
        return new Immutable(this);
    }

    private class Immutable extends Coord {

        public Immutable(Coord coord) {
            this.x = coord.getX();
            this.y = coord.getY();
        }

        @Override
        public Coord immutable() {
            return super.immutable();
        }

        @Override
        public String toString() {
            return "[" + x + ',' + y + ']';
        }

        @Override
        public Coord copy() {
            return super.copy();
        }

        @Override
        public Coord incrY(Style style) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Coord incrX(int incr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public Coord setY(int y) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Coord setX(int x) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Coord decrY(Style style) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Coord decrY(int decr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Coord decrX(int decr) {
            throw new UnsupportedOperationException();
        }
    }
}

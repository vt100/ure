package ure.areas;

import ure.math.URandom;
import ure.sys.Injector;
import ure.things.UThing;
import ure.things.UThingCzar;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * A Shapemask is a 1-bit 2D array of cells.  Landscaper methods can generate these to use for stamping terrain into areas.
 *
 * Shapemask has many public methods for warping, evolving, and otherwise changing the shape it contains.  You can combine these
 * methods in your Landscapers to build interesting terrain.
 *
 */
public class Shape {

    @Inject
    URandom random;

    @Inject
    UThingCzar thingCzar;

    public int xsize, ysize;
    public boolean[][] cells;
    boolean[][] buffer;

    public static final int MASK_OR = 0;
    public static final int MASK_AND = 1;
    public static final int MASK_XOR = 2;
    public static final int MASK_NOT = 3;

    public Shape(int _xsize, int _ysize) {
        Injector.getAppComponent().inject(this);
        xsize = _xsize;
        ysize = _ysize;
        cells = new boolean[xsize][ysize];
        buffer = new boolean[xsize][ysize];
        for (int x=0;x<xsize;x++) {
            for (int y=0;y<ysize;y++) {
                cells[x][y] = false;
                buffer[x][y] = false;
            }
        }
    }

    public boolean isValidXY(int x, int y) {
        if (x < 0 || y < 0) return false;
        if (x >= xsize || y >= ysize) return false;
        return true;
    }

    /**
     * Calculate the 0f-1f density of this mask
     */
    public float density() {
        int total = 0;
        for (int x=0;x<xsize;x++)
            for (int y=0;y<ysize;y++)
                if (cells[x][y])
                    total++;
        return (float)total / (float)(xsize*ysize);
    }

    public void set(int x, int y) {
        if (isValidXY(x,y))
            cells[x][y] = true;
    }
    public void clear(int x, int y) {
        if (isValidXY(x,y))
            cells[x][y] = false;
    }
    public void write(int x, int y, boolean val) {
        if (isValidXY(x,y))
            cells[x][y] = val;
    }

    /**
     * write a cell of the internal scratch buffer
     */
    public void writeBuffer(int x, int y, boolean val) {
        if (isValidXY(x,y))
            buffer[x][y] = val;
    }

    public boolean value(int x, int y) {
        if (isValidXY(x,y))
            return cells[x][y];
        return false;
    }
    public boolean valueBuffer(int x, int y) {
        if (isValidXY(x,y))
            return buffer[x][y];
        return false;
    }
    public Shape clear() {
        for (int x=0;x<xsize;x++) {
            for (int y = 0;y < ysize;y++) {
                cells[x][y] = false;
            }
        }
        return this;
    }
    public Shape fill() {
        for (int x=0;x<xsize;x++) {
            for (int y = 0;y < ysize;y++) {
                cells[x][y] = true;
            }
        }
        return this;
    }
    public void fillRect(int x1, int y1, int x2, int y2) {
        for (int x=x1;x<=x2;x++)
            for (int y=y1;y<=y2;y++)
                set(x,y);
    }
    public void clearBuffer() {
        for (int x=0;x<xsize;x++)
            for (int y=0;y<ysize;y++)
                buffer[x][y] = false;
    }
    public void fillBuffer() {
        for (int x=0;x<xsize;x++)
            for (int y=0;y<ysize;y++)
                buffer[x][y] = true;
    }
    public Shape noiseWipe(float density) {
        for (int x=0;x<xsize;x++) {
            for (int y = 0;y < ysize;y++) {
                if (random.f() < density) write(x, y, true);
                else write(x, y, false);
            }
        }
        return this;
    }
    // TODO: write simplexWipe
    public Shape simplexWipe() {

        return this;
    }

    /**
     * neighbor count at x,y -- not including the point itself
     */
    public int neighbors(int x, int y) { return neighbors(x,y,1); }

    /**
     * neighbor count at x,y at manhattan-distance d
     */
    public int neighbors(int x, int y, int d) {
        if (!isValidXY(x,y)) return 0;
        int n = 0;
        for (int dx=-d;dx<=d;dx++) {
            for (int dy = -d;dy <= d;dy++) {
                if (dx != 0 || dy != 0) {
                    if (value(x + dx, y + dy))
                        n++;
                }
            }
        }
        return n;
    }

    /**
     * neighbor count in cardinal directions only
     */
    public int neighborsPrime(int x, int y) {
        if (!isValidXY(x,y))
            return 0;
        int n = 0;
        if (value(x+1,y)) n++;
        if (value(x-1,y)) n++;
        if (value(x,y+1)) n++;
        if (value(x,y-1)) n++;
        return n;
    }

    /**
     * Combine with another mask using MASKTYPE_X constants (OR, AND, XOR)
     */
    public Shape maskWith(Shape mask, int masktype) { return maskWith(mask, masktype, 0, 0); }
    public Shape maskWith(Shape mask, int masktype, int xoffset, int yoffset) {
        for (int x=0;x<mask.xsize;x++) {
            for (int y=0;y<mask.ysize;y++) {
                boolean src = mask.value(x,y);
                boolean dst = value(x+xoffset,y+yoffset);
                switch (masktype) {
                    case MASK_OR:   dst = (dst || src);
                        break;
                    case MASK_AND:  dst = (dst && src);
                        break;
                    case MASK_XOR:  dst = (dst || src) && !(dst && src);
                        break;
                    case MASK_NOT:  dst = dst && !src;
                        break;
                }
                write(x+xoffset,y+yoffset,dst);
            }
        }
        return this;
    }

    /**
     * Does the given mask have any true cells that touch my true cells (at the offset position)?
     */
    public boolean touches(Shape mask, int xoffset, int yoffset) {
        for (int x=0;x<xsize;x++) {
            for (int y=0;y<ysize;y++) {
                if (mask.value(x+xoffset,y+yoffset) && value(x,y))
                    return true;
            }
        }
        return false;
    }
    public boolean touches(Shape mask, int xoffset, int yoffset, Shape ignoremask) {
        for (int x=0;x<xsize;x++) {
            for (int y=0;y<ysize;y++) {
                if (mask.value(x+xoffset,y+yoffset) && value(x,y) && !ignoremask.value(x+xoffset,y+yoffset))
                    return true;
            }
        }
        return false;
    }

    /**
     * Write the internal scratch buffer into the actual cells
     */
    public void printBuffer() {
        for (int x=0;x<xsize;x++) {
            for (int y=0;y<ysize;y++) {
                cells[x][y] = buffer[x][y];
            }
        }
    }

    /**
     * Write shape to internal scratch buffer
     */
    public void backupToBuffer() {
        for (int x=0;x<xsize;x++)
            for (int y=0;y<ysize;y++)
                buffer[x][y] = cells[x][y];
    }

    /**
     * Write this mask into a real area as terrain
     */
    public void writeTerrain(UArea area, String terrain, int xoffset, int yoffset) { writeTerrain(area,terrain,xoffset,yoffset,1f); }
    public void writeTerrain(UArea area, String terrain, int xoffset, int yoffset, float density) {
        for (int x=0;x<xsize;x++)
            for (int y=0;y<ysize;y++)
                if (value(x,y))
                    if (density >= 1f || random.f() < density)
                        area.setTerrain(x+xoffset,y+yoffset,terrain);
    }

    /**
     * Write a thing into every true cell of this mask in the area
     */
    public void writeThings(UArea area, String thing, int xoffset, int yoffset) {
        for (int x=0;x<xsize;x++)
            for (int y=0;y<ysize;y++)
                if (value(x,y)) {
                    UThing t = thingCzar.getThingByName(thing);
                    t.moveToCell(area,x+xoffset,y+yoffset);
                }
    }
    /**
     * Read a terrain type from a real area as a mask
     */
    public Shape readTerrain(UArea area, String terrain, int xoffset, int yoffset) {
        for (int x=0;x<xsize;x++) {
            for (int y = 0;y < ysize;y++) {
                if (area.terrainAt(x + xoffset, y + yoffset).getName().equals(terrain)) {
                    write(x, y, true);
                }
            }
        }
        return this;
    }

    /**
     * Make and return a copy of this mask
     */
    public Shape copy() {
        Shape copy = new Shape(xsize, ysize);
        for (int x=0;x<xsize;x++)
            for (int y=0;y<ysize;y++)
                copy.cells[x][y] = cells[x][y];
        return copy;
    }

    /**
     * Flip true-false on all cells of this mask
     */
    public Shape invert() {
        for (int x=0;x<xsize;x++) {
            for (int y = 0;y < ysize;y++) {
                cells[x][y] = !cells[x][y];
            }
        }
        return this;
    }

    /**
     * Grow outward from all edges
     */
    public Shape grow(int repeats) {
        for (int i=0;i<repeats;i++) {
            for (int x=0;x<xsize;x++)
                for (int y=0;y<ysize;y++)
                    if (value(x,y) || (neighbors(x,y) >= 2))
                        writeBuffer(x,y,true);
                    else
                        writeBuffer(x,y,false);
            printBuffer();
        }
        return this;
    }

    /**
     * Shrink inward from all edges
     */
    public Shape shrink(int repeats) {
        for (int i=0;i<repeats;i++) {
            for (int x=0;x<xsize;x++)
                for (int y=0;y<ysize;y++)
                    if (value(x,y) && (neighbors(x,y) >= 7))
                        writeBuffer(x,y,true);
                    else
                        writeBuffer(x,y,false);
            printBuffer();
        }
        return this;
    }

    /**
     * Randomly thin out true cells to false.
     */
    public Shape noiseThin(float density) {
        for (int x=0;x<xsize;x++) {
            for (int y = 0;y < ysize;y++)
                if (value(x, y))
                    if (random.f() > density)
                        clear(x, y);
        }
        return this;
    }

    /**
     * Keep only cells N spaces apart from other true cells
     */
    public Shape sparsen(int minspace, int maxspace) {
        clearBuffer();
        for (int x=0;x<xsize;x++) {
            for (int y = 0;y < ysize;y++)
                if (value(x, y)) {
                    writeBuffer(x, y, true);
                    int radius = (minspace + random.i(maxspace - minspace)) / 2;
                    for (int dx = -radius;dx < radius;dx++)
                        for (int dy = -radius;dy < radius;dy++)
                            if (!valueBuffer(x + dx, y + dy))
                                write(x + dx, y + dy, false);
                }
        }
        return this;
    }

    public Shape erode(float rot, int passes) {
        clearBuffer();
        int threshold = 6;
        for (int i=0;i<passes;i++) {
            for (int x=0;x<xsize;x++) {
                for (int y=0;y<ysize;y++) {
                    if (value(x,y)) {
                        int n = neighbors(x,y);
                        if (n >= threshold) {
                            writeBuffer(x,y,true);
                        } else if (random.f() < (1f - rot)) {
                            writeBuffer(x,y,true);
                        } else {
                            writeBuffer(x,y,false);
                        }
                    }
                }
            }
            printBuffer();
        }
        return this;
    }

    /**
     * Select only edge cells
     */
    public Shape edges() {
        clearBuffer();
        for (int x=0;x<xsize;x++)
            for (int y=0;y<ysize;y++)
                if (value(x,y) && neighborsPrime(x,y) < 4)
                    writeBuffer(x,y,true);
        printBuffer();
        return this;
    }

    /**
     * Select only edge cells, including diagonal corners
     */
    public Shape edgesThick() {
        clearBuffer();
        for (int x=0;x<xsize;x++)
            for (int y=0;y<ysize;y++)
                if (value(x,y) && neighbors(x,y) < 8)
                    writeBuffer(x,y,true);
        printBuffer();
        return this;
    }

    /**
     * 'Jumble' cells with a CA-type method
     */
    public Shape jumble(int nearDensity, int farDensity, int passes) {
        clearBuffer();
        for (int i=0;i<passes;i++) {
            for (int x = 0;x < xsize;x++) {
                for (int y = 0;y < ysize;y++) {
                    int near = neighbors(x, y);
                    int far = neighbors(x, y, 2);
                    if (value(x, y)) {
                        near++;
                        far++;
                    }
                    if (near >= nearDensity || far <= farDensity)
                        writeBuffer(x, y, true);
                    else
                        writeBuffer(x, y, false);
                }
            }
            printBuffer();
        }
        return this;
    }

    /**
     * Smooth out noise and leave continuous shapes
     */
    public Shape smooth(int density, int passes) {
        clearBuffer();
        for (int i=0;i<passes;i++) {
            for (int x=0;x<xsize;x++) {
                for (int y=0;y<ysize;y++) {
                    int n = neighbors(x,y);
                    if (value(x,y)) {
                        n++;
                    }
                    if (n >= density)
                        writeBuffer(x,y,true);
                    else
                        writeBuffer(x,y,false);
                }
            }
            printBuffer();
        }
        return this;
    }

    /**
     * Prune dead-end one space hallways
     */
    public Shape pruneDeadEnds() {
        clearBuffer();
        int passes = 20;
        for (int i=0;i<passes;i++) {
            boolean killedone = false;
            for (int x=0;x<xsize;x++) {
                for (int y=0;y<ysize;y++) {
                    if (value(x,y)) {
                        int n = neighborsPrime(x, y);
                        if (n <= 1) {
                            writeBuffer(x, y, false);
                            killedone = true;
                        } else
                            writeBuffer(x,y,true);
                    }
                }
            }
            printBuffer();
            if (!killedone) return this;
        }
        return this;
    }

    /**
     * Pick N random cells of a certain value
     */
    public int[] randomCell(boolean val) { return randomCells(val,1)[0]; }
    public int[][] randomCells(boolean val, int n) {
        int[][] results = new int[n][2];
        ArrayList<int[]> cells = new ArrayList<>();
        for (int x=0;x<xsize;x++)
            for (int y=0;y<ysize;y++)
                if (value(x,y) == val)
                    cells.add(new int[]{x,y});
        while (n > 0 && cells.size() > 0) {
            n--;
            int i = random.i(cells.size());
            results[n] = cells.get(i);
            cells.remove(i);
        }
        return results;
    }

    /**
     * Are all true cells connected in a single contiguous region?
     */
    public boolean isContiguous() {
        backupToBuffer();
        boolean filledOne = false;
        for (int x=0;x<xsize;x++)
            for (int y=0;y<ysize;y++)
                if (value(x,y)) {
                    if (filledOne) {
                        printBuffer();
                        return false;
                    } else {
                        flood(x,y,false);
                        filledOne = true;
                    }
                }
        printBuffer();
        return true;
    }

    /**
     * Find all contiguous self-contained regions
     */
    public ArrayList<Shape> regions() {
        backupToBuffer();
        ArrayList<Shape> regions = new ArrayList<>();
        for (int x=0;x<xsize;x++) {
            for (int y=0;y<ysize;y++) {
                if (value(x,y)) {
                    Shape region = copy();
                    region.flood(x,y,false);
                    region.maskWith(this, MASK_XOR);
                    maskWith(region, MASK_AND);
                    regions.add(region);
                }
            }
        }
        printBuffer();
        return regions;
    }

    /**
     * Flood-fill at x,y with val
     */
    public Shape flood(int x, int y, boolean val) {
        if (value(x,y) == val) return this;
        LinkedList<int[]> q = new LinkedList<>();
        q.push(new int[]{x,y});
        while (!q.isEmpty()) {
            int[] n = q.pop();
            int wx = n[0];
            int ex = n[0];
            while (value(wx,n[1]) != val) { wx--; }
            while (value(ex,n[1]) != val) { ex++; }
            for (int i=wx+1;i<ex;i++) {
                write(i,n[1],val);
                if (value(i,n[1]-1) != val) q.push(new int[]{i,n[1]-1});
                if (value(i,n[1]+1) != val) q.push(new int[]{i,n[1]+1});
            }
        }
        return this;
    }

    /**
     * Count how many cells a flood at x,y would fill -- the size of the contiguous space
     */
    public int floodCount(int x, int y, boolean val) {
        int c = 0;
        if (value(x,y) == val) return 0;
        backupToBuffer();
        LinkedList<int[]> q = new LinkedList<>();
        q.push(new int[]{x,y});
        while (!q.isEmpty()) {
            int[] n = q.pop();
            int wx = n[0];
            int ex = n[0];
            while (valueBuffer(wx,n[1]) != val) { wx--; }
            while (valueBuffer(ex,n[1]) != val) { ex++; }
            for (int i=wx+1;i<ex;i++) {
                writeBuffer(i,n[1],val);
                c++;
                if (valueBuffer(i,n[1]-1) != val) q.push(new int[]{i,n[1]-1});
                if (valueBuffer(i,n[1]+1) != val) q.push(new int[]{i,n[1]+1});
            }
        }
        return c;
    }

    public boolean tryToFitRoom(int x, int y, int width, int height, float angle, int displace, boolean draw) {
        int dxy = (int)Math.rint(Math.cos(angle));
        int dyy = (int)Math.rint(Math.sin(angle));
        int dxx = (int)Math.rint(Math.cos(angle+1.5708f));
        int dyx = (int)Math.rint(Math.sin(angle+1.5708f));
        width += 2; height += 2;
        float x1 = x + displace*dxy;
        float y1 = y + displace*dyy;
        x1 -= (dxx * (width/2) + dxy);
        y1 -= (dyx * (width/2) + dyy);
        boolean blocked = false;
        for (int i=0;i<width;i++) {
            for (int j=0;j<height;j++) {
                float cx = x1 + dxx*i + dxy*j;
                float cy = y1 + dyx*i + dyy*j;
                if (value((int)(cx),(int)cy)) {
                    blocked = true;
                }
            }
        }
        if (draw && !blocked ) {
            x1 += dxx + dyx;
            y1 += dxy + dyy;
            for (int i=0;i<width-2;i++) {
                for (int j=0;j<height-2;j++) {
                    float cx = x1 + dxx*i + dxy*j;
                    float cy = y1 + dyx*i + dyy*j;
                    set((int)cx,(int)cy);
                }
            }
            set(x + dxy,y+dyy);
        }
        return !blocked;
    }

}

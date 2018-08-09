package ure.editors.glyphed;

import ure.commands.UCommand;
import ure.math.UColor;
import ure.sys.GLKey;
import ure.terrain.UTerrain;
import ure.ui.Icons.Icon;
import ure.ui.Icons.UIconCzar;
import ure.ui.modals.UModal;

import javax.inject.Inject;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;

public class GlyphedModal extends UModal {

    int gridspacex = 0;
    int gridspacey = 0;
    int gridposx = 1;
    int gridposy = 7;

    int meterx = 20;
    int metery = 8;

    String selection;
    Icon selectedIcon;
    int glyphType = 0;
    static int TERRAIN = 0;
    static int THING = 1;
    static int ACTOR = 2;
    int pageOffset = 0;

    Set<String> thingNames;
    Set<String> actorNames;
    Set<String> terrainNames;
    ArrayList<Icon> thingIcons;
    ArrayList<Icon> actorIcons;
    ArrayList<Icon> terrainIcons;

    ArrayList<UTerrain> terrains;
    int refTerrain = 0;
    Icon refIcon;

    int currentAscii = 2;
    int cursorAscii = 0;

    UColor editColor;

    public GlyphedModal() {
        super(null, "", null);
        setDimensions(44,36);
        terrains = terrainCzar.getAllTerrainTemplates();
        makeRefIcon();
        thingNames = thingCzar.getAllThings();
        actorNames = actorCzar.getAllActors();
        terrainNames = terrainCzar.getAllTerrains();
        fillIconLists();
        selectIcon(0);
    }

    void makeRefIcon() {
        UTerrain t = terrains.get(refTerrain);
        UTerrain t2 = t.makeClone();
        t2.initializeAsCloneFrom(t);
        refIcon = t2.getIcon();
    }

    void fillIconLists() {
        thingIcons = new ArrayList<>();
        for (String name : thingNames) {
            Icon icon = iconCzar.getIconByName(name);
            if (icon == null) {
                icon = new Icon("blank");
                icon.setName(name);
            }
            thingIcons.add(icon);
        }
        actorIcons = new ArrayList<>();
        for (String name : actorNames) {
            Icon icon = iconCzar.getIconByName(name);
            if (icon == null) {
                icon = new Icon("blank");
                icon.setName(name);
            } else {
                System.out.println("GLYPHED: loaded existing icon " + name);
            }
            actorIcons.add(icon);
        }
        terrainIcons = new ArrayList<>();
        for (String name : terrainNames) {
            Icon icon = iconCzar.getIconByName(name);
            if (icon == null) {
                icon = new Icon("blank");
                icon.setName(name);
            }
            terrainIcons.add(icon);
        }
    }

    ArrayList<Icon> currentIconSet() {
        if (glyphType == TERRAIN)
            return terrainIcons;
        else if (glyphType == THING)
            return thingIcons;
        else
            return actorIcons;
    }

    void selectIcon(int i) {
        selectedIcon = currentIconSet().get(i);
        selection = selectedIcon.getName();
        currentAscii = UnicodeToCp437((int)selectedIcon.getGlyph());
        editColor = selectedIcon.fgColor;
        if (editColor == null) {
            selectedIcon.fgColor = new UColor(1f,1f,1f,1f);
            editColor = selectedIcon.fgColor;
        }
    }

    @Override
    public void mouseClick() {
        updateMouseGrid();
        if (mousex >= gridposx && mousex < (gridposx+16)) {
            if (mousey >= gridposy && mousey < (gridposy + 16)) {
                currentAscii = cursorAscii;
            }
        }
        if (mousex >= meterx && mousex <= meterx+13) {
            if (mousey >= metery && mousey <= (metery+8)) {
                int meteri = (mousex-meterx);

                int mousepy = commander.mouseY() - (ypos+metery*gh());
                float level = (float)mousepy / ((float)8*gh());
                level = 1f-level;
                System.out.println(Float.toString(level));
                if (meteri == 0)
                    editColor.setR(level);
                else if (meteri == 2)
                    editColor.setG(level);
                else if (meteri == 4)
                    editColor.setB(level);
            }
        }
        if (mousey >= 1 && mousey <= 3) {
            if (mousex >= 23 && mousex <= 29) {
                glyphType = mousey - 1;
            }
        }
        if (mousex >= 31 && mousex <= 40) {
            if (mousey >= 8 && mousey <= 28) {
                selectIcon(mousey-8);
            }
        }
    }

    @Override
    public void hearCommand(UCommand command, GLKey k) {
        if (k.k == GLFW_KEY_PAGE_UP) {
            refTerrain--;
            if (refTerrain < 0)
                refTerrain = terrains.size()-1;
            makeRefIcon();
        } else if (k.k == GLFW_KEY_PAGE_DOWN) {
            refTerrain++;
            if (refTerrain >= terrains.size())
                refTerrain = 0;
            makeRefIcon();
        }
    }

    @Override
    public void drawContent() {

        drawString("pgUp/pgDn: change terrain bg    ", 1, 34, UColor.COLOR_GRAY);
        updateMouseGrid();

        int u = cp437toUnicode(currentAscii);
        drawString("CP437 ASCII:", 33, 0, UColor.COLOR_GRAY);
        drawString(Integer.toString(currentAscii), 33, 1, UColor.COLOR_YELLOW);
        drawString(Integer.toString(u), 33, 3, UColor.COLOR_YELLOW);
        drawString("Unicode int:", 33,2,UColor.COLOR_GRAY);

        renderer.drawRect(1 * gw() + xpos, 1 * gh() + ypos, 3 * gw(), 3 * gh(), UColor.COLOR_BLACK);
        drawIcon(selectedIcon, 2, 2);
        for (int i=0;i<4;i++) {
            for (int x=0;x<3;x++) {
                for (int y=0;y<3;y++) {
                    drawIcon(refIcon, 5+x+(i*4), 1+y);
                }
            }
        }

        if (glyphType == TERRAIN) {
            drawIcon(selectedIcon, 6,1);
            drawIcon(selectedIcon,6,2);
            drawIcon(selectedIcon,6,3);
            drawIcon(selectedIcon,9,2);
            drawIcon(selectedIcon,10,2);
            drawIcon(selectedIcon,11,2);
            drawIcon(selectedIcon,13,1);
            drawIcon(selectedIcon,14,1);
            drawIcon(selectedIcon,15, 1);
            drawIcon(selectedIcon,13,2);
            drawIcon(selectedIcon,14,2);
            drawIcon(selectedIcon,15,2);
            drawIcon(selectedIcon,13,3);
            drawIcon(selectedIcon,14,3);
            drawIcon(selectedIcon,15,3);
            drawIcon(selectedIcon,17,3);
            drawIcon(selectedIcon,18,3);
            drawIcon(selectedIcon,19,3);
            drawIcon(selectedIcon,18,2);
            drawIcon(selectedIcon,19,2);
            drawIcon(selectedIcon,19,1);
        } else {
            drawIcon(selectedIcon,6,2);
            drawIcon(selectedIcon,9,1);
            drawIcon(selectedIcon,10,2);
            drawIcon(selectedIcon,11,2);
            drawIcon(selectedIcon,11,1);
            drawIcon(selectedIcon,10,3);
            drawIcon(selectedIcon,13,1);
            drawIcon(selectedIcon,15,3);
            drawIcon(selectedIcon,17,1);
            drawIcon(selectedIcon,19,1);
            drawIcon(selectedIcon,18,2);
            drawIcon(selectedIcon,17,3);
            drawIcon(selectedIcon,19,3);
        }

        drawString("terrain", 23, 1, glyphType == TERRAIN ? null : UColor.COLOR_GRAY);
        drawString("thing", 23,2, glyphType == THING ? null : UColor.COLOR_GRAY);
        drawString("actor", 23, 3, glyphType == ACTOR ? null : UColor.COLOR_GRAY);

        for (int x=0;x<16;x++) {
            for (int y=0;y<16;y++) {
                int ascii = x+y*16;
                int unicode = cp437toUnicode(ascii);
                if (ascii == currentAscii)
                    renderer.drawRect((gridposx+x)*(gw()+gridspacex)+xpos, (gridposy+y)*(gh() + gridspacey)+ypos, gw(), gh(), UColor.COLOR_BLUE);
                else if (ascii == cursorAscii)
                    renderer.drawRect((gridposx+x)*(gw()+gridspacex)+xpos, (gridposy+y)*(gh() + gridspacey)+ypos, gw(), gh(), UColor.COLOR_YELLOW);
                renderer.drawTile(unicode, (gridposx+x)*(gw()+gridspacex)+xpos, (gridposy+y)*(gh() + gridspacey)+ypos, UColor.COLOR_LIGHTRED);
            }
        }

        for (int i=0;i<8;i++) {
            drawTile('|', meterx, metery+i, editColor, meter(editColor.fR(), 8-i, 8) ? UColor.COLOR_RED : UColor.COLOR_DARKGRAY);
            drawTile('|', meterx+2, metery+i, editColor, meter(editColor.fG(), 8-i, 8) ? UColor.COLOR_GREEN : UColor.COLOR_DARKGRAY);
            drawTile('|', meterx+4, metery+i, editColor, meter(editColor.fB(), 8-i, 8) ? UColor.COLOR_BLUE : UColor.COLOR_DARKGRAY);
        }
        drawString("fgColor",21,19,null);
        drawTile(0,19,19,null, editColor);
        drawString(Integer.toString(editColor.iR()), 20, 17, UColor.COLOR_YELLOW);
        drawString(Integer.toString(editColor.iG()), 22, 17, UColor.COLOR_YELLOW);
        drawString(Integer.toString(editColor.iB()), 24, 17, UColor.COLOR_YELLOW);

        for (int i=0;i<20;i++) {
            ArrayList<Icon> iconset;
            if (glyphType == TERRAIN)
                iconset = terrainIcons;
            else if (glyphType == THING)
                iconset = thingIcons;
            else
                iconset = actorIcons;
            if (i+pageOffset < iconset.size()) {
                Icon icon = iconset.get(i + pageOffset);
                drawIcon(icon, 31, 8 + i);
                if (icon.getName().equals(selection))
                    drawString(icon.getName(), 33, 8+i,UColor.COLOR_YELLOW);
                else
                    drawString(icon.getName(), 33, 8 + i, UColor.COLOR_GRAY);
            }
        }
    }

    boolean meter(float level, int cell, int maxcell) {
        if (level == 0f) return false;
        if (level >= ((float)cell/(float)maxcell))
            return true;
        return false;
    }

    void updateMouseGrid() {
        if (mousex >= gridposx && mousex < (gridposx+16)) {
            if (mousey >= gridposy && mousey < (gridposy+16)) {
                cursorAscii = (mousex - gridposx) + (mousey - gridposy)*16;
            }
        }
    }

    public void drawTile(int u, int x, int y, UColor color, UColor bgcolor) {
        if (bgcolor != null)
            renderer.drawRect(x*gw()+xpos,y*gh()+ypos,gw(),gh(), bgcolor);
        renderer.drawTile(u, x*gw()+xpos,y*gh()+ypos,color);
    }
    public void drawTile(int u, int x, int y, UColor color) { drawTile(u,x,y,color,null); }

    public void drawTile(int u, int x, int y, UColor color, boolean actor) {
        int pixy = y*gh()+ypos;
        if (actor && commander.config.getActorBounceAmount() > 0f) {
            pixy = y*gh()+ypos-(int)(Math.abs(Math.sin((commander.frameCounter+x*4+y*5)*commander.config.getActorBounceSpeed()*0.1f))*commander.config.getActorBounceAmount()*5f);
        }
        if (actor && commander.config.isOutlineActors())
            renderer.drawTileOutline(u, x*gw()+xpos,pixy,UColor.COLOR_BLACK);
        if (actor && commander.config.getActorBounceAmount() > 0f) {
            renderer.drawTile(u,x*gw()+xpos, pixy, color);
        } else {
            drawTile(u, x, y, color);
        }
    }

    public int cp437toUnicode(int ascii) {
        int[] lookup = {
                0, 9786, 9787, 9829, 9830, 9827, 9824,
                8226, 9688, 9675, 9689, 9794, 9792, 9834, 9835,
                9788, 9658, 9668, 8597, 8252, 182, 167, 9644,
                8616, 8593, 8595, 8594, 8592, 8735, 8596, 9650,
                9660, 32, 33, 34, 35, 36, 37, 38,
                39, 40, 41, 42, 43, 44, 45, 46,
                47, 48, 49, 50, 51, 52, 53, 54,
                55, 56, 57, 58, 59, 60, 61, 62,
                63, 64, 65, 66, 67, 68, 69, 70,
                71, 72, 73, 74, 75, 76, 77, 78,
                79, 80, 81, 82, 83, 84, 85, 86,
                87, 88, 89, 90, 91, 92, 93, 94,
                95, 96, 97, 98, 99, 100, 101, 102,
                103, 104, 105, 106, 107, 108, 109, 110,
                111, 112, 113, 114, 115, 116, 117, 118,
                119, 120, 121, 122, 123, 124, 125, 126,
                8962, 199, 252, 233, 226, 228, 224, 229,
                231, 234, 235, 232, 239, 238, 236, 196,
                197, 201, 230, 198, 244, 246, 242, 251,
                249, 255, 214, 220, 162, 163, 165, 8359,
                402, 225, 237, 243, 250, 241, 209, 170,
                186, 191, 8976, 172, 189, 188, 161, 171,
                187, 9617, 9618, 9619, 9474, 9508, 9569, 9570,
                9558, 9557, 9571, 9553, 9559, 9565, 9564, 9563,
                9488, 9492, 9524, 9516, 9500, 9472, 9532, 9566,
                9567, 9562, 9556, 9577, 9574, 9568, 9552, 9580,
                9575, 9576, 9572, 9573, 9561, 9560, 9554, 9555,
                9579, 9578, 9496, 9484, 9608, 9604, 9612, 9616,
                9600, 945, 223, 915, 960, 931, 963, 181,
                964, 934, 920, 937, 948, 8734, 966, 949,
                8745, 8801, 177, 8805, 8804, 8992, 8993, 247,
                8776, 176, 8729, 183, 8730, 8319, 178, 9632,
                160
        };
        ascii = ascii % 256;
        return lookup[ascii];
    }
    public int UnicodeToCp437(int u) {
        for (int i=0;i<256;i++) {
            if (cp437toUnicode(i) == u) {
                return i;
            }
        }
        return 0;
    }
}

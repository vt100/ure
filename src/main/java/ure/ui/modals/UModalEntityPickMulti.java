package ure.ui.modals;

import ure.commands.UCommand;
import ure.math.UColor;
import ure.sys.Entity;
import ure.sys.GLKey;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class UModalEntityPickMulti extends UModal {

    String[] prompt;
    UColor bgColor, hiliteColor;
    int xpad,ypad;
    boolean showDetail;
    boolean escapable;
    int selection = 0;
    int listWidth = 0;
    int width = 0;
    int height = 0;
    ArrayList<Entity> entities;
    ArrayList<Boolean> selectedEntities;

    public UModalEntityPickMulti(String _prompt, UColor _bgColor, int _xpad, int _ypad, ArrayList<Entity> _entities, boolean _showDetail, boolean _escapable, HearModalEntityPickMulti _callback, String _callbackContext) {
        super(_callback, _callbackContext, _bgColor);
        prompt = splitLines(_prompt);
        xpad = _xpad;
        ypad = _ypad;
        entities = _entities;
        showDetail = _showDetail;
        escapable = _escapable;
        selectedEntities = new ArrayList<>();
        for (int i=0;i<entities.size();i++) {
            selectedEntities.add(false);
            String n = entities.get(i).getName();
            int len = textWidth(n);
            if (len > listWidth) listWidth = len;
        }

        height = Math.max(entities.size() + prompt.length + 1, (showDetail ? 7 : 2));
        if (prompt != null) {
            height += prompt.length;
            for (String line : prompt) {
                if (line.length()/2 > listWidth)
                    listWidth = line.length()/2;
            }
        }
        width = listWidth + 1 + (showDetail ? 10 : 0);
        width = Math.max(width, longestLine(prompt));
        setDimensions(width + xpad, height + ypad);
        if (bgColor == null)
            bgColor = config.getModalBgColor();
        setBgColor(bgColor);
        hiliteColor = config.getHiliteColor();
    }

    @Override
    public void drawContent() {
        selection = mouseToSelection(entities.size(), prompt.length+1, selection);
        drawStrings(prompt, xpad, ypad);
        int y = 0;
        for (Entity entity: entities) {
            int liney = y+prompt.length+1;
            drawIcon(entity.icon(), xpad+1, liney+ypad);
            String n = entity.getName();
            UColor textColor = UColor.GRAY;
            if (selectedEntities.get(y)) {
                textColor = null;
                drawTile(config.getUiCheckGlyph().charAt(0), xpad+2, liney+ypad, hiliteColor);
            }
            drawString(n, xpad+3, liney+ypad, textColor, (y == selection) ? hiliteColor : null);
            y++;
        }
        if (showDetail) {
            showDetail(entities.get(selection), listWidth+1+xpad, prompt.length+ypad+1);
        }
    }

    @Override
    public void hearCommand(UCommand command, GLKey k) {
        if (command != null) {
            if (command.id.equals("MOVE_N")) {
                selection = cursorMove(selection, -1, entities.size());
            } else if (command.id.equals("MOVE_S")) {
                selection = cursorMove(selection, 1, entities.size());
            } else if (command.id.equals("PASS")) {
                selectEntity();
            } else if (command.id.equals("ESC") && escapable) {
                escape();
            }
        } else if (k.k == GLFW_KEY_ENTER || k.k == GLFW_KEY_KP_ENTER) {
            completeSelection();
        } else if (k.k == GLFW_KEY_A && k.shift) {
            selectAll();
        }
    }
    @Override
    public void mouseClick() { selectEntity(); }

    void selectEntity() {
        selectedEntities.set(selection, !selectedEntities.get(selection));
    }

    void completeSelection() {
        dismiss();
        ArrayList<Entity> selected = new ArrayList<>();
        int i = 0;
        for (Entity entity : entities) {
            if (selectedEntities.get(i))
                selected.add(entity);
            i++;
        }
        ((HearModalEntityPickMulti)callback).hearModalEntityPickMulti(callbackContext, selected);
    }

    void selectAll() {
        boolean allAlready = true;
        for (int i = 0;i<selectedEntities.size();i++) {
            if (!selectedEntities.get(i))
                allAlready = false;
        }
        for (int i = 0;i<selectedEntities.size();i++) {
            selectedEntities.set(i, allAlready ? false : true);
        }
    }
}

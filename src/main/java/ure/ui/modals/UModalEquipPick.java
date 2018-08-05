package ure.ui.modals;

import ure.commands.UCommand;
import ure.math.UColor;
import ure.render.URenderer;
import ure.sys.GLKey;
import ure.things.UThing;

import java.util.ArrayList;

public class UModalEquipPick extends UModal {

    UColor bgColor;
    int xpad, ypad;
    ArrayList<UThing> things;
    UThing equipped;
    boolean showDetail;
    boolean escapable;
    int textWidth = 0;
    int selection = 0;
    UColor tempHiliteColor, flashColor;

    public UModalEquipPick(UColor _bgColor, int _xpad, int _ypad, ArrayList<UThing> _things, UThing _equipped, boolean _showDetail, boolean _escapable, HearModalEquipPick _callback, String _callbackContext) {
        super(_callback, _callbackContext, _bgColor);
        xpad = _xpad;
        ypad = _ypad;
        things = _things;
        equipped = _equipped;
        showDetail = _showDetail;
        escapable = _escapable;
        textWidth = 12;
        width = textWidth;
        int height = Math.max(5, things.size() + 2);
        if (showDetail) {
            width += 9;
            height += 5;
        }
        setDimensions(width + 2 + xpad, height);
        if (bgColor == null)
            bgColor = commander.config.getModalBgColor();
        setBgColor(bgColor);
        tempHiliteColor = commander.config.getHiliteColor();
        flashColor = new UColor(commander.config.getHiliteColor());
        flashColor.setAlpha(1f);
        dismissFrameEnd = 8;
        int i = 0;
        for (UThing thing : things) {
            if (thing == equipped) {
                selection = i;
            }
            i++;
        }
    }

    @Override
    public void drawContent(URenderer renderer) {
        int y = 0;
        for (UThing thing : things) {
            String n;
            if (thing == null) {
                n = "<nothing>";
            } else {
                drawIcon(renderer, thing.getIcon(), 1, y + ypad);
                n = thing.getName();
            }
            if (thing == equipped)
                drawGlyph(renderer, commander.config.getUiCheckGlyph().charAt(0), 2, y+ypad, commander.config.getHiliteColor());
            drawString(renderer, n, 3, y+ypad, (y == selection || thing == equipped )? null : UColor.COLOR_GRAY, (y == selection) ? tempHiliteColor : null);
            y++;
        }
        if (showDetail) {
            showDetail(renderer, equipped, xpad+textWidth, ypad);
            showDetail(renderer, things.get(selection), xpad+textWidth, ypad+5);
        }
    }

    @Override
    public void hearCommand(UCommand command, GLKey k) {
        if (command == null) return;
        if (command.id.equals("MOVE_N")) {
            selection = cursorMove(selection, -1, things.size());
        } else if (command.id.equals("MOVE_S")) {
            selection = cursorMove(selection, 1, things.size());
        } else if (command.id.equals("PASS")) {
            selectEquip();
        } else if (command.id.equals("ESC") && escapable) {
            escape();
        }
    }

    void selectEquip() {
        dismiss();
        UThing thing = things.get(selection);
        if (thing == equipped)
            dismissFrameEnd = 0;
        if (thing != null) {
            ((HearModalEquipPick) callback).hearModalEquipPick(callbackContext, things.get(selection));
        } else {
            ((HearModalEquipPick) callback).hearModalEquipPick("unequip", equipped);
        }
    }

    @Override
    public void animationTick() {
        if (dismissed) {
            if ((dismissFrames % 2) == 0) {
                tempHiliteColor = commander.config.getModalBgColor();
            } else {
                tempHiliteColor = flashColor;
            }
        }
        super.animationTick();
    }
}

package ure.ui.modals;

import ure.commands.UCommand;
import ure.math.UColor;
import ure.sys.GLKey;

public class UModalLoading extends UModal {

    String text;
    int xpad,ypad;

    public UModalLoading() {
        super(null, "", UColor.BLACK);
        this.text = "Loading...";
        this.xpad = 1;
        this.ypad = 1;
        setDimensions(text.length() + xpad, ypad + 1);
        setBgColor(commander.config.getModalBgColor());
    }

    @Override
    public void drawContent() {
        renderer.drawString(relx(xpad/2), rely(ypad/2), commander.config.getTextColor(), text);
    }

    @Override
    public void hearCommand(UCommand command, GLKey k) {

    }
}

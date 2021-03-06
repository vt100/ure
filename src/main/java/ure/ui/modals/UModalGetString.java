package ure.ui.modals;

import ure.commands.UCommand;
import ure.math.UColor;
import ure.render.URenderer;
import ure.sys.GLKey;

import static org.lwjgl.glfw.GLFW.*;

public class UModalGetString extends UModal {

    String[] prompt;
    String input;
    boolean escapable;
    int maxlength;
    int fieldY;

    int blinkCount = 0;
    boolean blunk = true;

    UColor fieldColor;
    UColor cursorColor;

    public UModalGetString(String _prompt, int _maxlength, boolean _escapable,
                           UColor bgColor, HearModalGetString _callback, String _callbackContext) {
        super(_callback, _callbackContext, bgColor);
        prompt = splitLines(_prompt);
        escapable = _escapable;
        input = "";
        maxlength = _maxlength;
        int width = longestLine(prompt);
        if (maxlength > width)
            width = maxlength;
        int height = 2;
        if (prompt != null)
            height += prompt.length;
        setDimensions(width, height);
        if (prompt == null)
            fieldY = 0;
        else
            fieldY = prompt.length + 1;

        fieldColor = new UColor(config.getHiliteColor());
        cursorColor = new UColor(config.getHiliteColor());
        fieldColor.setAlpha(0.2f);
        cursorColor.setAlpha(1f);
    }

    @Override
    public void drawContent() {
        renderer.setFont(URenderer.FontType.TEXT_FONT);
        drawStrings(prompt, 0, 0);
        renderer.drawRect(0 + xpos, fieldY * gh() + ypos, maxlength*gw(), gh(), fieldColor);
        drawString(input, 0, fieldY);
        if (blunk) {
            renderer.drawRect(xpos + renderer.stringWidth(input), fieldY * gh() + ypos + (gh()/2), config.getTextWidth(), gh()/2, cursorColor);
        }
        renderer.setFont(URenderer.FontType.TILE_FONT);
    }

    @Override
    public void animationTick() {
        super.animationTick();
        blinkCount++;
        if (blinkCount > config.getCursorBlinkSpeed()) {
            blinkCount = 0;
            blunk = !blunk;
        }
    }

    @Override
    public void hearCommand(UCommand command, GLKey k) {
        if (command != null) {
            if (command.id.equals("ESC") && escapable)
                escape();
        }
        if (k.k == GLFW_KEY_BACKSPACE) {
            if (!input.isEmpty()) {
                if (input.length() == 1)
                    input = "";
                else
                    input = input.substring(0, input.length() - 1);
                speaker.playUI(config.soundKeystroke);
            } else
                speaker.playUI(config.soundBumpLimit);
        } else if (k.k == GLFW_KEY_ENTER) {
            sendInput();
        } else {
            String typed = k.typed();
            if (typed != null) {
                input = input + typed;
                speaker.playUI(config.soundKeystroke);
            }
        }
    }

    public void sendInput() {
        dismiss();
        ((HearModalGetString)callback).hearModalGetString(callbackContext, input);
    }
}

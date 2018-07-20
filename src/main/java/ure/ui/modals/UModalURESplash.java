package ure.ui.modals;

import ure.commands.UCommand;
import ure.math.UColor;
import ure.render.URenderer;
import ure.sys.GLKey;
import ure.ui.RexFile;

public class UModalURESplash extends UModal {

    RexFile logo;
    float alpha;
    int waitTick = 0;
    int waitMax = 180;
    float fadetick = 0.02f;

    public UModalURESplash() {
        super(null, "", UColor.COLOR_BLACK);
        logo = new RexFile("src/main/resources/ure_logo.xp");
        alpha = 0f;
    }

    @Override
    public void hearCommand(UCommand command, GLKey k) {
        waitTick = 10000;
        alpha = Math.min(alpha, 0.7f);
        fadetick = 0.06f;
    }

    @Override
    public void draw(URenderer renderer) {
        logo.draw(renderer, alpha);
    }

    @Override
    public void animationTick() {
        super.animationTick();
        if (waitTick < waitMax) {
            alpha += fadetick;
        }
        if (alpha >= 1f) {
            alpha = 1f;
        }
        waitTick++;
        if (waitTick > waitMax) {
            alpha -= fadetick;
            if (alpha <= 0f) {
                dismiss();
            }
        }
    }
}

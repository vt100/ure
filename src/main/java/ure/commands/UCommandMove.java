package ure.commands;

import ure.actions.ActionWalk;
import ure.actors.UPlayer;

public abstract class UCommandMove extends UCommand {

    public int xdir, ydir;
    boolean latch;

    public UCommandMove(int _xdir, int _ydir, boolean _latch) {
        super();
        xdir = _xdir;
        ydir = _ydir;
        latch = _latch;
    }

    @Override
    public void execute(UPlayer player) {
        if (latch)
            commander.setMoveLatch(xdir, ydir);
        player.doAction(new ActionWalk(player, xdir, ydir));
    }
}

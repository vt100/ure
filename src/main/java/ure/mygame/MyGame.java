package ure.mygame;

import ure.actors.UActorCzar;
import ure.actors.UPlayer;
import ure.areas.UArea;
import ure.areas.UCartographer;
import ure.areas.UCell;
import ure.examplegame.ExampleCartographer;
import ure.math.UColor;
import ure.render.URenderer;
import ure.sys.Injector;
import ure.sys.UCommander;
import ure.sys.UConfig;
import ure.sys.UREGame;
import ure.terrain.UTerrainCzar;
import ure.things.UThingCzar;
import ure.ui.UCamera;
import ure.ui.View;
import ure.ui.panels.UActorPanel;
import ure.ui.panels.UScrollPanel;
import ure.ui.panels.UStatusPanel;

import javax.inject.Inject;

public class MyGame implements UREGame {

    static UArea area;
    static UCamera camera;
    static UPlayer player;
    static UStatusPanel statusPanel;
    static UScrollPanel scrollPanel;

    @Inject
    public URenderer renderer;
    @Inject
    public UCommander commander;
    @Inject
    public UConfig config;
    @Inject
    public UTerrainCzar terrainCzar;
    @Inject
    public UThingCzar thingCzar;
    @Inject
    public UActorCzar actorCzar;
    @Inject
    public UCartographer cartographer;

    public MyGame() { Injector.getAppComponent().inject(this); }

    public void startUp() {
        cartographer = new ExampleCartographer();
        commander.registerComponents(this, player, renderer, thingCzar, actorCzar, cartographer);

        makeWindow();

        commander.registerScrollPrinter(scrollPanel);
        commander.addAnimator(camera);

        player = makeNewPlayer();
        cartographer.startLoader();
        area = cartographer.makeStartArea();
        UCell startcell = area.randomOpenCell(player);
        player.setSaveLocation(area, startcell.x, startcell.y);

        commander.startGame(player, area);
        player.attachCamera(camera, config.getCameraPinStyle());
        commander.gameLoop();
    }

    public void setupTitleScreen() {
        ;
    }

    private void makeWindow() {
        View rootView = new View();
        camera = new UCamera(0,0,1200,800);
        camera.moveTo(area, 40, 20);
        rootView.addChild(camera);

    }

    private UPlayer makeNewPlayer() {
        player = new UPlayer("Player", UColor.WHITE, 4, 4);
        player.setID(commander.generateNewID(player));
        return player;
    }
}

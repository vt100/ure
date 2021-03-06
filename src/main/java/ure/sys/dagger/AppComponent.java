package ure.sys.dagger;

import dagger.Component;
import ure.areas.*;
import ure.mygame.MyGame;
import ure.render.URendererOGL;
import ure.sys.UCommander;
import ure.actors.actions.UAction;
import ure.actors.UActorCzar;
import ure.actors.behaviors.UBehavior;
import ure.commands.UCommand;
import ure.examplegame.ExampleGame;
import ure.terrain.Stairs;
import ure.terrain.UTerrain;
import ure.terrain.UTerrainCzar;
import ure.things.SpawnItem;
import ure.things.UThing;
import ure.things.UThingCzar;
import ure.ui.Icons.Icon;
import ure.ui.Icons.UIconCzar;
import ure.ui.RexFile;
import ure.ui.UCamera;
import ure.ui.ULight;
import ure.ui.sounds.Sound;
import ure.ui.sounds.USpeaker;
import ure.ui.panels.ULensPanel;
import ure.ui.panels.UPanel;
import ure.ui.panels.UStatusPanel;
import ure.ui.modals.UModal;
import ure.ui.particles.UParticle;

import javax.inject.Singleton;

/**
 * The register for classes that need dependency injection.
 * If you add a class to this register, you'll also need to include
 * Injector.getAppComponent().inject(this);
 * in your constructors to receive the dependencies.  You can then add
 *
 * @Inject
 * UCommander commander
 *
 * (or other injected singletons) to your class and receive the global
 * instance.
 *
 */
@Singleton
@Component(modules =  { AppModule.class })
public interface AppComponent {
    void inject(UTerrainCzar czar);
    void inject(UThingCzar czar);
    void inject(UActorCzar czar);
    void inject(UIconCzar czar);
    void inject(UCartographer cartographer);
    void inject(UCommander cmdr);
    void inject(ExampleGame game);
    void inject(MyGame mygame);
    void inject(UAction act);
    void inject(UThing thingi);
    void inject(UArea uarea);
    void inject(UCamera cam);
    void inject(UCell cel);
    void inject(UTerrain terr);
    void inject(UBehavior behav);
    void inject(UModal mod);
    void inject(UCommand comm);
    void inject(URendererOGL rend);
    void inject(UStatusPanel statpan);
    void inject(ULensPanel lenspan);
    void inject(ULandscaper landscape);
    void inject(URegion reg);
    void inject(UPanel pan);
    void inject(USpeaker speak);
    void inject(Stairs stairs);
    void inject(UParticle parti);
    void inject(Icon icon);
    void inject(RexFile rexfile);
    void inject(Shape shape);
    void inject(SpawnItem item);
    void inject(ULight lit);
    void inject(Sound snd);
}

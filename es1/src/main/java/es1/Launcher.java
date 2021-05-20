package es1;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import es1.actors.Arbiter;
import es1.view.ViewFrame;

/**
 * Class that Launch the Game.
 */
public class Launcher {

    /**
     * Main of the Project.
     * @param args of the main.
     */
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("MySystem");
        ActorRef act = system.actorOf(Props.create(Arbiter.class));
        new ViewFrame(act);
    }
}

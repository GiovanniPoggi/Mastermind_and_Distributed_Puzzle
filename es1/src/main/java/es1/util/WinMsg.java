package es1.util;

import java.util.ArrayList;
import akka.actor.ActorRef;

/**
 * Class that manage the Win message.
 */
public class WinMsg {

    /**
     * Field that represents all results of the segret code of the game.
     */
    private final ArrayList<Pair<ActorRef, ArrayList<Integer>>> result;

    /**
     * Constructor of the Class.
     * @param result
     */
    public WinMsg(ArrayList<Pair<ActorRef, ArrayList<Integer>>> result) {
        this.result = result;
    }

    /**
     * Method that return the result of the Game.
     * @return result
     */
    public ArrayList<Pair<ActorRef, ArrayList<Integer>>> getResult() {
        return result;
    }
}

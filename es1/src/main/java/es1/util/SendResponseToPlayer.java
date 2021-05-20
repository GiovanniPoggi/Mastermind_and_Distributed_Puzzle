package es1.util;

import java.util.ArrayList;

/**
 * Class that manage the Response to Player from Another Player.
 */
public class SendResponseToPlayer {

    /**
     * Fields of the class that represents the attempt of the Player.
     */
    private final ArrayList<Integer> attempt;

    /**
     * Constructor of the Class.
     * @param attempt of the player.
     */
    public SendResponseToPlayer(ArrayList<Integer> attempt)
    {
        this.attempt = attempt;
    }

    /**
     * Method that returns the Attempt of the Player.
     * @return attempt of the player.
     */
    public ArrayList<Integer> getResult() {
        return attempt;
    }
}

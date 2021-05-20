package es1.util;

import java.util.ArrayList;

/**
 * Class that manage the Message for the Attempt of Player to Another Player.
 */
public class PlayerAttemptMsg {

    /**
     * Fields of the class that represents the attempt of the Player and result of the Attempt.
     */
    private final ArrayList<Integer> attempt;
    private final ArrayList<Integer> result;

    /**
     * Constructor of the class.
     * @param attempt sent.
     * @param result of the attempt.
     */
    public PlayerAttemptMsg(ArrayList<Integer> attempt, ArrayList<Integer> result) {
        this.attempt = attempt;
        this.result = result;
    }

    /**
     * Method to Get the Attempt of the Player.
     * @return attempt.
     */
    public ArrayList<Integer> getAttempt() {
        return attempt;
    }

    /**
     * Method to Get result of the Attempt of the Player.
     * @return result.
     */
    public ArrayList<Integer> getResult() {
        return result;
    }
}

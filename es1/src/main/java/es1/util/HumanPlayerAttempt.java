package es1.util;

import java.util.ArrayList;

/**
 * Class that manage the Human Player Attempt in the Game.
 */
public class HumanPlayerAttempt {

    /**
     * Fields that represents Attempt of Human Player and the Player to Send Attempt.
     */
    private final int playerToSend;
    private final ArrayList<Integer> attempt;

    /**
     * Constructor of the Class.
     * @param playerToSend the Attempt.
     * @param attempt to send.
     */
    public HumanPlayerAttempt(int playerToSend, ArrayList<Integer> attempt) {
        this.playerToSend = playerToSend;
        this.attempt = attempt;
    }

    /**
     * Method to Get the Player to send Attempt.
     * @return player to send attempt.
     */
    public int getPlayerToSend() {
        return playerToSend;
    }

    /**
     * Method to Get the Attempt sent to Player.
     * @return attempt sent.
     */
    public ArrayList<Integer> getAttempt() {
        return attempt;
    }
}

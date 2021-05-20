package es1.util;

import akka.actor.ActorRef;
import java.util.ArrayList;

/**
 * Class that manage the Attempt of all Players (Human or Not)
 */
public class TryAttempt {

    /**
     * Fields that manage players, eventually Human player and attempts.
     */
    private final ArrayList<ActorRef> players;
    private final boolean isHuman;
    private final ArrayList<Integer> attempt;
    private final int playerToSend;

    /**
     * Constructor of the Class
     * @param players - all players of the game.
     * @param isHuman - check if there's the Human player or not.
     * @param attempt - the attempt of the Player.
     * @param playerToSend - which Player to send the attempt.
     */
    public TryAttempt(ArrayList<ActorRef> players, boolean isHuman, ArrayList<Integer> attempt, int playerToSend) {
        this.players = players;
        this.isHuman = isHuman;
        this.attempt = attempt;
        this.playerToSend = playerToSend;
    }

    /**
     *
     * @return
     */
    public ArrayList<ActorRef> getPlayers() {
        return players;
    }

    /**
     * Method that checks if there's a Human Player in the Game.
     * @return true or false.
     */
    public boolean isHuman() {
        return isHuman;
    }

    /**
     * Method that returns the attempt of the User in the Game.
     * @return attempt
     */
    public ArrayList<Integer> getAttempt() {
        return attempt;
    }

    /**
     * Method that returns the Player to send the Attempt
     * @return player to send attempt
     */
    public int getPlayerToSend() {
        return playerToSend;
    }
}

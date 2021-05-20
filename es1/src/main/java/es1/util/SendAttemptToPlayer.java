package es1.util;

import akka.actor.ActorRef;
import java.util.ArrayList;

/**
 * Class that sends the Attempt of Player to Another Player.
 */
public class SendAttemptToPlayer {

    /**
     * Fields of the class that represents the attempt of the Player and the Player to send it.
     */
    private final ArrayList<ActorRef> players;
    private final ArrayList<Integer> attempt;

    /**
     * Constructor of the class.
     * @param players to send attempt.
     * @param attempt to send.
     */
    public SendAttemptToPlayer(ArrayList<ActorRef> players, ArrayList<Integer> attempt)
    {
        this.players = players;
        this.attempt = attempt;
    }

    /**
     * Method to Get Players.
     * @return players.
     */
    public ArrayList<ActorRef> getPlayers() {
        return players;
    }

    /**
     * Method to get Attempt to send.
     * @return attempt.
     */
    public ArrayList<Integer> getAttempt() {
        return attempt;
    }
}

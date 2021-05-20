package es1.util;

import es1.view.ViewFrame;
import java.util.ArrayList;

/**
 * Class that manage the Start message between Arbiter and Players.
 */
public class StartMsg {

    /**
     * Fields to use
     */
    private final int number;
    private final int players;
    private final ViewFrame viewFrame;
    private final boolean humanPlayer;
    private String playerName;
    private ArrayList<Integer> playerCode;

    /**
     * Constructor of the class if there isn't Human Player.
     * @param number - number of chars of secret code in the game.
     * @param players - number of Players in the game.
     * @param viewFrame - GUI of the game.
     */
    public StartMsg(int number, int players, ViewFrame viewFrame){
        this.number = number;
        this.players = players;
        this.viewFrame = viewFrame;
        this.humanPlayer = false;
    }

    /**
     * Constructor of the class if there's an Human Player.
     * @param number - number of chars of secret code in the game.
     * @param players - number of Players in the game.
     * @param viewFrame - GUI of the game.
     * @param playerName - Name of the Human Player.
     * @param playerCode - Secret code of the Human Player.
     */
    public StartMsg(int number, int players, ViewFrame viewFrame, String playerName, ArrayList<Integer> playerCode){
        this.number = number;
        this.players = players;
        this.viewFrame = viewFrame;
        this.humanPlayer = true;
        this.playerName = playerName;
        this.playerCode = playerCode;
    }

    /**
     * Method that return the number of the chars in the secret code of the Players.
     * @return numbers of chars in the secret code.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Method that return the number of the Players in the Game.
     * @return numbers of Players.
     */
    public int getPlayers() {
        return players;
    }

    /**
     * Method that return the GUI of the Game.
     * @return viewFrame.
     */
    public ViewFrame getViewFrame() {
        return viewFrame;
    }

    /**
     * Method that return if there's a Human Player or Not.
     * @return true or false.
     */
    public boolean isHumanPlayer() {
        return humanPlayer;
    }

    /**
     * Method that return the Name of the Human Player.
     * @return the name of the Human Player.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Method that return the secret code of the Human Player.
     * @return secret code of the Human Player.
     */
    public ArrayList<Integer> getPlayerCode() {
        return playerCode;
    }
}

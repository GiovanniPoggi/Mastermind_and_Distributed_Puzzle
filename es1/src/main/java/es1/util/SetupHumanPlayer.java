package es1.util;

import java.util.ArrayList;

/**
 * Class That Set Up Human Player.
 */
public class SetupHumanPlayer {

    /**
     * Field that represents the number of the Human Player and the single chars.
     */
    private final int number;
    private final ArrayList<Integer> code;

    /**
     * Constructor of the class.
     * @param number of the player.
     * @param code chars of the Secret Code.
     */
    public SetupHumanPlayer(int number, ArrayList<Integer> code) {
        this.number = number;
        this.code = code;
    }

    /**
     * Method that return the Number of the Player.
     * @return number of the player.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Method that return the Chars of the Secret Code of the Player.
     * @return chars of the Secret Code of the player.
     */
    public ArrayList<Integer> getCode() {
        return code;
    }
}

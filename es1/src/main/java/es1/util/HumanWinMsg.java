package es1.util;

import java.util.ArrayList;

/**
 * Class that manage the Win Message to Human Player.
 */
public class HumanWinMsg {

    /**
     * Fields that represents the result of Attempt.
     */
    private final ArrayList<Pair<Integer, ArrayList<Integer>>> result;

    /**
     * Constructor of the Class.
     * @param result of the Attempt.
     */
    public HumanWinMsg(ArrayList<Pair<Integer, ArrayList<Integer>>> result) {
        this.result = result;
    }

    /**
     * Method that Get the Result of the Attempt.
     * @return result of the Attempt.
     */
    public ArrayList<Pair<Integer, ArrayList<Integer>>> getResult() {
        return result;
    }
}

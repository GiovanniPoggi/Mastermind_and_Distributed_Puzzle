package es1.util;

import java.util.ArrayList;

/**
 * Class that manage the Secret Code of the Player in the Game.
 */
public class CodeGeneratedMsg {

    /**
     * Fields that represent the Secret Code of the Player.
     */
    private final ArrayList<Integer> code;

    /**
     * Constructor of the Class.
     * @param code of the Player.
     */
    public CodeGeneratedMsg(ArrayList<Integer> code) {
        this.code = code;
    }

    /**
     * Method that Get the Secret Code of the Player.
     * @return code of the Player.
     */
    public ArrayList<Integer> getCode() {
        return code;
    }
}

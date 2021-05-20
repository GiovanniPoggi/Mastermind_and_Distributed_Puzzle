package es1.util;

/**
 * Class That Set Up Player.
 */
public class SetupPlayer {

    /**
     * Field that represents the number of the Player.
     */
    private final int number;

    /**
     * Constructor of the class.
     * @param number of the player.
     */
    public SetupPlayer(int number) {
        this.number = number;
    }

    /**
     * Method that return the Number of the Player.
     * @return number of the player.
     */
    public int getNumber() {
        return number;
    }
}

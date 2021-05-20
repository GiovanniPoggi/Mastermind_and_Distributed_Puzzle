package puzzle.impl;

import java.awt.image.BufferedImage;

/**
 * Class that manage the Tile of all Images of the Puzzle.
 */
public class Tile implements Comparable<Tile> {

    /**
     * Fields that manage the Image, Current and Original Position of the Pieces of the Puzzle.
     */
	private final BufferedImage image;
	private final int originalPosition;
	private int currentPosition;

    /**
     * Constructor that creates Tile with all Images of the Puzzle, original position of all pieces and the current position of the pieces of the Puzzle.
     * @param image - image in the Game.
     * @param originalPosition - original position of the Pieces in the Puzzle.
     * @param currentPosition - current position of the Pieces in the Puzzle.
     */
    public Tile(final BufferedImage image, final int originalPosition, final int currentPosition) {
        this.image = image;
        this.originalPosition = originalPosition;
        this.currentPosition = currentPosition;
    }

    /**
     * Method that return the Image of the Puzzle.
     * @return the Image of the Puzzle.
     */
    public BufferedImage getImage() {
    	return image;
    }

    /**
     * Check if a piece is in the right position in the Puzzle.
     * @return true is OK or false if it isn't in the right place.
     */
    public boolean isInRightPlace() {
    	return currentPosition == originalPosition;
    }

    /**
     * Get position of Piece of Puzzle.
     * @return position.
     */
    public int getCurrentPosition() {
    	return currentPosition;
    }

    /**
     * Set the current position of Piece of Puzzle.
     * @param newPosition of the piece of Puzzle.
     */
    public void setCurrentPosition(final int newPosition) {
    	currentPosition = newPosition;
    }

    /**
     * Method that compare two pieces position of the Puzzle.
     * @param other - tile with the all position and pieces of Puzzle.
     * @return true of false.
     */
	@Override
	public int compareTo(Tile other) {
		return Integer.compare(this.currentPosition, other.currentPosition);
	}
}

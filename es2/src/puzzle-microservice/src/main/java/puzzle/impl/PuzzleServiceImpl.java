package puzzle.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import puzzle.PuzzleService;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Class that manages all operation of the Image in the Game.
 */
public class PuzzleServiceImpl  implements PuzzleService {

    /**
     * Fields that manage the Images and Settings of the Puzzle.
     */
    private final Vertx vertx;
    private int rows, columns;
    private final List<Tile> tiles = new ArrayList<>();
    private final ArrayList<String> users = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(PuzzleServiceImpl.class);

    /**
     * Constructor that set Vertx, Rows and Columns of the Puzzle.
     * @param vertx
     */
    public PuzzleServiceImpl(Vertx vertx) {
        this.rows = 0;
        this.columns = 0;
        this.vertx = vertx;
    }

    /**
     * Method that create a List with all pieces of puzzle.
     * @param imageURL - Image Url of Image.
     * @param col - Number of Column and Rows of Puzzle.
     * @param username - Username of the user that create a Game.
     * @param resultHandler - The result handler will be called as soon as the initialization has been accomplished.
     */
    @Override
    public void createTiles(String imageURL, int col, String username, Handler<AsyncResult<JsonObject>> resultHandler) {
        this.rows = col;
        this.columns = col;
        BufferedImage image = null;

        tiles.clear();
        users.clear();

        users.add(username);

        try {
            logger.info("Trying to load the image from the URL...");
            URL url = new URL(imageURL);
            image = ImageIO.read(url);
        } catch (IOException ex) {
            logger.info("Could not load image");
            resultHandler.handle(Future.failedFuture(ex));
        }
        
        final int imageWidth = image.getWidth(null);
        final int imageHeight = image.getHeight(null);

        int position = 0;

        final List<Integer> randomPositions = new ArrayList<>();
        IntStream.range(0, rows*columns).forEach(randomPositions::add);
        Collections.shuffle(randomPositions);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {

                BufferedImage bufferedImage = image.getSubimage(j * imageWidth / columns,
                        i * imageHeight / rows,
                        (imageWidth / columns),
                        imageHeight / rows);

                tiles.add(new Tile(bufferedImage, position, randomPositions.get(position)));
                position++;
            }
        }

        JsonObject images = new JsonObject();
        
        for(int i = 0; i < tiles.size(); i++) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                ImageIO.write(tiles.get(i).getImage(), "jpg", output);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String imageBase64 = DatatypeConverter.printBase64Binary(output.toByteArray());
            
            images.put("image" + i, imageBase64);
            images.put("position" + i, tiles.get(i).getCurrentPosition());
        }

        resultHandler.handle(Future.succeededFuture(images));
    }

    /**
     * Method that Swap two pieces of the Puzzle.
     * @param t1 - First Piece of Puzzle.
     * @param t2 - Second Piece of Puzzle to Swap.
     * @param resultHandler - The result handler will be called as soon as the initialization has been accomplished.
     */
    public void swap(int t1, int t2, Handler<AsyncResult<Void>> resultHandler) {
        int tile1 = 0;
        int tile2 = 0;

        for(int i = 0; i < tiles.size(); i++){
            if(tiles.get(i).getCurrentPosition() == t1){
                tile1 = i;
            }else if (tiles.get(i).getCurrentPosition() == t2){
                tile2 = i;
            }
        }

        int tmp = tiles.get(tile1).getCurrentPosition();

        logger.info("BEFORE SWAP  T1: "+tiles.get(tile1).getCurrentPosition() + " T2: " +tiles.get(tile2).getCurrentPosition());

        tiles.get(tile1).setCurrentPosition(tiles.get(tile2).getCurrentPosition());
        tiles.get(tile2).setCurrentPosition(tmp);

        logger.info("AFTER SWAP  T1: "+tiles.get(tile1).getCurrentPosition() + " T2: " +tiles.get(tile2).getCurrentPosition());

        JsonObject position = new JsonObject()
                .put("position0", t1)
                .put("position1", t2);

        vertx.eventBus().publish("global_puzzle.1", position);

        checkSolution();

        resultHandler.handle(Future.succeededFuture());
    }

    /**
     * Method that let User to Join into another Game Puzzle.
     * @param username - Username of the user that want to Join in a Game.
     * @param resultHandler - The result handler will be called as soon as the initialization has been accomplished.
     */
    @Override
    public void joinTable(String username, Handler<AsyncResult<JsonObject>> resultHandler) {
        JsonObject images = new JsonObject();
        images.put("onlineUsers", users);
        images.put("lenght", rows * columns);

        users.add(username);

        for(int i = 0; i < tiles.size(); i++) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                ImageIO.write(tiles.get(i).getImage(), "jpg", output);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String imageBase64 = DatatypeConverter.printBase64Binary(output.toByteArray());

            images.put("image" + i, imageBase64);
            images.put("position" + i, tiles.get(i).getCurrentPosition());
        }

        vertx.eventBus().publish("newOnlineUser.1", new JsonObject().put("username", username));

        resultHandler.handle(Future.succeededFuture(images));
    }

    @Override
    public void fail(Handler<AsyncResult<Void>> resultHandler) {
        resultHandler.handle(Future.failedFuture("Not implemented"));
    }

    /**
     * Method that checks if the Puzzle is completed or not.
     */
    private void checkSolution() {
        if(tiles.stream().allMatch(Tile::isInRightPlace)) {
            vertx.eventBus().publish("end.1", "Puzzle Completed!");
            logger.info("Puzzle Completed!");
        }
    }
}

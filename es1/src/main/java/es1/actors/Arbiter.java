package es1.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import es1.util.*;
import es1.view.ViewFrame;
import java.util.*;
import java.util.stream.IntStream;

/**
 * This Class manage all action of the Arbiter in the game.
 */
public class Arbiter extends AbstractActor {

    /**
     * Fields that represents all data of the Class.
     */
    private int nPlayers;
    private int count;
    private final ArrayList<ActorRef> players = new ArrayList<>();
    private final Map<ActorRef, ArrayList<Integer>> playerCodes = new HashMap<>();
    private ActorRef timeoutActor;
    private ActorRef humanPlayer;
    private ArrayList<Integer> attempt = new ArrayList<>();
    private int playerToSend = 0;
    private ArrayList<Integer> turn;
    private ViewFrame viewFrame;
    private boolean stop = false;

    /**
     * Method that manage all operations of the Arbiter Player.
     * @return Message to Player or TimeOutActor with the Result of the Operation required.
     */
    @Override
    public Receive createReceive() {
        LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
        return receiveBuilder()
                .match(
                        StartMsg.class,
                        msg -> {
                            playerCodes.clear();
                            players.clear();
                            log.info("Pressed! " + "number: " +msg.getNumber() + " players: " + msg.getPlayers());
                            //setting viewFrame
                            this.viewFrame = msg.getViewFrame();
                            //setting players number in order to magane turns
                            this.nPlayers = msg.getPlayers();
                            //Creating new actor system for players
                            ActorSystem system = ActorSystem.create("PlayMistermind");
                            //creating timeout Actor
                            this.timeoutActor = system.actorOf(Props.create(TimeoutActor.class));
                            //Creating plyers Actors and saving them into a List
                            for(int i = 0; i < msg.getPlayers(); i++){
                                players.add(system.actorOf(Props.create(Player.class), "Guess" + i));
                            }
                            //checking if there's an human player
                            if(msg.isHumanPlayer()){
                                this.humanPlayer = system.actorOf(Props.create(Player.class), msg.getPlayerName());
                                log.info("ADDING HUMAN PLAYER...");
                                players.add(humanPlayer);
                                this.nPlayers = msg.getPlayers() + 1;
                            }
                            this.turn = generateRoundsList();
                            this.count = 1;
                            //telling player Actors to start
                            players.forEach(p-> {
                                if(p.equals(humanPlayer)){
                                    log.info("SETTING UP HUMAN PLAYER...");
                                    p.tell(new SetupHumanPlayer(msg.getNumber(), msg.getPlayerCode()), this.getSelf());
                                }else{
                                    p.tell(new SetupPlayer(msg.getNumber()), this.getSelf());
                                }
                            });
                })
                .match(
                        StopMsg.class,
                        stopMsg -> {
                            log.info("STOP!");
                            players.forEach(p-> p.tell(new StopMsg(), ActorRef.noSender()));
                            timeoutActor.tell("StopTimer", ActorRef.noSender());
                })
                .match(
                        HumanPlayerAttempt.class,
                        humanPlayerAttempt->{
                            this.playerToSend = humanPlayerAttempt.getPlayerToSend();
                            this.attempt = humanPlayerAttempt.getAttempt();
                            humanPlayer.tell(new TryAttempt(players, true, attempt, playerToSend), this.getSelf());
                })
                .match(
                        CodeGeneratedMsg.class,
                        done-> {
                            playerCodes.put(getSender(), done.getCode());
                            if(count < nPlayers){
                                log.info("DONE");
                                count++;
                            }else{
                                this.count = 0;
                                log.info("ALL PLAYERS GENERATED A SECRET NUMBER, THE GAME IS STARTING ...");
                                Collections.shuffle(turn);
                                log.info("Rounds: " + turn);
                                nextTurn();
                            }
                })
                .match(
                        PlayerAttemptMsg.class,
                        guess-> {
                            log.info("Player finished his turn.. next turn");
                            timeoutActor.tell("ResetTimer", ActorRef.noSender());
                            viewFrame.addEvent("Player" + players.indexOf(getSender()) + " attempt: " + guess.getAttempt() + "\t Result: " + guess.getResult());
                            count++;
                            nextTurn();
                })
                .matchEquals(
                        "Timeout",
                        timeout->{
                            log.info("Timeout... starting next turn");
                            viewFrame.addEvent("Timeout... starting next players turn");
                            players.get(turn.get(count)).tell("Timeout", this.getSelf());
                            count++;
                            nextTurn();
                })
                .match(
                		HumanWinMsg.class, 
                		humanWin -> {
                			ArrayList<Pair<ActorRef, ArrayList<Integer>>> tmp = new ArrayList<>();
                			for(int i = 0; i < humanWin.getResult().size(); i++) {
                    			tmp.add(new Pair<>(players.get(i), humanWin.getResult().get(i).getValue()));
                			}
                			this.getSelf().tell(new WinMsg(tmp), ActorRef.noSender());
                })
                .match(
                        WinMsg.class,
                        win->{
                        	//checking if players guess is correct
                            if(checkWinner(win.getResult())){
                                log.info("WINNERRRRRRRRR: " + getSender());
                                timeoutActor.tell("StopTimer", ActorRef.noSender());
                                viewFrame.addEvent("Winner: " + "Player" + players.indexOf(getSender()));
                                win.getResult().forEach(res-> {
                                    ArrayList<Integer> tmp = playerCodes.get(res.getKey());
                                    viewFrame.addEvent("Player" + players.indexOf(res.getKey()) + " numbers: "+ tmp  + "Guessed:  " + res.getValue());
                                });
                                //telling all players to stop couse one player submitted the correct guess
                                players.forEach(p-> p.tell(new StopMsg(), ActorRef.noSender()));
                                timeoutActor.tell("Stop", ActorRef.noSender());
                            } else {
                            	//removing the player that submitted the wrong guess
                                turn.remove(count);
                                //players.get(count).tell(new StopMsg(), ActorRef.noSender());
                                count++;
                                nextTurn();
                            }
        }).build();
    }

    /**
     * Method that generates the list of the random turns.
     * @return the turn of the Game.
     */
    private ArrayList<Integer> generateRoundsList() {
        ArrayList<Integer> turn = new ArrayList<>();
        IntStream.range(0, nPlayers)
                .distinct()
                .limit(nPlayers)
                .forEach(turn::add);
        return turn;
    }

    /**
     * Method that starts next players turn if it's the last player turn shuffles the list and restart the round robin.
     */
    private void nextTurn() {
        //Thread.sleep(1000);
        if(!stop){
            timeoutActor.tell("StartTimer", this.getSelf());
            if(count < turn.size()){
                if(players.get(turn.get(count)).equals(humanPlayer)){
                    viewFrame.addEvent("Please chose a code and a player to send it..");
                }else{
                    players.get(turn.get(count)).tell(new TryAttempt(players, false, attempt, playerToSend), this.getSelf());
                }
            }else{
                System.out.println("All players done one turn... changing turn order");
                count = 0;
                Collections.shuffle(turn);
                System.out.println("NEW ORDER: " + turn);
                nextTurn();
            }
        }
    }

    /**
     * Method that checks if the submitted result is correct.
     * @param results with the Secret Code.
     * @return true if correct or false if not.
     */
    private boolean checkWinner(ArrayList<Pair<ActorRef, ArrayList<Integer>>> results){
        ArrayList<Boolean> winCondition = new ArrayList<>();
        results.forEach(res-> {
            ArrayList<Integer> tmp = playerCodes.get(res.getKey());
            System.out.println("Players numbers: "+tmp  + "Guessed:  " + res.getValue());
            for(int i = 0; i < tmp.size(); i++){
                winCondition.add(tmp.get(i).equals(res.getValue().get(i)));
            }
        });
        return winCondition.stream().noneMatch(cond -> cond.equals(false));
    }
}
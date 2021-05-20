package es1.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import es1.util.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class that is the Actor that Manages all Player (Human or Not) of the Game.
 */
public class Player extends AbstractActor {

    /**
     * Fields that represents all data of the Class.
     */
    private boolean _duplicatesAllowed;
    private int _codeLength;
    private int _minCodeValue;
    private int _maxCodeValue;
    private int numberOfPlayers;
    private boolean stop;
    private boolean timeout;
    private ArrayList<Integer> _code = new ArrayList<>();
    private ActorRef sendedTo;
    private ActorRef arbiterRef;
    private ArrayList<Integer> currentAttempt;
    private final ArrayList<Pair<ActorRef, ArrayList<Integer>>> results = new ArrayList<>();
    private final ArrayList<Integer> basicLogicIA = new ArrayList<>();

    /**
     * Method to pre-Setup variables before Actor starts working.
     */
    public void preStart() {
        _codeLength = 2;
        _minCodeValue = 1;
        _maxCodeValue = 10;
        _duplicatesAllowed = false;
        stop = false;
        timeout = false;
    }

    /**
     * Method that manage all operations of the Actor Player.
     * @return Message to Arbiter or other Player with the Result of the Operation required.
     */
    @Override
    public Receive createReceive() {
        LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
        return receiveBuilder()
                .match(
                        SetupPlayer.class,
                        s -> {
                            results.clear();
                            _code.clear();
                            this.stop = false;
                            this._codeLength = s.getNumber();
                            generateCode();
                            log.info("Created New Code" + _code);
                            getSender().tell(new CodeGeneratedMsg(_code), this.getSelf());
                })
                .match(
                        SetupHumanPlayer.class,
                        setupHumanPlayer -> {
                            this.stop = false;
                            this._codeLength = setupHumanPlayer.getNumber();
                            _code = setupHumanPlayer.getCode();
                            log.info("Created New Code" + _code);
                            getSender().tell(new CodeGeneratedMsg(_code), this.getSelf());
                })
                .match(
                        SendAttemptToPlayer.class,
                        s -> {
                            log.info("Received Attempt of Another Actor");
                            getSender().tell(new SendResponseToPlayer(checkAttempt(s.getAttempt())), this.getSelf());
                            log.info("tentativo" + checkAttempt(s.getAttempt()).toString());
                })
                .match(
                        TryAttempt.class,
                        s -> {
                            if(!stop){
                                //Decoment in order to test TimeoutActor
                                //Thread.sleep(10000);
                                this.timeout = false;
                                this.numberOfPlayers = s.getPlayers().size();
                                this.arbiterRef = getSender();
                                //checks if the current player is a human player or a bot
                                if(s.isHuman()){
                                    this.currentAttempt = s.getAttempt();
                                    log.info("Inserted Code: " + currentAttempt);
                                    this.sendedTo = s.getPlayers().get(s.getPlayerToSend());
                                }else{
                                    this.currentAttempt = generateGuess();
                                    log.info("Generated Code: " + currentAttempt);
                                    int num = new Random().nextInt(s.getPlayers().size());
                                    while(num == s.getPlayers().indexOf(this.getSelf())){
                                        num = new Random().nextInt(s.getPlayers().size());
                                    }
                                    this.sendedTo = s.getPlayers().get(num);
                                }

                                if(!timeout){
                                    sendedTo.tell(new SendAttemptToPlayer(s.getPlayers(), currentAttempt), this.getSelf());
                                }
                            }
                })
                .match(
                        SendResponseToPlayer.class,
                        response-> {
                        	
                             if(!timeout){
                                 log.info("SENDED TO: " + sendedTo + "SENDER: " + getSender());
                                 log.info("RESULT: " + response.getResult());
                                 //checks if a player guessed all the numbers of other players, if yes sends a win mesage otherwise ends his turn
                                 if(!checkWin(response.getResult(), getSender())){
                                     log.info("ENDING MY TURN ...");
                                     arbiterRef.tell(new PlayerAttemptMsg(currentAttempt, response.getResult()), this.getSelf());
                                 }else {
                                     log.info("SENDING WIN MESSAGE");
                                     arbiterRef.tell(new WinMsg(results), this.getSelf());
                                 }
                             }
                })
                .match(StopMsg.class, stop -> this.stop = true)
                .matchEquals("Timeout", timeout-> this.timeout = true)
                .matchAny(s-> log.info("received unknown message"))
                .build();
    }

    /**
     * Method that generates the secret code for the user to guess.
     */
    private void generateCode() {
        if (!_duplicatesAllowed) {
            int codeRangeSize = _maxCodeValue - _minCodeValue;
            if (codeRangeSize < _codeLength) {
                throw new RuntimeException("Code value range must be larger than code length! Duplicate values are not permitted.");
            }
        }

        for (int i = 0; i < _codeLength; i++) {
            int number = ThreadLocalRandom.current().nextInt(_minCodeValue, _maxCodeValue);
            if (!_duplicatesAllowed) {
                // Recalculate number for current position if it already exists to prevent duplicates
                while (_code.contains(number)) {
                    number = ThreadLocalRandom.current().nextInt(_minCodeValue, _maxCodeValue);
                }
            }
            _code.add(number);
        }
    }

    /**
     * Method that generate an attempt for the game.
     * @return List<Integer> that contains the attempt.
     */
    private ArrayList<Integer> generateGuess() {
        ArrayList<Integer> attempt = new ArrayList<>();
        for (int i = 0; i < _codeLength; i++) {
            int number = ThreadLocalRandom.current().nextInt(_minCodeValue, _maxCodeValue);
            if (!_duplicatesAllowed) {
                // Recalculate number for current position if it already exists to prevent duplicates
                while (attempt.contains(number)) {
                    number = ThreadLocalRandom.current().nextInt(_minCodeValue, _maxCodeValue);
                }
                attempt.add(number);
            }
        }
        return attempt;
    }

    /**
     * Method that generate an attempt for the game.
     * @return List<Integer> that contains the attempt.
     */
    private ArrayList<Integer> generateAttempt() {
        ArrayList<Integer> attempt = new ArrayList<>();
        int x;
        do {
            attempt.clear();
            Random rand = new Random();
            attempt.add(rand.nextInt(_maxCodeValue));
            x = attempt.get(0);
            for (int i=1; i<_codeLength; i++) {
                attempt.add(rand.nextInt(_maxCodeValue));
                x = (x * 10) + attempt.get(i);
            }
        } while (basicLogicIA.contains(x));
        basicLogicIA.add(x);
        return attempt;
    }

    /**
     *
     * @param attemptCode
     * @return
     */
    private ArrayList<String> checkCode(ArrayList<Integer> attemptCode){
        ArrayList<String> resultOfAttempt = new ArrayList<>();
        attemptCode.forEach(value -> {
            if(_code.contains(value)){
                resultOfAttempt.add("-");
            }else{
                resultOfAttempt.add(" ");
            }
        });
        for(int i = 0; i < attemptCode.size(); i++){
            if(_code.get(i).equals(attemptCode.get(i))){
                resultOfAttempt.remove(resultOfAttempt.get(i));
                resultOfAttempt.add(i, "+");
            }
        }
        return resultOfAttempt;
    }

    /**
     *
     * @param result
     * @param sender
     * @return
     */
    private Boolean checkResults(ArrayList<Integer> result, ActorRef sender){
        if(result.stream().filter(r -> r.equals("+")).count() == result.size() && !results.contains(sender)){
            results.add(new Pair(sender, currentAttempt));
            System.out.println("FOUND A RESULT---> SENDER: " + sender + "CODE: " + currentAttempt + "Winning list: " + results + "-----");
            if(results.size() == numberOfPlayers - 1){
                System.out.println("WINNER" + this.getSelf() + "Winning list: " + results);
                return true;
            }
        }
        return false;
    }

    /**
     * Method that check the Attempt that a Player sent to it.
     * @param attemptCode of the other Player to check.
     * @return result of the Attempt (0 wrong or 1 ok).
     */
    private ArrayList<Integer> checkAttempt(ArrayList<Integer> attemptCode){
        ArrayList<Integer> resultOfAttempt = new ArrayList<>();
        int positive = 0;
        int negative = 0;
        for(int i = 0; i < attemptCode.size(); i++){
            if(_code.get(i).equals(attemptCode.get(i))){
                positive++;
            } else {
                if(_code.contains(attemptCode.get(i))){
                    negative++;
                }
            }
        }
        resultOfAttempt.add(positive);
        resultOfAttempt.add(negative);
        return resultOfAttempt;
    }

    /**
     * Method that Checks if the Actor has Win the Game or not.
     * @param result - All secret Code to Check.
     * @param sender of the operation.
     * @return true if it has win or false.
     */
    private Boolean checkWin(ArrayList<Integer> result, ActorRef sender){
        if(result.get(0) == _codeLength && !results.contains(sender)){
            results.add(new Pair(sender, currentAttempt));
            System.out.println("FOUND A RESULT---> SENDER: " + sender + "CODE: " + currentAttempt + "Winning list: " + results + "-----");
            if(results.size() == numberOfPlayers - 1){
                System.out.println("WINNER" + this.getSelf() + "Winning list: " + results);
                return true;
            }
        }
        return false;
    }
}
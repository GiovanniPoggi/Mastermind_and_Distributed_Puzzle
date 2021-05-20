package es1.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

/**
 * Class that is the Actor that manage the TimeOut in the Game.
 */
public class TimeoutActor extends AbstractActor{

    /**
     * Fields to count the time and stop clock.
     */
    private int count = 0;
    private boolean stop = false;

    /**
     * Method that manage all operations of the Actor TimeOut.
     * @return Message to Arbiter with the Result of the Operation.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("StartTimer", timer->{
                    if(!stop){
                        Thread.sleep(1);
                        this.count++;
                        if(count >= 5000){
                            this.count = 0;
                            getSender().tell("Timeout", ActorRef.noSender());
                        }
                        getSelf().tell("StartTimer", getSender());
                    }
                }).matchEquals("ResetTimer", reset-> {
                    this.count = 0;
                }).matchEquals("StopTimer", stop -> {
                    this.stop = true;
                })
                .build();
    }
}
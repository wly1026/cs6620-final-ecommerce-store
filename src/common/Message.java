package common;

import java.io.Serializable;

public class Message implements Serializable {
    private PaxosState state;
    private Proposal proposal;

    public Message(PaxosState state, Proposal proposal){
        this.state = state;
        this.proposal = proposal;
    }

    public PaxosState getState(){
        return this.state;
    }

    public void setState(PaxosState newState){
        this.state = newState;
    }

}

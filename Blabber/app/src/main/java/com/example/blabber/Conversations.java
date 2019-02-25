package com.example.blabber;

public class Conversations {

    public boolean seen;
    public long timeStamp;

    public Conversations() {}

    public Conversations(boolean seen, long timeStamp) {
        this.seen = seen;
        this.timeStamp = timeStamp;
    }

    public boolean isSeen() { return seen; }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void getTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}

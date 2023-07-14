package com.electrodiux.events;

import java.io.Serializable;

public abstract class Event implements Serializable {

    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

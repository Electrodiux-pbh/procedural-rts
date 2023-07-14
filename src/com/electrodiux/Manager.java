package com.electrodiux;

import com.electrodiux.block.Blocks;
import com.electrodiux.events.EventManager;

public class Manager {

    private static EventManager eventManager;

    public static EventManager eventManager() {
        return eventManager;
    }

    public static void load() {
        eventManager = new EventManager();

        Blocks.loadBlocks();
    }

}

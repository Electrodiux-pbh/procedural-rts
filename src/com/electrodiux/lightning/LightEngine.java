package com.electrodiux.lightning;

import java.util.LinkedList;

import com.electrodiux.block.BlockDefinition;
import com.electrodiux.world.Chunk;
import com.electrodiux.world.World;

public class LightEngine {

    private World world;

    public LightEngine(World world) {
        this.world = world;
    }

    public static void calculateLight(Chunk chunk) {
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                    BlockDefinition block = chunk.getBlock(x, y, z);
                    if (block != null && block.emitsLight()) {
                        updateLight(chunk, x, y, z, block.getLightEmision());
                    }
                }
            }
        }
    }

    private static void updateLight(Chunk chunk, int lx, int ly, int lz, byte lightLevel) {
        // int y = ly;
        // for (int tx = -lightLevel; tx < lightLevel; tx++) {
        // int x = lx + tx;
        // for (int tz = -lightLevel; tz < lightLevel; tz++) {
        // int z = lz + tz;
        // if (Chunk.outOfBounds(x, y, z))
        // continue;

        // byte xlight = (byte) (tx < 0 ? lightLevel + tx : lightLevel - tx);
        // byte zlight = (byte) (tz < 0 ? lightLevel + tz : lightLevel - tz);

        // chunk.setLight(x, y, z, (byte) Math.min(xlight, zlight));
        // }
        // }
        boolean[] visited = new boolean[Chunk.CHUNK_AREA * Chunk.CHUNK_HEIGHT];
        LinkedList<LightNode> queue = new LinkedList<>();

        queue.add(new LightNode(lx, ly, lz, lightLevel));

        while (!queue.isEmpty()) {
            LightNode node = queue.removeFirst();
            int posX = node.x;
            int posY = node.y;
            int posZ = node.z;

            int index = Chunk.getBlockIndex(posX, posY, posZ);

            if (Chunk.outOfBounds(posX, posY, posZ))
                continue;

            // byte currentLightLevel = chunk.getLight(posX, posY, posZ);
            // if (currentLightLevel > node.lightLevel)
            // continue;

            chunk.setLight(posX, posY, posZ, node.lightLevel);
            visited[index] = true;

            if (node.lightLevel <= 1)
                continue;

            if (!isVisited(visited, lx, ly + 1, lz))
                queue.add(new LightNode(posX, posY + 1, posZ, node.lightLevel - 1)); // up
            if (!isVisited(visited, lx, ly - 1, lz))
                queue.add(new LightNode(posX, posY - 1, posZ, node.lightLevel - 1)); // down

            if (!isVisited(visited, lx, ly, lz - 1))
                queue.add(new LightNode(posX, posY, posZ - 1, node.lightLevel - 1)); // north
            if (!isVisited(visited, lx, ly, lz + 1))
                queue.add(new LightNode(posX, posY, posZ + 1, node.lightLevel - 1)); // south

            if (!isVisited(visited, lx - 1, ly, lz))
                queue.add(new LightNode(posX - 1, posY, posZ, node.lightLevel - 1)); // west
            if (!isVisited(visited, lx + 1, ly, lz))
                queue.add(new LightNode(posX + 1, posY, posZ, node.lightLevel - 1)); // east
        }
    }

    private static boolean isVisited(boolean[] visited, int x, int y, int z) {
        if (Chunk.outOfBounds(x, y, z))
            return true;
        return visited[Chunk.getBlockIndex(x, y, z)];
    }

    private static class LightNode {
        public int x, y, z;
        public byte lightLevel;

        public LightNode(int x, int y, int z, int lightLevel) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.lightLevel = (byte) lightLevel;
        }

    }
}

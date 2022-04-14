/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.examples.dark;

import java.util.Arrays;

public final class MapModelBuilder {

    private final Tile[][] mapModel;

    private MapModelBuilder(int width, int height) {
        mapModel = new Tile[width][height];
    }

    public static Tile[][] build(int width, int height, int roomCount) {
        var builder = new MapModelBuilder(width, height);
        builder.fillRect(0, 0, width, height, Tile.WALL);
        builder.buildRooms(width, height, roomCount);
        return builder.mapModel;
    }

    private void buildRooms(int width, int height, int roomCount) {
        fillRect(5, 5, width >>> 1, height >>> 1, Tile.FLOOR);
    }

    private void fillRect(int x, int y, int width, int height, Tile tile) {
        for (int i = y; i < y + height; i++) {
            Arrays.fill(mapModel[i], x, x + width, tile);
        }
    }

    public enum Tile {
        WALL,
        FLOOR
    }
}

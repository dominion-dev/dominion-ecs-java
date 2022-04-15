/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.examples.dark;

import java.util.Arrays;
import java.util.Random;

/**
 * This map builder is inspired by Herbert Wolverson's awesome Rust tutorial on rogue-like:
 * https://github.com/amethyst/rustrogueliketutorial/blob/master/chapter-04-newmap/src/map.rs
 */
public final class MapModelBuilder {
    public final static int ROOM_MIN_SIDE_LENGTH = 4;
    public final static int ROOM_MAX_SIDE_LENGTH = 10;
    private final Tile[][] mapModel;
    private final Rect[] rooms;

    private MapModelBuilder(int width, int height, int roomCount) {
        mapModel = new Tile[height][width];
        rooms = new Rect[roomCount];
    }

    public static Tile[][] build(int width, int height, int roomCount) {
        var builder = new MapModelBuilder(width, height, roomCount);
        builder.fillRect(new Rect(0, 0, width, height), Tile.WALL);
        builder.buildRooms(width, height, roomCount);
        return builder.mapModel;
    }

    private void buildRooms(int width, int height, int roomCount) {
        var rand = new Random();
        for (int index = 0; index < roomCount; ) {
            var room = new Rect(
                    rand.nextInt(1, width - ROOM_MAX_SIDE_LENGTH)
                    , rand.nextInt(1, height - ROOM_MAX_SIDE_LENGTH)
                    , (short) rand.nextInt(ROOM_MIN_SIDE_LENGTH, ROOM_MAX_SIDE_LENGTH)
                    , (short) rand.nextInt(ROOM_MIN_SIDE_LENGTH, ROOM_MAX_SIDE_LENGTH)
            );
            boolean addRoom = true;
            for (int i = 0; i < index; i++) {
                if (room.intersect(rooms[i])) {
                    addRoom = false;
                    break;
                }
            }
            if (addRoom) {
                rooms[index++] = room;
                fillRect(room, Tile.FLOOR);
            }
        }
    }

    private void fillRect(Rect rect, Tile tile) {
        for (int i = rect.top(); i < rect.bottom(); i++) {
            Arrays.fill(mapModel[i], rect.left(), rect.right(), tile);
        }
    }

    public enum Tile {
        WALL,
        FLOOR
    }

    public record Rect(int left, int top, int right, int bottom, Point center) {

        public Rect(int left, int top, int right, int bottom) {
            this(left, top, right, bottom
                    , new Point((left + right) >>> 1, (top + bottom) >>> 1)
            );
        }

        public Rect(int x, int y, short width, short height) {
            this(x, y, x + width, y + height);
        }

        public boolean intersect(Rect other) {
            return other != null &&
                    !(other.left > right ||
                            other.right < left ||
                            other.top > bottom ||
                            other.bottom < top);
        }

        record Point(int x, int y) {
        }
    }
}

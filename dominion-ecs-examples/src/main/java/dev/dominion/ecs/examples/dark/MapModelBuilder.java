/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.examples.dark;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * This map builder is inspired by Herbert Wolverson's awesome Rust tutorial on rogue-like:
 * https://github.com/amethyst/rustrogueliketutorial/blob/master/chapter-04-newmap/src/map.rs
 */
public final class MapModelBuilder {
    public final static int ROOM_MIN_SIDE_LENGTH = 4;
    public final static int ROOM_MAX_SIDE_LENGTH = 10;

    private MapModelBuilder() {
    }

    public static MapModel build(int width, int height, int roomCount) {
        return buildHallways(buildRooms(width, height, roomCount));
    }

    private static void fillRect(Rect rect, Tile tile, Tile[][] map) {
        for (int i = rect.top(); i < rect.bottom(); i++) {
            Arrays.fill(map[i], rect.left(), rect.right(), tile);
        }
    }

    private static void hLine(int x1, int x2, int y, Tile[][] map) {
        Arrays.fill(map[y], Math.min(x1, x2), Math.max(x1, x2) + 1, Tile.FLOOR);
    }

    private static void vLine(int y1, int y2, int x, Tile[][] map) {
        for (int i = Math.min(y1, y2); i <= Math.max(y1, y2); i++) {
            map[i][x] = Tile.FLOOR;
        }
    }

    private static MapModel buildRooms(int width, int height, int roomCount) {
        var map = new Tile[height][width];
        var rooms = new Rect[roomCount];
        // first, fill the map with walls
        fillRect(new Rect(0, 0, width, height), Tile.WALL, map);
        // then carve randomly generated non-overlapping rooms
        var rand = new Random();
        for (int index = 0; index < rooms.length; ) {
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
                // carve the room
                fillRect(room, Tile.FLOOR, map);
            }
        }
        rooms = Arrays.stream(rooms)
                .sorted(Comparator.comparingInt(r -> r.center.x))
                .toArray(Rect[]::new);
        return new MapModel(map, rooms, width, height);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static MapModel buildHallways(MapModel mapModel) {
        var map = mapModel.map;
        var rooms = mapModel.rooms;
        Random rand = new Random();
        Arrays.stream(rooms)
//                .peek(System.out::println)
                .reduce((prev, curr) -> {
                    int hY, vX;
                    if (rand.nextBoolean()) {
                        hY = prev.center.y;
                        vX = curr.center.x;
                    } else {
                        hY = curr.center.y;
                        vX = prev.center.x;
                    }
                    hLine(prev.center.x, curr.center.x, hY, map);
                    vLine(prev.center.y, curr.center.y, vX, map);
                    return curr;
                });
        return mapModel;
    }

    public enum Tile {
        WALL, FLOOR
    }

    record MapModel(Tile[][] map, Rect[] rooms, int width, int height) {
    }

    public record Rect(int left, int top, int right, int bottom, Point center) {

        public Rect(int left, int top, int right, int bottom) {
            this(left, top, right, bottom, new Point((left + right) >>> 1, (top + bottom) >>> 1));
        }

        public Rect(int x, int y, short width, short height) {
            this(x, y, x + width, y + height);
        }

        public boolean intersect(Rect other) {
            return other != null && !(other.left > right || other.right < left || other.top > bottom || other.bottom < top);
        }

        record Point(int x, int y) {
        }
    }
}

/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.examples.dark;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * A map builder able to build a dungeon with a given number of random rooms
 */
public final class MapModelBuilder {
    public final static int ROOM_MIN_SIDE_LENGTH = 4;
    public final static int ROOM_MAX_SIDE_LENGTH = 10;

    private MapModelBuilder() {
    }

    /**
     * the MapModelBuilder entry-point to build a map
     *
     * @param width     the width of the map
     * @param height    the height of the map
     * @param roomCount the total number of random rooms to build
     * @return the map model
     */
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

    /**
     * Build the dungeon creating random rooms.
     * <p>
     * Builder functions inspired by Herbert Wolverson's awesome Rust tutorial on rogue-like:
     * https://github.com/amethyst/rustrogueliketutorial/blob/master/chapter-04-newmap/src/map.rs
     *
     * @param width     the width of the map
     * @param height    the height of the map
     * @param roomCount the total number of rooms to build
     * @return the map model
     */
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

    /**
     * builds hallways between rooms
     *
     * @param mapModel the msp model
     * @return the map model
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static MapModel buildHallways(MapModel mapModel) {
        var map = mapModel.map;
        var rooms = mapModel.rooms;
        Random rand = new Random();
        Arrays.stream(rooms)
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
        WALL(true),
        FLOOR(false);
        public final boolean opaque;

        Tile(boolean opaque) {
            this.opaque = opaque;
        }
    }


    /**
     * MapModel is a bi-dimensional array of Tile.
     * It contains an array of rooms represented with a Rect type and having a center Point
     *
     * @param map    a bi-dimensional Tile array
     * @param rooms  an array of Rect
     * @param width  the width of the map
     * @param height the height of the map
     */
    record MapModel(Tile[][] map, Rect[] rooms, int width, int height) {

        // Thanks Uncle Bresenham for your line drawing algorithm
        public boolean checkSightLine(int fromX, int fromY, int toX, int toY) {
            int dx = Math.abs(toX - fromX), dy = Math.abs(toY - fromY);
            int sx = fromX < toX ? 1 : -1, sy = fromY < toY ? 1 : -1;
            int err = dx - dy, errDot2;
            for (; ; ) {
                if (fromX == toX && fromY == toY) {
                    return true;
                }
                if (map[fromY][fromX].opaque) {
                    return false;
                }
                errDot2 = err << 1;
                if (errDot2 > -dy) {
                    err = err - dy;
                    fromX += sx;
                }
                if (errDot2 < dx) {
                    err += dx;
                    fromY += sy;
                }
            }
        }
    }

    public record Rect(int left, int top, int right, int bottom, Point center) {

        public Rect(int left, int top, int right, int bottom) {
            this(left, top, right, bottom, new Point((left + right) >>> 1, (top + bottom) >>> 1));
        }

        public Rect(int x, int y, short width, short height) {
            this(x, y, x + width, y + height);
        }

        // returns true if this intersects the other Rect
        public boolean intersect(Rect other) {
            return other != null && !(other.left > right || other.right < left || other.top > bottom || other.bottom < top);
        }
    }

    record Point(int x, int y) {
    }
}

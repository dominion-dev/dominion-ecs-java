/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.examples.dark;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.api.Scheduler;
import dev.dominion.ecs.examples.dark.MapModelBuilder.MapModel;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DarkEntities is a simple example app that could inspire a turn-based rogue-like game running on a terminal window.
 * Shows how to create a basic Dominion of entities, components, and systems by creating and moving a camera with a
 * light to illuminate and explore a random dungeon map.
 * Implements a wise light system able to fork subsystems and distribute on worker threads to take advantage of multiple
 * cores.
 */
public final class DarkEntities {

    // don't print Dominion banner info on startup
    static {
        System.setProperty("dominion.show-banner", "false");
    }

    // the game entry point
    public static void main(String[] args) {
        Screen screen = new Screen(120, 25);
        menu(screen);
    }

    // the menu flow
    private static void menu(Screen screen) {
        screen.drawRect(0, 0, screen.width, screen.height);
        screen.drawText("Dark Entities", screen.center.x(), screen.center.y(), Screen.TextAlignment.CENTER);
        String playerName = screen.prompt("What's your name, Hero?", "^[a-zA-Z0-9-_]+$");
        String input = screen.prompt(String.format("Hello %s, are you ready for the darkness? (press Y to confirm)", playerName));
        if (input.toLowerCase().startsWith("y")) {
            game(playerName, screen);
        }
    }

    // the game flow
    private static void game(String playerName, Screen screen) {
        // creates the dark dominion
        Dominion dark = Dominion.create();
        // creates the map model
        var mapModel = MapModelBuilder.build(64, 48, 16);
        // creates entities
        createEntities(dark, mapModel);
        // creates the input holder
        var pressedKey = new AtomicReference<ArrowKey>();
        // creates systems returning the scheduler
        var scheduler = createSystems(dark, pressedKey, mapModel, screen);
        // executes first tick
        screen.clear();
        scheduler.tick();
        // greets the player
        screen.drawText(String.format("Hello %s, your adventure starts here!", playerName), screen.center.x(), 3, Screen.TextAlignment.CENTER);
        // starts the game loop
        loop(scheduler, pressedKey, screen);
    }

    // creates all game entities
    private static void createEntities(Dominion dark, MapModel mapModel) {
        // creates a camera with a light
        MapModelBuilder.Point firstRoomCenter = mapModel.rooms()[0].center();
        dark.createEntity(new Camera(), new Light(50), new Position(firstRoomCenter.x(), firstRoomCenter.y()));
        // creates map-view entities
        for (int y = 0; y < mapModel.height(); y++) {
            for (int x = 0; x < mapModel.width(); x++) {
                dark.createEntity(new Map(), new Position(x, y), switch (mapModel.map()[y][x]) {
                    case WALL -> new Render('#', '.');
                    case FLOOR -> new Render(':', ' ');
                }).setState(Visibility.NOT_VISIBLE);
            }
        }
    }

    // creates all game systems
    private static Scheduler createSystems(Dominion dark, AtomicReference<ArrowKey> pressedKey, MapModel mapModel, Screen screen) {
        // creates scheduler
        var scheduler = dark.createScheduler();
        // cameras input-controller system
        scheduler.schedule(() -> {
            if (pressedKey.get() == null) {
                return;
            }
            dark.findEntitiesWith(Camera.class, Position.class).stream().forEach(r -> {
                Position position = r.comp2();
                switch (pressedKey.get()) {
                    case LEFT -> position.x--;
                    case RIGHT -> position.x++;
                    case UP -> position.y--;
                    case DOWN -> position.y++;
                }
            });
        });
        // system to set visible map tiles as visited
        scheduler.schedule(() -> {
            var map = dark.findEntitiesWith(Map.class);
            var iterator = map.withState(Visibility.VISIBLE).iterator();
            while (iterator.hasNext()) {
                var tile = iterator.next();
                tile.entity().setState(Visibility.VISITED);
            }
        });
        // light system
        scheduler.schedule(() -> {
            var light = dark.findEntitiesWith(Light.class, Position.class).iterator().next();
            var map = dark.findEntitiesWith(Map.class, Position.class);
            var notVisibleMap = map.withState(Visibility.NOT_VISIBLE).iterator();
            var visitedMap = map.withState(Visibility.VISITED).iterator();
            Position lightPosition = light.comp2();
            int lightLumen = light.comp1().lumen;
            scheduler.forkAndJoinAll(
                    () -> setVisibleArea(lightPosition, lightLumen, notVisibleMap, mapModel),
                    () -> setVisibleArea(lightPosition, lightLumen, visitedMap, mapModel)
            );
        });
        // map-view renderer system
        scheduler.schedule(() -> {
            var camera = dark.findEntitiesWith(Camera.class, Position.class).iterator().next();
            var map = dark.findEntitiesWith(Map.class, Render.class, Position.class);
            var visibleMap = map.withState(Visibility.VISIBLE).iterator();
            var visitedMap = map.withState(Visibility.VISITED).iterator();
            scheduler.forkAndJoinAll(
                    () -> renderMap(camera.comp2(), visibleMap, false, screen),
                    () -> renderMap(camera.comp2(), visitedMap, true, screen)
            );
            // draw the camera position
            screen.drawGlyph('@', screen.center.x(), screen.center.y());
        });
        return scheduler;
    }

    // set a visible area in the map view by checking the sight-line from the center using the map model
    private static void setVisibleArea(Position areaCenter, int area, Iterator<Results.With2<Map, Position>> mapView, MapModel mapModel) {
        while (mapView.hasNext()) {
            var tile = mapView.next();
            Position mapPosition = tile.comp2();
            int dx = Math.abs(mapPosition.x - areaCenter.x);
            int dy = Math.abs(mapPosition.y - areaCenter.y);
            boolean isTileInsideArea = area >= dx * dx + dy * dy;
            if (isTileInsideArea && mapModel.checkSightLine(areaCenter.x, areaCenter.y, mapPosition.x, mapPosition.y)) {
                tile.entity().setState(Visibility.VISIBLE);
            }
        }
    }

    // map rendering function relative to the camera position
    private static void renderMap(Position cameraPosition, Iterator<Results.With3<Map, Render, Position>> mapView,
                                  boolean isVisitedView, Screen screen) {
        while (mapView.hasNext()) {
            var tile = mapView.next();
            Position position = tile.comp3();
            Render render = tile.comp2();
            renderTile(position, cameraPosition, isVisitedView ? render.visitedGlyph : render.glyph, screen);
        }
    }

    // tile rendering function relative to the camera position
    private static void renderTile(Position mapPosition, Position cameraPosition, char glyph, Screen screen) {
        screen.drawGlyph(glyph, (mapPosition.x - cameraPosition.x) * 2 + screen.center.x(),// * 2 to fix the char size ratio
                (mapPosition.y - cameraPosition.y) + screen.center.y());
    }

    // the game loop
    private static void loop(Scheduler scheduler, AtomicReference<ArrowKey> pressedKey, Screen screen) {
        boolean goOn = true;
        while (goOn) {
            // fetches input
            String input = screen.prompt("Press WASD keys to Move, Q to Quit", "^[wasdqWASDQ]+$").toLowerCase();
            // checks if quit
            if (input.startsWith("q")) {
                goOn = !confirmQuit(screen);
                continue;
            }
            // translates input in pressedKey
            pressedKey.set(switch (input.substring(0, 1)) {
                case "w" -> ArrowKey.UP;
                case "a" -> ArrowKey.LEFT;
                case "s" -> ArrowKey.DOWN;
                case "d" -> ArrowKey.RIGHT;
                default -> null;
            });
            // executes periodic tick
            screen.clear();
            scheduler.tick();
        }
        // orderly shutdown the scheduler
        scheduler.shutDown();
    }

    // quit-confirming function
    private static boolean confirmQuit(Screen screen) {
        String input = screen.prompt("Do you really want to quit? (press Y to confirm)");
        return input.toLowerCase().startsWith("y");
    }

    // enum and entity states

    // input mapping
    enum ArrowKey {
        LEFT, RIGHT, UP, DOWN
    }

    // map visibility state
    enum Visibility {
        VISIBLE, NOT_VISIBLE, VISITED
    }

    // entity components

    // absolute position in the map
    static final class Position {
        int x, y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Position{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    // render component provides glyphs
    record Render(char glyph, char visitedGlyph) {
    }

    // light
    record Light(int lumen) {
    }

    // camera tag
    record Camera() {
    }

    // map tag
    record Map() {
    }
}

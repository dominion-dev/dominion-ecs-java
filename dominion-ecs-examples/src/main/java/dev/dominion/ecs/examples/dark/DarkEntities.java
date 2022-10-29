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
 * Shows how to create a basic Dominion of entities, components, and systems by adding a camera with a light to illuminate
 * and explore a random dungeon map.
 * Implements a neat lighting system able to fork subsystems and distribute on worker threads to take advantage of multiple
 * cores.
 */
public final class DarkEntities {

    // don't print Dominion banner info on startup
    static {
        System.setProperty("dominion.show-banner", "false");
    }

    // the game entry point
    public static void main(String[] args) {
        // creates the user interface with a given size in the terminal window
        Screen screen = new Screen(120, 25);
        // starts the menu flow
        menu(screen);
    }

    // the menu flow
    private static void menu(Screen screen) {
        // draws a frame with the app name in the center
        screen.drawRect(0, 0, screen.width, screen.height);
        screen.drawText("Dark Entities", screen.center.x(), screen.center.y(), Screen.TextAlignment.CENTER);
        // asks for the player name
        String playerName = screen.prompt("What's your name, Hero?", "^[a-zA-Z0-9-_]+$").toUpperCase();
        // greets the player and asks if he's ready
        String input = screen.prompt(String.format("Hello %s, are you ready for the darkness? (press Y to confirm)", playerName));
        if (input.toLowerCase().startsWith("y")) {
            // starts the game flow
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
        // clears the UI
        screen.clear();
        // executes first tick
        scheduler.tick();
        // greets the player
        screen.drawText(String.format("Hello %s, your adventure starts here!", playerName), screen.center.x(), 3, Screen.TextAlignment.CENTER);
        // starts the game loop
        loop(scheduler, pressedKey, screen);
    }

    // creates all game entities
    private static void createEntities(Dominion dark, MapModel mapModel) {
        // gets the center of the first room
        MapModelBuilder.Point firstRoomCenter = mapModel.rooms()[0].center();
        // creates a Camera with a Light entity with:
        // - a Camera tag: a component without properties just to classify this entity as a camera
        // - a Light: a component to add a light to the entity with a specific lumen value
        // - a Position: a component that keep the x and y coordinates of the camera in the map
        dark.createEntity(new Camera(), new Light(50), new Position(firstRoomCenter.x(), firstRoomCenter.y()));
        // creates map-view entities
        for (int y = 0; y < mapModel.height(); y++) {
            for (int x = 0; x < mapModel.width(); x++) {
                // for each Tile in the map model you will get a map-tile view entity with:
                // - a Map tag: a component without properties just to classify this entity as a map-tile view
                // - a Position: a component that keep the x and y coordinates of the tile view in the map
                // - a Render: a component with glyphs to represent the Tile, one when the tile is visible and one when is not
                // visible but already seen (visited)
                // - a Visibility state: an enum value to keep the visibility state of the map-tile view. The initial value
                // is NOT_VISIBLE as all entities are in the darkness...
                dark.createEntity(new Map(), new Position(x, y), switch (mapModel.map()[y][x]) {
                    case WALL -> new Render('#', '.');
                    case FLOOR -> new Render(':', ' ');
                }).setState(Visibility.NOT_VISIBLE);
            }
        }
    }

    // creates all game systems
    // all the scheduled systems will be executed in sequence at each tick of the scheduler, in the order in which they were added
    // some systems will fork other subsystems to distribute tasks across multiple worker threads
    private static Scheduler createSystems(Dominion dark, AtomicReference<ArrowKey> pressedKey, MapModel mapModel, Screen screen) {
        // creates the scheduler
        var scheduler = dark.createScheduler();
        // adds the cameras input-controller system
        scheduler.schedule(() -> {
            if (pressedKey.get() == null) {
                return;
            }
            // finds the camera and move its position according to the pressed key (no wall collision detection)
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
        // adds a system to reset all visible map tiles as visited
        scheduler.schedule(() -> {
            // finds all map entities
            var map = dark.findEntitiesWith(Map.class);
            // selects only the visible map
            var iterator = map.withState(Visibility.VISIBLE).iterator();
            // resets the visible map as visited map
            while (iterator.hasNext()) {
                var tile = iterator.next();
                tile.entity().setState(Visibility.VISITED);
            }
        });
        // adds the lighting system
        scheduler.schedule(() -> {
            // finds the light entity and gets the position and the lumen power
            var light = dark.findEntitiesWith(Light.class, Position.class).iterator().next();
            Position lightPosition = light.comp2();
            int lightLumen = light.comp1().lumen;
            // finds all map entities
            var map = dark.findEntitiesWith(Map.class, Position.class);
            // selects the not-visible map
            var notVisibleMap = map.withState(Visibility.NOT_VISIBLE).iterator();
            // selects the visited map
            var visitedMap = map.withState(Visibility.VISITED).iterator();
            // forks the system in two subsystems that will run immediately distributed across worker threads:
            scheduler.forkAndJoinAll(
                    // sets all not-visible map tiles that are illuminated by light to be visible
                    () -> setVisibleArea(lightPosition, lightLumen, notVisibleMap, mapModel),
                    // sets all visited map tiles that are illuminated by light to be visible
                    () -> setVisibleArea(lightPosition, lightLumen, visitedMap, mapModel)
            );
        });
        // adds the map-view renderer system
        scheduler.schedule(() -> {
            // finds the camera
            var camera = dark.findEntitiesWith(Camera.class, Position.class).iterator().next();
            // finds all map entities
            var map = dark.findEntitiesWith(Map.class, Render.class, Position.class);
            // selects the visible map
            var visibleMap = map.withState(Visibility.VISIBLE).iterator();
            // selects the visited map
            var visitedMap = map.withState(Visibility.VISITED).iterator();
            // renders both visible and visited map in parallel by forking the system in two subsystems that will run
            // immediately distributed across worker threads:
            scheduler.forkAndJoinAll(
                    // renders the visible map
                    () -> renderMap(camera.comp2(), visibleMap, false, screen),
                    // renders the visited map
                    () -> renderMap(camera.comp2(), visitedMap, true, screen)
            );
            // draws the camera position
            screen.drawGlyph('@', screen.center.x(), screen.center.y());
        });
        return scheduler;
    }

    // sets a visible area in the map view by checking the sight-line from the center using the map model
    private static void setVisibleArea(Position areaCenter, int area, Iterator<Results.With2<Map, Position>> mapView, MapModel mapModel) {
        while (mapView.hasNext()) {
            // gets the next tile and the position in the map
            var tile = mapView.next();
            Position mapPosition = tile.comp2();
            // checks if the tile is inside the circle area
            int dx = Math.abs(mapPosition.x - areaCenter.x), dy = Math.abs(mapPosition.y - areaCenter.y);
            boolean isTileInsideArea = area >= dx * dx + dy * dy;
            // checks if the center is visible from the tile and if the view is free from obstacles
            boolean checkSightLine = mapModel.checkSightLine(areaCenter.x, areaCenter.y, mapPosition.x, mapPosition.y);
            if (isTileInsideArea && checkSightLine) {
                // sets the tile to be visible
                tile.entity().setState(Visibility.VISIBLE);
            }
        }
    }

    // the map rendering function relative to the camera position
    private static void renderMap(Position cameraPosition, Iterator<Results.With3<Map, Render, Position>> mapView,
                                  boolean isVisitedView, Screen screen) {
        while (mapView.hasNext()) {
            // gets the next tile, the position in the map, and the render component to use
            var tile = mapView.next();
            Position position = tile.comp3();
            Render render = tile.comp2();
            // renders the tile using the visible|visited glyph provided by the render component
            renderTile(position, cameraPosition, isVisitedView ? render.visitedGlyph : render.glyph, screen);
        }
    }

    // the tile rendering function relative to the camera position
    private static void renderTile(Position mapPosition, Position cameraPosition, char glyph, Screen screen) {
        screen.drawGlyph(glyph, (mapPosition.x - cameraPosition.x) * 2 + screen.center.x(),// * 2 to fix the char size ratio
                (mapPosition.y - cameraPosition.y) + screen.center.y());
    }

    // the game loop
    private static void loop(Scheduler scheduler, AtomicReference<ArrowKey> pressedKey, Screen screen) {
        boolean keepGoingOn = true;
        while (keepGoingOn) {
            // fetches input
            String input = screen.prompt("Press WASD keys to Move, Q to Quit", "^[wasdqWASDQ]+$").toLowerCase();
            // checks if quit
            if (input.startsWith("q")) {
                keepGoingOn = !confirmQuit(screen);
                continue;
            }
            // translates raw input in pressed ArrowKey
            pressedKey.set(switch (input.substring(0, 1)) {
                case "w" -> ArrowKey.UP;
                case "a" -> ArrowKey.LEFT;
                case "s" -> ArrowKey.DOWN;
                case "d" -> ArrowKey.RIGHT;
                default -> null;
            });
            // clears the screen and executes the periodic tick
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

    // enum and entity states:

    // input mapping
    enum ArrowKey {
        LEFT, RIGHT, UP, DOWN
    }

    // the map visibility state
    enum Visibility {
        VISIBLE, NOT_VISIBLE, VISITED
    }

    // entity components:

    // the component the provides the entity absolute position in the map
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

    // the render component that provides both visible and visited glyphs
    record Render(char glyph, char visitedGlyph) {
    }

    // the map tag component
    record Map() {
    }

    // the camera tag component
    record Camera() {
    }

    // the light component that provides the lumen value
    record Light(int lumen) {
    }
}

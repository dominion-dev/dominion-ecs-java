/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.examples.dark;

import dev.dominion.ecs.examples.dark.MapModelBuilder.MapModel;
import dev.dominion.ecs.examples.dark.MapModelBuilder.Rect;
import dev.dominion.ecs.examples.dark.MapModelBuilder.Tile;

public final class DarkEntities {

    // don't print Dominion's info on startup
    static {
        System.setProperty("dominion.show-banner", "false");
    }

    public static void main(String[] args) {
        Screen screen = new Screen(120, 25);
        menu(screen);
    }

    private static void menu(Screen screen) {
        screen.drawRect(0, 0, screen.width, screen.height);
        screen.drawText("Dark Entities", screen.center.x(), screen.center.y(), Screen.TextAlignment.CENTER);
        String playerName = screen.prompt("What's your name, Hero?", "^[a-zA-Z0-9-_]+$");
        String input = screen.prompt(String.format("Hello %s, are you ready for the darkness? (press Y to confirm)", playerName));
        if (input.toLowerCase().startsWith("y")) {
            new Game(playerName, screen);
        }
    }

    private static class Game {
        private final Screen screen;
        private final MapModel mapModel;
        private final Position cameraPosition;

        Game(String playerName, Screen screen) {
            this.screen = screen;
            this.mapModel = MapModelBuilder.build(64, 48, 16);
            Rect.Point firstRoomCenter = mapModel.rooms()[0].center();
            this.cameraPosition = new Position(firstRoomCenter.x(), firstRoomCenter.y());
            screen.clear();
            renderMap();
            screen.drawText(String.format("Hello %s, your adventure starts here!", playerName)
                    , screen.center.x(), 3, Screen.TextAlignment.CENTER);
            loop();
        }

        private void renderMap() {
            Tile[][] map = mapModel.map();
            for (int y = 0; y < map.length; y++) {
                for (int x = 0; x < map[y].length; x++) {
                    screen.drawGlyph(switch (map[y][x]) {
                                case WALL -> '#';
                                case FLOOR -> ':';
                            },
                            (x - cameraPosition.x) * 2 + screen.center.x(),// * 2 to fix the char size ratio
                            (y - cameraPosition.y) + screen.center.y());
                }
            }
        }

        private void loop() {
            boolean goOn = true;
            while (goOn) {
                String pressedKey = screen.prompt("Press WASD keys to Move, Q to Quit",
                        "^[wasdqWASDQ]+$").toLowerCase();
                if (pressedKey.startsWith("q")) {
                    goOn = !confirmQuit();
                    continue;
                }
                switch (pressedKey.substring(0, 1)) {
                    case "w" -> cameraPosition.y--;
                    case "a" -> cameraPosition.x--;
                    case "s" -> cameraPosition.y++;
                    case "d" -> cameraPosition.x++;
                }
                screen.clear();
                renderMap();
            }
        }

        private boolean confirmQuit() {
            String input = screen.prompt("Do you really want to quit? (press Y to confirm)");
            return input.toLowerCase().startsWith("y");
        }
    }

    static final class Position {
        int x, y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static final class Render {
        char glyph;

        public Render(char glyph) {
            this.glyph = glyph;
        }
    }
}

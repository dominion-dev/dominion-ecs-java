/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.examples.dark;

import dev.dominion.ecs.examples.dark.MapModelBuilder.Tile;

public final class DarkEntities {

    // don't print Dominion's info on startup
    static {
        System.setProperty("dominion.show-banner", "false");
    }

    public static void main(String[] args) {
        Screen screen = new Screen(100, 20);
        menu(screen);
//        new Game("jumpixel", screen);
    }

    private static void menu(Screen screen) {
        screen.drawRect(0, 0, screen.width, screen.height);
        screen.drawText("Dark Entities", screen.center.x, screen.center.y, Screen.TextAlignment.CENTER);
        String playerName = screen.prompt("What's your name, Hero?", "^[a-zA-Z0-9-_]+$");
        String input = screen.prompt(String.format("Hello %s, are you ready for the darkness? (press Y to confirm)", playerName));
        if (input.startsWith("y")) {
            new Game(playerName, screen);
        }
    }

    private static class Game {
        private final Screen screen;
        private final Tile[][] mapModel;
        private final Position cameraPosition = new Position(10, 10);

        Game(String playerName, Screen screen) {
            this.screen = screen;
            this.mapModel = MapModelBuilder.build(20, 20, 1);
            screen.clear();
            renderMap();
            screen.drawText(String.format("Hello %s, your adventure starts here!", playerName)
                    , screen.center.x, 5, Screen.TextAlignment.CENTER);
            loop();
        }

        private void renderMap() {
            for (int y = 0; y < mapModel.length; y++) {
                for (int x = 0; x < mapModel[y].length; x++) {
                    screen.drawGlyph(switch (mapModel[y][x]) {
                        case WALL -> '#';
                        case FLOOR -> ':';
                    }, (x - cameraPosition.x) + screen.center.x, (y - cameraPosition.y) + screen.center.y);
                }
            }
        }

        private void loop() {
            boolean goOn = true;
            while (goOn) {
                String pressedKey = screen.prompt("Press WASD keys to Move, Q to Quit", "^[wasdq]+$");
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
            return input.startsWith("y");
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

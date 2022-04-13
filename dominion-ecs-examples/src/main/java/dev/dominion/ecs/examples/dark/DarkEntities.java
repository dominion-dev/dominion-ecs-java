/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.examples.dark;

public final class DarkEntities {

    // don't print Dominion's info on startup
    static {
        System.setProperty("dominion.show-banner", "false");
    }

    public static void main(String[] args) {
        Screen screen = new Screen(100, 20);
        menu(screen);
    }


    private static void menu(Screen screen) {
        screen.drawRect(0, 0, screen.width, screen.height);
        screen.drawText("Dark Entities", screen.width >> 1, screen.height >> 1, Screen.TextAlignment.CENTER);
        String playerName = screen.prompt("What's your name, Hero?", "^[a-zA-Z0-9-_]+$");
        String input = screen.prompt(String.format("Hello %s, are you ready to go? (press Y + ENTER)", playerName));
        if (input.startsWith("y")) {
            new Game(playerName, screen);
        }

    }

    private static class Game {
        private final Screen screen;

        Game(String playerName, Screen screen) {
            this.screen = screen;
            screen.clear();
            screen.drawText(String.format("Hello %s, your adventure starts here!", playerName), screen.width >> 1, screen.height >> 1, Screen.TextAlignment.CENTER);
            loop();
        }

        private void loop() {
            for (; ; ) {
                String pressedKey = screen.prompt("Press WASD keys to Move, Q to Quit", "^[wasdq]+$");
                screen.clear();
                if (pressedKey.startsWith("q")) {
                    break;
                }
                String moveTo = switch (pressedKey.substring(0, 1)) {
                    case "w" -> "up";
                    case "a" -> "left";
                    case "s" -> "down";
                    case "d" -> "right";
                    default -> "unknown";
                };
                screen.drawText(String.format("Move to %s!", moveTo), screen.width >> 1, screen.height >> 1, Screen.TextAlignment.CENTER);
            }
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
        char texture;

        public Render(char texture) {
            this.texture = texture;
        }
    }

}

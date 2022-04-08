/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.examples.dark;

import java.io.IOException;
import java.util.Arrays;

public final class DarkEntities {

    // don't print Dominion's info on startup
    static {
        System.setProperty("dominion.show-banner", "false");
    }

    public static void main(String[] args) {
        Screen screen = new Screen(100, 20);
        screen.drawText("Hello Dominion", 2, 10, Alignment.LEFT);
        screen.drawText("Hello Dominion", 2, 11, Alignment.CENTER);
        screen.drawText("Hello Dominion", 2, 12, Alignment.RIGHT);
        screen.drawRect(0, 0, screen.width, screen.height);
        screen.update();
    }

    enum Alignment {
        LEFT,
        CENTER,
        RIGHT
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

    static final class Screen {
        private final ProcessBuilder clearTermBuilder = System.getProperty("os.name").contains("Win") ?
                new ProcessBuilder("cmd", "/c", "cls") :
                new ProcessBuilder("clear");

        private final int width;
        private final int height;
        private final char[][] buffer;

        public Screen(int width, int height) {
            this.width = width;
            this.height = height;
            buffer = new char[height][width];
            clear();
        }

        public void clear() {
            for (char[] row : buffer) {
                Arrays.fill(row, ' ');
            }
        }

        public void drawText(String text, int x, int y, Alignment alignment) {
            if (y < 0 || y >= height) {
                return;
            }
            x -= switch (alignment) {
                case LEFT -> 0;
                case RIGHT -> text.length();
                case CENTER -> text.length() >> 1;
            };
            int srcBegin = Math.max(0, -x);
            int srcEnd = text.length() + Math.min(0, width - (x + text.length()));
            int textLength = srcEnd - srcBegin;
            if (textLength > 0) {
                text.getChars(srcBegin, srcEnd, buffer[y], x + srcBegin);
            }
        }

        public void drawRect(int x, int y, int width, int height) {
            if (x < 0 || x >= this.width
                    || y < 0 || y >= this.height
                    || width <= 0 || x + width > this.width
                    || height <= 0 || y + height > this.height
            ) {
                return;
            }
            char horizontal = '-', vertical = '|';
            Arrays.fill(buffer[y], x, x + width, horizontal);
            Arrays.fill(buffer[y + height - 1], x, x + width, horizontal);
            for (int i = y + 1; i < y + height - 1; i++) {
                buffer[i][x] = vertical;
                buffer[i][x + width - 1] = vertical;
            }
        }

        public void update() {
            clearTerm();
            for (char[] row : buffer) {
                System.out.println(row);
            }
        }

        private void clearTerm() {
            try {
                Process clearTerm = clearTermBuilder.inheritIO().start();
                clearTerm.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

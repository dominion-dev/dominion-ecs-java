/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.examples.dark;

import java.io.IOException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Pattern;

final class Screen {
    public final int width;
    public final int height;

    private final ProcessBuilder clearTermBuilder = System.getProperty("os.name").contains("Win") ? new ProcessBuilder("cmd", "/c", "cls") : new ProcessBuilder("clear");
    private final char[][] buffer;
    private Scanner scanner;
    private Prompt prompt;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        buffer = new char[height][width];
        scanner = newScanner();
        clear();
    }

    private Scanner newScanner() {
        return new Scanner(System.in);
    }

    public void clear() {
        for (char[] row : buffer) {
            Arrays.fill(row, ' ');
        }
    }

    public void drawText(String text, int x, int y, TextAlignment alignment) {
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
        if (x < 0 || x >= this.width || y < 0 || y >= this.height || width <= 0 || x + width > this.width || height <= 0 || y + height > this.height) {
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

    public String prompt(String outMessage) {
        return prompt(outMessage, null);
    }

    public String prompt(String outMessage, String inPattern) {
        prompt = new Prompt(outMessage, inPattern == null ? null : Pattern.compile(inPattern, Pattern.MULTILINE));
        return update();
    }

    public String update() {
        clearTerm();
        for (char[] row : buffer) {
            System.out.println(row);
        }
        if (prompt == null) {
            return null;
        }
        System.out.printf("\n> %s %s ", prompt.err, prompt.outMessage);
        try {
            String input = prompt.inPattern == null ? scanner.next() : scanner.next(prompt.inPattern);
            scanner = newScanner();
            return input;
        } catch (InputMismatchException e) {
            prompt = new Prompt(prompt.outMessage, prompt.inPattern, String.format("'%s' is not good.. ", scanner.next()));
            scanner = newScanner();
            return update();
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

    enum TextAlignment {
        LEFT, CENTER, RIGHT
    }

    record Prompt(String outMessage, Pattern inPattern, String err) {
        public Prompt(String outMessage, Pattern inPattern) {
            this(outMessage, inPattern, "");
        }
    }
}

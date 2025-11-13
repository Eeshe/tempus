package me.eeshe.tempus.util;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

public class CursorUtil {

  public static void attemptCursorMovement(Screen screen, KeyStroke keyStroke) {
    if (screen == null) {
      return;
    }
    if (keyStroke == null) {
      return;
    }
    switch (keyStroke.getKeyType()) {
      case ArrowUp -> {
        moveCursorUp(screen);
      }
      case ArrowDown -> {
        moveCursorDown(screen);
      }
      case ArrowLeft -> {
        moveCursorLeft(screen);
      }
      case ArrowRight -> {
        moveCursorRight(screen);
      }
      case Character -> {
        char character = keyStroke.getCharacter();
        switch (character) {
          case 'h' -> {
            moveCursorLeft(screen);
          }
          case 'j' -> {
            moveCursorDown(screen);
          }
          case 'k' -> {
            moveCursorUp(screen);
          }
          case 'l' -> {
            moveCursorRight(screen);
          }
        }
      }
      default -> {

      }
    }
  }

  private static void moveCursorUp(Screen screen) {
    screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(-1));
  }

  private static void moveCursorDown(Screen screen) {
    screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(1));
  }

  private static void moveCursorLeft(Screen screen) {
    screen.setCursorPosition(screen.getCursorPosition().withRelativeColumn(-1));
  }

  private static void moveCursorRight(Screen screen) {
    screen.setCursorPosition(screen.getCursorPosition().withRelativeColumn(1));
  }
}

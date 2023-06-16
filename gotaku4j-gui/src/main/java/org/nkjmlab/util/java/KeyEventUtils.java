package org.nkjmlab.util.java;

import static java.awt.event.InputEvent.*;
import java.awt.event.KeyEvent;

public class KeyEventUtils {

  public static String getKeyName(KeyEvent keyEvent) {
    return KeyEventUtils.getKeyName(keyEvent.getKeyCode(), keyEvent.getModifiersEx());
  }

  public static String getKeyName(int keyCode, int modifiersEx) {
    String mod = KeyEvent.getModifiersExText(modifiersEx);
    String key = KeyEvent.getKeyText(keyCode);
    return mod.length() == 0 ? key : mod + "+" + key;
  }

  public static final String SHIFT = KeyEvent.getModifiersExText(SHIFT_DOWN_MASK);

  public static String getKeyCharString(char keyChar) {
    return String.valueOf(keyChar);
  }

}

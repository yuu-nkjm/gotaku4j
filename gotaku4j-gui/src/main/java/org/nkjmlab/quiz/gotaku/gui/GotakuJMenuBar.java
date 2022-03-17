package org.nkjmlab.quiz.gotaku.gui;

import static java.awt.event.KeyEvent.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.apache.logging.log4j.LogManager;
import org.nkjmlab.util.java.KeyEventUtils;


public class GotakuJMenuBar extends JMenuBar {
  private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();

  public GotakuJMenuBar() {
    {
      JMenu menu = new JMenu("操作 (F)");
      menu.setMnemonic(VK_F);
      add(menu);
    }
    {
      JMenu menu = new JMenu("選択 (S)");
      menu.setMnemonic(VK_S);
      addToMenu(menu, "反転", VK_R, e -> log.debug("反転 {}", e));
      addToMenu(menu, "同名", VK_Y, e -> log.debug("同名 {}", e));
      add(menu);
    }
    {
      JMenu menu = new JMenu("編集 (E)");
      menu.setMnemonic(VK_E);
      add(menu);
    }
    {
      JMenu menu = new JMenu("表示 (V)");
      menu.setMnemonic(VK_V);
      add(menu);
    }
    {
      JMenu menu = new JMenu("機器 (D)");
      menu.setMnemonic(VK_D);
      add(menu);
    }
    {
      JMenu menu = new JMenu("説明 (H)");
      menu.setMnemonic(VK_H);
      add(menu);
    }

  }

  private void addToMenu(JMenu menu, String text, int keyCode, Consumer<ActionEvent> action) {
    JMenuItem item = new JMenuItem(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        action.accept(e);
      }
    });
    item.setText(text + " (" + KeyEventUtils.getKeyName(keyCode, 0) + ")");
    item.setMnemonic(keyCode);
    menu.add(item);
  }

  // @Override
  // public void processKeyEvent(KeyEvent e, MenuElement path[], MenuSelectionManager manager) {}

  @Override
  public void menuSelectionChanged(boolean isIncluded) {
    log.debug("menuSelectionChanged={}", isIncluded);
    setVisible(isIncluded);
  }

}

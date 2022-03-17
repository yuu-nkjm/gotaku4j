package org.nkjmlab.quiz.gotaku.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.logging.log4j.LogManager;

public class GotakuGuiApplication extends JFrame {
  private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();

  static {
    UIManager.put("OptionPane.okButtonText", "OK");
    UIManager.put("OptionPane.cancelButtonText", "Quit");
    UIManager.put("OptionPane.okButtonMnemonic", "79"); // 'O'
    UIManager.put("OptionPane.cancelButtonMnemonic", "81");// 'Q'

    try {
      String lf = UIManager.getSystemLookAndFeelClassName();
      UIManager.setLookAndFeel(lf);
    } catch (Exception e) {
      log.error(e, e);
    }
    log.info("file.encoding={}, defaultCharset={}, VMArgs={}", System.getProperty("file.encoding"),
        Charset.defaultCharset(), ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
            .collect(Collectors.toList()));
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new GotakuGuiApplication());
  }

  private GotakuJMenuBar jMenuBar;

  public GotakuGuiApplication() {
    super("ごたく");
    Dimension preffiedSize = new Dimension(1000, 600);
    setSize(preffiedSize);
    setPreferredSize(preffiedSize);

    {
      this.jMenuBar = new GotakuJMenuBar();
      setJMenuBar(jMenuBar);
      jMenuBar.setVisible(false);

    }

    setMinimumSize(new Dimension(200, 100));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    getContentPane().setBackground(Color.white);


    {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBackground(Color.white);
      panel.add(Box.createGlue());

      {
        JPanel subpanel = new JPanel();
        subpanel.setLayout(new BorderLayout());
        JLabel label = new JLabel();
        label.setFont(new Font("UD デジタル 教科書体 N-R", Font.PLAIN, 20));
        label.setText("<html><p>享保の改革を推進したのは誰？享保の改革を推進したのは誰？</p><p>享保の改革を推進したのは誰？</p></html>");
        subpanel.add(label, BorderLayout.CENTER);
        {
          JLabel label2 = new JLabel();
          label2.setFont(new Font("UD デジタル 教科書体 N-R", Font.PLAIN, 20));
          label2.setText("<html><h2>問題</h2></html>");
          subpanel.add(label2, BorderLayout.NORTH);
        }
        panel.add(subpanel);

      }
      {
        JList<String> jList = new JList<>(new String[] {"1: Blue", "2: Green", "3: Red",
            "4: White111111111111111111111", "5: Black"});
        jList.setFont(new Font("UD デジタル 教科書体 N-R", Font.PLAIN, 20));
        jList.setPreferredSize(new Dimension(400, 200));

        panel.add(jList);
      }
      getContentPane().add(panel, BorderLayout.CENTER);
    }


    {
      JPanel panel = new JPanel();
      panel.setBackground(Color.yellow);
      panel.setPreferredSize(new Dimension(200, 200));
      JLabel label = new JLabel();
      label.setFont(new Font("UD デジタル 教科書体 N-R", Font.PLAIN, 20));
      label.setText("<html><h2>得点</h2></html>");
      panel.add(label);
      getContentPane().add(panel, BorderLayout.WEST);
    }
    {
      JPanel panel = new JPanel();
      panel.setBackground(Color.yellow);
      panel.setPreferredSize(new Dimension(200, 200));
      {
        JLabel label = new JLabel();
        label.setFont(new Font("UD デジタル 教科書体 N-R", Font.PLAIN, 20));
        label.setText("<html><h2>ステージ1</h2></html>");
        panel.add(label);
      }
      {
        JLabel label = new JLabel();
        label.setFont(new Font("UD デジタル 教科書体 N-R", Font.PLAIN, 20));
        label.setText("<html><h2>ボーダーライン</h2></html>");
        panel.add(label);
      }
      getContentPane().add(panel, BorderLayout.EAST);
    }


    pack();
    setVisible(true);



  }


}

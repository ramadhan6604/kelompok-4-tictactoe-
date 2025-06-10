import javax.swing.*;
import java.awt.*;

public class GameClientUI extends JFrame {
    protected JButton[] buttons = new JButton[9];
    protected String myMark = "X";
    protected boolean myTurn = false;
    protected boolean gameOver = false;

    public GameClientUI(String title) {
        setTitle(title);
        setSize(300, 300);
        setLayout(new GridLayout(3, 3));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        for (int i = 0; i < 9; i++) {
            int index = i;
            buttons[i] = new JButton("");
            buttons[i].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 60));
            buttons[i].addActionListener(e -> handleButtonClick(index));
            add(buttons[i]);
        }

        setVisible(true);
    }

    protected void handleButtonClick(int index) {
        // Override in subclass (GameClient)
    }

    protected String get(int i) {
        return buttons[i].getText();
    }

    protected void reset() {
        for (JButton b : buttons) {
            b.setText("");
        }
        myTurn = myMark.equals("X");
    }
}

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class GameClient extends JFrame {
    private JButton[] buttons = new JButton[9];
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private String myMark = "X";
    private boolean myTurn = false;
    private boolean gameOver = false;

    public GameClient(String serverIp, String playerId) throws IOException {
        setTitle("Tic-Tac-Toe Client - " + playerId);
        setSize(300, 300);
        setLayout(new GridLayout(3, 3));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Board buttons
        for (int i = 0; i < 9; i++) {
            int index = i;
            buttons[i] = new JButton("");
            buttons[i].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 60));
            buttons[i].addActionListener(e -> {
                if (myTurn && buttons[index].getText().equals("") && !gameOver) {
                    buttons[index].setText(myMark);
                    out.println("MOVE:" + index);
                    myTurn = false;
                    checkWin();
                }
            });
            add(buttons[i]);
        }

        socket = new Socket(serverIp, 5000);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Kirim ID player ke server
        out.println("PLAYER_ID:" + playerId);

        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("MARK:")) {
                        myMark = line.substring(5);
                        setTitle("You are: " + myMark + " (" + playerId + ")");
                    } else if (line.equals("TURN")) {
                        myTurn = true;
                    } else if (line.startsWith("MOVE:")) {
                        int index = Integer.parseInt(line.substring(5));
                        buttons[index].setText(myMark.equals("X") ? "O" : "X");
                        myTurn = true;
                        checkWin();
                    }
                }
            } catch (IOException e) {
                if (!gameOver) {
                    JOptionPane.showMessageDialog(this, "Connection lost.");
                    System.exit(0);
                }
            }
        }).start();
setLocationRelativeTo(null);
        setVisible(true);
    }

    private void checkWin() {
        String[][] combos = {
            {get(0), get(1), get(2)},
            {get(3), get(4), get(5)},
            {get(6), get(7), get(8)},
            {get(0), get(3), get(6)},
            {get(1), get(4), get(7)},
            {get(2), get(5), get(8)},
            {get(0), get(4), get(8)},
            {get(2), get(4), get(6)},
        };

        for (String[] combo : combos) {
            if (combo[0].equals(combo[1]) && combo[1].equals(combo[2]) && !combo[0].equals("")) {
                JOptionPane.showMessageDialog(this, combo[0] + " wins!");
                gameOver = true;
                closeConnection();
                return;
            }
        }

        boolean full = true;
        for (JButton b : buttons) {
            if (b.getText().equals("")) {
                full = false;
                break;
            }
        }

        if (full) {
            JOptionPane.showMessageDialog(this, "Draw!");
            reset();
        }
    }

    private String get(int i) {
        return buttons[i].getText();
    }

    private void reset() {
        for (JButton b : buttons) {
            b.setText("");
        }
        myTurn = myMark.equals("X");
    }

    private void closeConnection() {
    try {
        if (out != null) out.close();
        if (in != null) in.close();
        if (socket != null && !socket.isClosed()) socket.close();

        // Jalankan ulang GameClient (restart otomatis)
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        File currentJar = new File(GameClient.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        // Jika file bukan JAR, berarti running dari IDE - restart manual
        if (!currentJar.getName().endsWith(".jar")) {
            JOptionPane.showMessageDialog(this, "Game selesai. Silakan jalankan ulang dari IDE.");
        } else {
            ProcessBuilder builder = new ProcessBuilder(javaBin, "-jar", currentJar.getPath());
            builder.start();
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        System.exit(0); // Tutup aplikasi sekarang
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pilih Pemain");
            String[] players = {"Player 1", "Player 2", "Player 3"};
            JComboBox<String> playerList = new JComboBox<>(players);
            JButton connectButton = new JButton("Connect");

            connectButton.addActionListener(e -> {
                String selectedPlayer = (String) playerList.getSelectedItem();
                frame.dispose();
                try {
                    String ip = "192.168.48.71"; // Ganti jika perlu
                    new GameClient(ip, selectedPlayer);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Tidak dapat terhubung ke server.");
                }
            });

            frame.setLayout(new FlowLayout());
            frame.add(new JLabel("Pilih Sebagai:"));
            frame.add(playerList);
            frame.add(connectButton);
            frame.setSize(250, 100);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameServer {
    private static JTextArea logArea;
    private static GameViewer gameViewer; // Tambahan

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Tic-Tac-Toe Server Log");
        logArea = new JTextArea(20, 40);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        gameViewer = new GameViewer(); // Inisialisasi viewer
        gameViewer.setVisible(true);

        ServerSocket serverSocket = new ServerSocket(5000);
        logToGui("Server started. Waiting for players...");

        while (true) {
            try {
                logToGui("Waiting for Player 1...");
                Socket player1 = serverSocket.accept();
                logToGui("Player 1 connected.");

                logToGui("Waiting for Player 2...");
                Socket player2 = serverSocket.accept();
                logToGui("Player 2 connected.");

                new Thread(new GameSession(player1, player2)).start();
            } catch (IOException e) {
                logToGui("Error accepting players: " + e.getMessage());
            }
        }
    }

    static class GameSession implements Runnable {
        private Socket p1, p2;
        private BufferedReader in1, in2;
        private PrintWriter out1, out2;
        private String[] board = new String[9];

        public GameSession(Socket p1, Socket p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        public void run() {
            try {
                in1 = new BufferedReader(new InputStreamReader(p1.getInputStream()));
                out1 = new PrintWriter(p1.getOutputStream(), true);
                in2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
                out2 = new PrintWriter(p2.getOutputStream(), true);

                for (int i = 0; i < 9; i++) board[i] = "";

                out1.println("MARK:X");
                out2.println("MARK:O");
                out1.println("TURN");

                logToGui("Game started between Player 1 and Player 2.");
                gameViewer.resetBoard(); // Reset viewer

                Thread t1 = new Thread(() -> relay("Player 1", in1, out2, "X"));
                Thread t2 = new Thread(() -> relay("Player 2", in2, out1, "O"));
                t1.start();
                t2.start();

                t1.join();
                t2.join();

            } catch (Exception e) {
                logToGui("Game session error: " + e.getMessage());
            } finally {
                try { p1.close(); } catch (IOException ignored) {}
                try { p2.close(); } catch (IOException ignored) {}
                logToGui("Game session ended.");
            }
        }

        private void relay(String player, BufferedReader in, PrintWriter out, String mark) {
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    logToGui(player + " sent: " + line);
                    if (line.startsWith("MOVE:")) {
                        int index = Integer.parseInt(line.substring(5));
                        board[index] = mark;
                        gameViewer.updateBoard(index, mark);

                        String winner = checkWinner();
                        if (winner != null) {
                            logToGui("Winner is: " + winner);
                            gameViewer.showWinner(winner);
                            break;
                        }
                    }
                    out.println(line);
                }
            } catch (IOException e) {
                logToGui(player + " disconnected.");
            }
        }

        private String checkWinner() {
            int[][] winCombos = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
            };
            for (int[] combo : winCombos) {
                if (!board[combo[0]].equals("") &&
                    board[combo[0]].equals(board[combo[1]]) &&
                    board[combo[1]].equals(board[combo[2]])) {
                    return board[combo[0]];
                }
            }
            return null;
        }
    }

    private static void logToGui(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
        });
    }
}

class GameViewer extends JFrame {
    private JButton[] cells = new JButton[9];
    private JLabel statusLabel;

    public GameViewer() {
        setTitle("Tic-Tac-Toe Spectator");
        setSize(320, 380);
        setLayout(new BorderLayout());
        JPanel board = new JPanel(new GridLayout(3, 3));

        for (int i = 0; i < 9; i++) {
            cells[i] = new JButton("");
            cells[i].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
            cells[i].setEnabled(false);
            board.add(cells[i]);
        }

        statusLabel = new JLabel("Waiting for game...", SwingConstants.CENTER);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));

        add(statusLabel, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void updateBoard(int index, String mark) {
        SwingUtilities.invokeLater(() -> cells[index].setText(mark));
    }

    public void resetBoard() {
        SwingUtilities.invokeLater(() -> {
            for (JButton cell : cells) {
                cell.setText("");
            }
            statusLabel.setText("Game in progress...");
        });
    }

    public void showWinner(String winner) {
        SwingUtilities.invokeLater(() -> statusLabel.setText("Winner: " + winner));
    }
}

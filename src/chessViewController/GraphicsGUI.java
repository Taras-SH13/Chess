package chessViewController;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import chessModel.Board;
import chessModel.Game;
import chessModel.HumanPlayer;
import chessModel.Player;

@SuppressWarnings("serial")
public class GraphicsGUI extends JFrame {
	private JMenuBar menu;
	private JMenu file;
	private JMenuItem save, details;
	private Game game;
	private ChessView chessView;
	JLabel timer1Label;
	JLabel timer2Label;
	JLabel player1Score;
	JLabel player2Score;

	public GraphicsGUI(int gameMode, Player player1, Player player2) {

		// this.setResizable(false);
		this.setMinimumSize(new Dimension(300, 300));

		setVisible(true);

		// Set to the native look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Could not find native look and feel.");
		}

		centerWindow();

		this.setLayout(new BorderLayout());

		menu = new JMenuBar();
		file = new JMenu("File");
		save = new JMenuItem("Save...");
		details = new JMenuItem("View...");
		menu.add(file);
		file.add(save);
		file.add(details);

		this.setJMenuBar(menu);

		game = new Game(gameMode, player1, player2);

		final Board board = game.getBoard();

		chessView = new ChessView(board);

		if (player2 instanceof HumanPlayer) {
			((HumanPlayer) player2).setupView(this, chessView);
		}
		if (player1 instanceof HumanPlayer) {
			((HumanPlayer) player1).setupView(this, chessView);
		}
		

		// Timer/Score area stuff
		timer1Label = new JLabel("", SwingConstants.CENTER);
		timer2Label = new JLabel("", SwingConstants.CENTER);
		player1Score = new JLabel("", SwingConstants.CENTER);
		player2Score = new JLabel("", SwingConstants.CENTER);
		Timer updateTimer = new Timer(100, new ActionListener() { // Updated 10
																	// times a
																	// second
			public void actionPerformed(ActionEvent e) {
				if (game.getCurrentSide() == 0) {
					timer1Label.setText("<html>P1 Time: <font color='red'>" + game.getPlayer1Time() + "</font></html>");
					timer2Label.setText("<html>P2 Time: " + game.getPlayer2Time() + "</html>");
				} else if (game.getCurrentSide() == 1) {
					timer1Label.setText("<html>P1 Time: " + game.getPlayer1Time() + "</html>");
					timer2Label.setText("<html>P2 Time: <font color='red'>" + game.getPlayer2Time() + "</font></html>");
				}
				player1Score.setText("Score: " + game.getPlayer1Score());
				player2Score.setText("Score: " + game.getPlayer2Score());
				chessView.repaint();
			}
		});
		updateTimer.start();
		JPanel timerScorePanel = new JPanel();
		timerScorePanel.setLayout(new GridLayout(1, 4));
		timerScorePanel.add(timer1Label);
		timerScorePanel.add(player1Score);
		timerScorePanel.add(player2Score);
		timerScorePanel.add(timer2Label);
		this.add(timerScorePanel, BorderLayout.SOUTH);

		this.add(chessView, BorderLayout.CENTER);

		pack();

		centerWindow();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		save.addActionListener(new ActionListener() {

			// @Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				File f = new File(System.getProperty("user.home"));
				fc.setFileFilter(new FileNameExtensionFilter("Text File", "txt"));
				f.mkdirs();
				fc.setCurrentDirectory(f);
				fc.showSaveDialog(fc.getParent());
				try {
					PrintWriter pw = new PrintWriter(fc.getSelectedFile());
					StringBuilder log = new StringBuilder();
					log.append(board.getPGN());
					pw.write(log.toString());
					pw.close();
				} catch (FileNotFoundException e1) {
					popup("Unable to write file");
				}
			}
		});

		details.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel p = new JPanel();
				p.setLayout(new BorderLayout());

				JTextArea pgn = new JTextArea("PGN:\n" + board.getPGN(), 8, 35);
				pgn.setEditable(false);

				JScrollPane pgnScrollPane = new JScrollPane(pgn);
				pgnScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

				JTextArea fen = new JTextArea("\nFen:\n" + board.getFEN() + "\n");
				fen.setEditable(false);
				fen.setForeground(new Color(22, 22, 200));

				JTextArea raw = new JTextArea(4, 35);
				raw.setText("Raw Moves:\n" + board.getLogRaw());
				raw.setEditable(false);

				JScrollPane rawScrollPane = new JScrollPane(raw);
				rawScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

				p.add(pgnScrollPane, BorderLayout.NORTH);
				p.add(fen, BorderLayout.CENTER);
				p.add(rawScrollPane, BorderLayout.SOUTH);

				JOptionPane.showMessageDialog(null, p, "Details", JOptionPane.PLAIN_MESSAGE);
			}
		});

	}

	/**
	 * 
	 */
	private void centerWindow() {
		this.setSize(424, 500);

		Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) ((screenDimensions.getWidth() - getWidth()) / 2),
				(int) ((screenDimensions.getHeight() - getHeight()) / 2));
	}

	public void isOver() {
		if (game.isCheckMate()) {
			JOptionPane.showMessageDialog(this, game.getCurrentSide() + " has lost from checkmate");
		} else if (game.isDraw()) {
			JOptionPane.showMessageDialog(this, "It's a draw!");
		} else if (game.getPlayer1Time().equals("0:0") || game.getPlayer2Time().equals("0:0")) {
			JOptionPane.showMessageDialog(this, "Time's Up!");
		}
	}

	public Integer[] handleLocationClicked(int xLoc, int yLoc) {
		if (chessView.getSelected() == null) {
			chessView.setSelected(game.getBoard().getPiece(xLoc, yLoc));
			chessView.repaint();
			return null;
		} else {
			Integer[] move = new Integer[4];
			move[0] = chessView.getSelected().getX();
			move[1] = chessView.getSelected().getY();
			move[2] = xLoc;
			move[3] = yLoc;
			chessView.setSelected(null);
			chessView.repaint();
			return move;
		}
	}

	public static void popup(String message) {
		JOptionPane.showMessageDialog(null, message, "", JOptionPane.PLAIN_MESSAGE);
	}
	
	public int getCurrentSide(){
		return game.getCurrentSide();
	}
}

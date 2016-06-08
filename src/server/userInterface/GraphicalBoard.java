package server.userInterface;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import server.Board;
import server.Card;
import server.Position;

public class GraphicalBoard extends JPanel {

	private static final long serialVersionUID = -8826989344981510486L;
	private GraphicalCardBuffered[][] boardDisplay = null;
	private TexturePaint paintBuffer = null;
	@SuppressWarnings("unused")
	private Board b;

	public GraphicalBoard() {
		super();
		GridLayout BoardPaneLayout = new GridLayout(7, 7);
		BoardPaneLayout.setHgap(0);
		BoardPaneLayout.setVgap(0);
		setLayout(BoardPaneLayout);
		setPreferredSize(new java.awt.Dimension(700, 700));
		setBounds(0, 0, 700, 700);
		boardDisplay = new GraphicalCardBuffered[7][7];
		for (int i = 0; i < boardDisplay.length; i++) {
			for (int j = 0; j < boardDisplay[i].length; j++) {
				boardDisplay[i][j] = new GraphicalCardBuffered();
				add(boardDisplay[i][j]);
			}

		}
	}

	@Override
	public void setSize(int width, int height) {
		for (int i = 0; i < boardDisplay.length; i++) {
			for (int j = 0; j < boardDisplay[i].length; j++) {
				boardDisplay[i][j].setSize(height / 7, width / 7);
			}
		}
	}

	@Override
	public void setSize(Dimension d) {
		setSize(d.width, d.height);
	}

	public void updateBoard(Board b) {
		for (int i = 0; i < boardDisplay.length; i++) {
			for (int j = 0; j < boardDisplay[i].length; j++) {
				Card c = new Card(b.getCard(i, j));
				boardDisplay[i][j].setCard(c);
			}

		}
		updatePaint();
		repaint();
		this.b = b;
	}

	private void updatePaint() {
		if (boardDisplay == null) {
			paintBuffer = null;
			return;
		}
		int w = getWidth();
		int h = getHeight();
		if (w <= 0 || h <= 0) {
			paintBuffer = null;
			return;
		}
		BufferedImage buff = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = buff.createGraphics();
		super.paintComponent(g2);
		paintBuffer = new TexturePaint(buff, new Rectangle(0, 0, w, h));
		g2.dispose();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (paintBuffer != null) {
			int w = getWidth();
			int h = getHeight();
			Insets in = getInsets();

			int x = in.left;
			int y = in.top;
			w = w - in.left - in.right;
			h = h - in.top - in.bottom;

			if (w >= 0 && h >= 0) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setPaint(paintBuffer);
				g2.fillRect(x, y, w, h);
			}
		}
	}

	public void blink(Position insertPos, long millis) {
		boardDisplay[insertPos.getRow()][insertPos.getCol()].blinkCard(millis,
				3);
	}

	// public void blink(Integer playerID,long millis) {
	// Position p=new Position( b.findPlayer(playerID) );
	// boardDisplay[p.getRow()][p.getCol()].blinkPlayer(millis, 3);
	// }

}

package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class Game2048 extends JPanel {
    private static final int SIZE = 4;
    private static final int TILE_SIZE = 100;
    private static final int TILE_MARGIN = 16;
    private static final Color BG_COLOR = new Color(0xbbada0);
    private static final String FONT_NAME = "Arial";
    private static final int[] TILE_COLORS = {
            0xcdc1b4, 0xeee4da, 0xede0c8, 0xf2b179,
            0xf59563, 0xf67c5f, 0xf65e3b, 0xedcf72,
            0xedcc61, 0xedc850, 0xedc53f, 0xedc22e
    };

    private Tile[][] tiles;
    private boolean won = false;
    private boolean lost = false;

    public Game2048() {
        setPreferredSize(new Dimension(SIZE * (TILE_SIZE + TILE_MARGIN) + TILE_MARGIN,
                SIZE * (TILE_SIZE + TILE_MARGIN) + TILE_MARGIN));
        setBackground(BG_COLOR);
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!won && !lost) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            moveLeft();
                            break;
                        case KeyEvent.VK_RIGHT:
                            moveRight();
                            break;
                        case KeyEvent.VK_UP:
                            moveUp();
                            break;
                        case KeyEvent.VK_DOWN:
                            moveDown();
                            break;
                    }
                    checkGameState();
                }
                repaint();
            }
        });
        resetGame();
    }

    public void resetGame() {
        tiles = new Tile[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                tiles[y][x] = new Tile();
            }
        }
        addTile();
        addTile();
        won = false;
        lost = false;
    }

    public void addTile() {
        Random rand = new Random();
        int x, y;
        do {
            x = rand.nextInt(SIZE);
            y = rand.nextInt(SIZE);
        } while (!tiles[y][x].isEmpty());

        tiles[y][x].value = rand.nextInt(10) == 0 ? 4 : 2;
    }

    public void moveLeft() {
        boolean needAddTile = false;
        for (int y = 0; y < SIZE; y++) {
            Tile[] row = tiles[y];
            Tile[] compressedRow = compressRow(row);
            Tile[] mergedRow = mergeRow(compressedRow);
            if (!compare(row, mergedRow)) {
                needAddTile = true;
            }
            setRow(y, mergedRow);
        }
        if (needAddTile) {
            addTile();
        }
    }
    public void moveRight() {
        rotate(180);
        moveLeft();
        rotate(180);
    }

    public void moveUp() {
        rotate(270);
        moveLeft();
        rotate(90);
    }

    public void moveDown() {
        rotate(90);
        moveLeft();
        rotate(270);
    }

    private void checkGameState() {
        boolean hasEmptyTile = false;
        boolean canMerge = false;

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (tiles[y][x].isEmpty()) {
                    hasEmptyTile = true;
                }
                if (tiles[y][x].value == 2048) {
                    won = true;
                }
                if (x < SIZE - 1 && tiles[y][x].value == tiles[y][x + 1].value) {
                    canMerge = true;
                }
                if (y < SIZE - 1 && tiles[y][x].value == tiles[y + 1][x].value) {
                    canMerge = true;
                }
            }
        }

        if (!hasEmptyTile && !canMerge) {
            lost = true;
        }
    }

    private boolean compare(Tile[] a, Tile[] b) {
        if (a == b) return true;
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i].value != b[i].value) return false;
        }
        return true;
    }

    private Tile[] compressRow(Tile[] oldRow) {
        Tile[] newRow = new Tile[SIZE];
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            if (!oldRow[i].isEmpty()) {
                newRow[count++] = oldRow[i];
            }
        }
        for (int i = count; i < SIZE; i++) {
            newRow[i] = new Tile();
        }
        return newRow;
    }

    private Tile[] mergeRow(Tile[] oldRow) {
        for (int i = 0; i < SIZE - 1; i++) {
            if (oldRow[i].value == oldRow[i + 1].value) {
                oldRow[i].value *= 2;
                oldRow[i + 1] = new Tile();
            }
        }
        return compressRow(oldRow);
    }

    private void setRow(int row, Tile[] newRow) {
        tiles[row] = newRow;
    }

    private void rotate(int angle) {
        Tile[][] newTiles = new Tile[SIZE][SIZE];
        int offsetX = SIZE - 1, offsetY = SIZE - 1;
        if (angle == 90) {
            offsetX = SIZE - 1;
            offsetY = 0;
        } else if (angle == 270) {
            offsetX = 0;
            offsetY = SIZE - 1;
        }

        double rad = Math.toRadians(angle);
        int cos = (int) Math.cos(rad);
        int sin = (int) Math.sin(rad);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int newX = (x * cos) - (y * sin) + offsetX;
                int newY = (x * sin) + (y * cos) + offsetY;
                newTiles[newY][newX] = tiles[y][x];
            }
        }

        tiles = newTiles;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(BG_COLOR);
        g.fillRect(0, 0, this.getSize().width, this.getSize().height);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                drawTile(g, tiles[y][x], x, y);
            }
        }
    }

    private void drawTile(Graphics g2, Tile tile, int x, int y) {
        Graphics2D g = (Graphics2D) g2;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        int value = tile.value;
        int xOffset = offsetCoors(x);
        int yOffset = offsetCoors(y);

        g.setColor(new Color(TILE_COLORS[Math.min(value, TILE_COLORS.length - 1)]));
        g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 14, 14);
        g.setColor(new Color(0x776e65));
        final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
        final Font font = new Font(FONT_NAME, Font.BOLD, size);
        g.setFont(font);

        String s = String.valueOf(value);
        final FontMetrics fm = getFontMetrics(font);
        final int w = fm.stringWidth(s);
        final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

        if (value != 0) {
            g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE - (TILE_SIZE - h) / 2 - 2);
        }

        if (won || lost) {
            g.setColor(new Color(255, 255, 255, 30));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(78, 139, 202));
            g.setFont(new Font(FONT_NAME, Font.BOLD, 48));
            if (won) {
                g.drawString("You won!", 68, 150);
            }
            if (lost) {
                g.drawString("Game over!", 50, 150);
            }
        }
    }

    private static int offsetCoors(int arg) {
        return arg * (TILE_MARGIN + TILE_SIZE) + TILE_MARGIN;
    }

    private static class Tile {
        int value;

        Tile() {
            this(0);
        }

        Tile(int num) {
            value = num;
        }

        boolean isEmpty() {
            return value == 0;
        }
    }

    public static void main(String[] args) {
        JFrame game = new JFrame();
        game.setTitle("2048 Game");
        game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        game.setSize(520, 580); // Adjusted size to accommodate tiles and margins
        game.setResizable(false);

        game.add(new Game2048(), BorderLayout.CENTER);
        game.setLocationRelativeTo(null);
        game.setVisible(true);
    }
}


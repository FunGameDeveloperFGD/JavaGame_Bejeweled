import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import static java.lang.Math.abs;

class Piece {
    int x, y, row, col, kind, match;

    public Piece() {
        match = 0;
    }
}

public class Game extends JPanel implements Runnable, MouseListener {
    final int WIDTH = 740;
    final int HEIGHT = 480;

    boolean isRunning;
    Thread thread;
    BufferedImage view;

    Piece[][] grid;
    BufferedImage background, gems, cursor;
    MouseEvent mouse;
    int tileSize = 54;
    int offsetX = 48, offsetY = 24;
    int x0, y0, x, y;
    int click = 0;
    int posX, posY;
    int speedSwapAnimation = 4;
    boolean isSwap = false, isMoving = false;

    public Game() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
    }

    public static void main(String[] args) {
        JFrame w = new JFrame("Bejeweled (Match-3)");
        w.setResizable(false);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.add(new Game());
        w.pack();
        w.setLocationRelativeTo(null);
        w.setVisible(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if(thread == null) {
            thread = new Thread(this);
            isRunning = true;
            thread.start();
        }
    }

    public void swap(Piece p1, Piece p2) {
        int rowAux = p1.row;
        p1.row = p2.row;
        p2.row = rowAux;

        int colAux = p1.col;
        p1.col = p2.col;
        p2.col = colAux;

        grid[p1.row][p1.col] = p1;
        grid[p2.row][p2.col] = p2;
    }

    public void start () {
        try {
            view = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            grid = new Piece[10][10];

            background = ImageIO.read(getClass().getResource("/background.png"));
            gems = ImageIO.read(getClass().getResource("/gems.png"));
            cursor = ImageIO.read(getClass().getResource("/cursor.png"));

            for(int i = 0;i < 10;i++) {
                for(int j = 0;j < 10;j++) {
                    grid[i][j] = new Piece();
                }
            }

            for (int i = 1;i <=8;i++) {
                for(int j = 1;j <= 8;j++) {
                    grid[i][j].kind = (new Random().nextInt(7));
                    grid[i][j].row = i;
                    grid[i][j].col = j;
                    grid[i][j].x = j * tileSize;
                    grid[i][j].y = i * tileSize;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update () {
        if (mouse != null && mouse.getID() == MouseEvent.MOUSE_PRESSED) {
            if (mouse.getButton() == MouseEvent.BUTTON1) {
                if(!isSwap && !isMoving) {
                    click++;
                }
                posX = mouse.getX() - offsetX;
                posY = mouse.getY() - offsetY;

                if (click == 1) {
                    x0 = posX / tileSize + 1;
                    y0 = posY / tileSize + 1;
                }
                if (click == 2) {
                    x = posX / tileSize + 1;
                    y = posY / tileSize + 1;
                    if (abs(x - x0) + abs(y - y0) == 1) {
                        swap(grid[y0][x0], grid[y][x]);
                        isSwap = true;
                        click = 0;
                    } else {
                        click = 1;
                    }
                }
            }
            mouse = null;
        }

        //Match finding
        for (int i = 1;i <= 8;i++) {
            for (int j = 1;j <= 8;j++) {
                if (grid[i][j].kind == grid[i + 1][j].kind) {
                    if (grid[i][j].kind == grid[i - 1][j].kind) {
                        for (int n = -1;n <= 1;n++) {
                            grid[i+n][j].match++;
                        }
                    }
                }
                if(grid[i][j].kind == grid[i][j + 1].kind) {
                    if (grid[i][j].kind == grid[i][j - 1].kind) {
                        for(int n = -1;n <= 1;n++) {
                            grid[i][j + n].match++;
                        }
                    }
                }
            }
        }

        //Moving animation
        isMoving = false;
        for(int i = 1;i <= 8;i++) {
            for (int j = 1;j <= 8;j++) {
                Piece p = grid[i][j];
                int dx = 0, dy = 0;
                for(int n = 0;n < speedSwapAnimation;n++) {
                    dx = p.x - p.col * tileSize;
                    dy = p.y - p.row * tileSize;
                    if (dx != 0) {
                        p.x -= dx / abs(dx);
                    }
                    if (dy != 0) {
                        p.y -= dy / abs(dy);
                    }
                }
                if (dx != 0 || dy != 0) {
                    isMoving = true;
                }
            }
        }

        //Get score
        int score = 0;
        for(int i = 1;i <= 8;i++) {
            for(int j = 1;j <= 8;j++) {
                score += grid[i][j].match;
            }
        }

        //Second swap if no match
        if(isSwap && !isMoving) {
            if(score == 0) {
                swap(grid[y0][x0], grid[y][x]);
            }
            isSwap = false;
        }

        //Update grid
        if (!isMoving) {
            for (int i = 8;i > 0;i--) {
                for (int j = 1;j <= 8;j++) {
                    if (grid[i][j].match != 0) {
                        for (int n = i;n > 0;n--) {
                            if (grid[n][j].match == 0) {
                                swap(grid[n][j], grid[i][j]);
                                break;
                            }
                        }
                    }
                }
            }
            for(int j = 1;j <= 8;j++) {
                for(int i = 8, n = 0;i > 0;i--) {
                    if(grid[i][j].match != 0) {
                        grid[i][j].kind = new Random().nextInt(7);
                        grid[i][j].y = -tileSize * n++;
                        grid[i][j].match = 0;
                    }
                }
            }
        }
    }

    public void draw () {
        Graphics2D g2 = (Graphics2D) view.getGraphics();
        g2.drawImage(background, 0, 0, WIDTH, HEIGHT, null);

        for (int i = 1;i <= 8;i++) {
            for (int j = 1;j <= 8;j++) {
                g2.drawImage(
                        gems.getSubimage(grid[i][j].kind * 49, 0, 49, 49),
                        grid[i][j].x + (offsetX - tileSize),
                        grid[i][j].y + (offsetY - tileSize),
                        49,
                        49,
                        null
                );

                //Show cursor
                if(click == 1) {
                    if(x0 == j && y0 == i) {
                        g2.drawImage(
                                cursor,
                                grid[i][j].x + (offsetX - tileSize),
                                grid[i][j].y + (offsetY - tileSize),
                                cursor.getWidth(),
                                cursor.getHeight(),
                                null
                        );
                    }
                }
            }
        }

        Graphics g = getGraphics();
        g.drawImage(view, 0, 0, WIDTH, HEIGHT, null);
        g.dispose();
    }

    @Override
    public void run() {
        try {
            start();
            while(isRunning) {
                update();
                draw();
                Thread.sleep(1000/60);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouse = e;
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
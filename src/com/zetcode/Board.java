package com.zetcode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import javax.sound.sampled.*;
import javax.swing.*;


public class Board extends JPanel implements ActionListener {

    private final int B_WIDTH = 300;
    private final int B_HEIGHT = 300;
    private final int DOT_SIZE = 10;
    private final int ALL_DOTS = 900;
    private final int RAND_POS = 29;
    private final int DELAY = 140;

    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[ALL_DOTS];

    private int dots;
    private int apple_x;
    private int apple_y;

    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;
    private boolean inGame = true;

    private Timer timer;
    private Image ball;
    private Image apple;
    private Image head;

    private int score;	// 점수
    private Image bufferImage;
    private Graphics screenGraphic;

    private Clip clip;
    private Clip clip1;
    AudioInputStream audioStream;

    public Board() {
        initBoard1();

    }
    
    private void initBoard() {

        score = 0;

        playSound("src/com/audio/backgroundMusic.wav", true);

        addKeyListener(new TAdapter());
        setBackground(Color.black);
        setFocusable(true);

        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
        loadImages();
        initGame();

    }
    private void initBoard1() {

        addKeyListener(new TAdapter());
        setBackground(Color.black);
        setFocusable(true);

        setLayout(null);

        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));



        startBt();
        //버튼 집어넣기


    }

    public void startBt() {
        JLabel main_title = new JLabel("Snake Game");
        main_title.setForeground(Color.ORANGE);
        main_title.setFont(main_title.getFont().deriveFont(30.0f));
        main_title.setLocation(65, 100);
        main_title.setSize(200, 50);
        add(main_title);


        JLabel la = new JLabel("Hello, Press Buttons!");
        la.setForeground(Color.WHITE);
        la.setFont(la.getFont().deriveFont(16.0f));
        la.setLocation(70, 155);
        la.setSize(200, 20);
        add(la);

        JButton bt_start = new JButton("시작");
        bt_start.setBounds(100, 210, 100, 30);


        bt_start.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                initBoard();
                bt_start.setVisible(false);
                la.setVisible(false);
                main_title.setVisible(false);
            }
        });

        add(bt_start);

    }

    private void loadImages() {

        ImageIcon iid = new ImageIcon("src/resources/dot.png");
        ball = iid.getImage();

        ImageIcon iia = new ImageIcon("src/resources/apple.png");
        apple = iia.getImage();

        ImageIcon iih = new ImageIcon("src/resources/head.png");
        head = iih.getImage();
    }

    private void initGame() {
        dots = 3;

        for (int z = 0; z < dots; z++) {
            x[z] = 50 - z * 10;
            y[z] = 50;
        }
        
        locateApple();

        timer = new Timer(DELAY, this);
        timer.start();

    }



    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
        screenDraw(g);
    }
    
    private void doDrawing(Graphics g) {
        
        if (inGame) {

            g.drawImage(apple, apple_x, apple_y, this);

            for (int z = 0; z < dots; z++) {
                if (z == 0) {
                    g.drawImage(head, x[z], y[z], this);
                } else {
                    g.drawImage(ball, x[z], y[z], this);
                }
            }

            Toolkit.getDefaultToolkit().sync();

        } else {
            gameOver(g);
        }        
    }

    private void gameOver(Graphics g) {

        String msg = "Game Over";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(msg, (B_WIDTH - metr.stringWidth(msg)) / 2, B_HEIGHT / 2);

        JButton bt_restart = new JButton("재시작");
        bt_restart.setBounds(100, 210, 100, 30);

        bt_restart.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                String args[] = {"a","b"};
                new Snake();
                Snake.main(args);
                if (clip1 != null) {
                    clip1.stop();
                    clip1.loop(-1);
                    clip1.close();
                    clip.loop(-1);
                    clip.stop();
                    clip.close();
                }

            }
        });

        add(bt_restart);
    }
    


    public void dispose() {
        JFrame parent = (JFrame) this.getTopLevelAncestor();
        parent.dispose();
    }

    private void checkApple() {


        if ((x[0] == apple_x) && (y[0] == apple_y)) {

            score+=10;
            playSound("src/com/audio/getApple.wav", false);
            dots++;
            locateApple();
        }
    }

    public void screenDraw(Graphics g) {
        g.setColor(Color.white);
        g.drawString("SCORE : " + score,17 ,30 );
        this.repaint();

    }


    private void move() {
        for (int z = dots; z > 0; z--) {
            x[z] = x[(z - 1)];
            y[z] = y[(z - 1)];
        }

        if (leftDirection) {
            x[0] -= DOT_SIZE;
        }

        if (rightDirection) {
            x[0] += DOT_SIZE;
        }

        if (upDirection) {
            y[0] -= DOT_SIZE;
        }

        if (downDirection) {
            y[0] += DOT_SIZE;
        }
    }

    private void checkCollision() {

        for (int z = dots; z > 0; z--) {

            if ((z > 4) && (x[0] == x[z]) && (y[0] == y[z])) {
                inGame = false;
            }
        }

        if (y[0] >= B_HEIGHT) {
            inGame = false;
        }

        if (y[0] < 0) {
            inGame = false;
        }

        if (x[0] >= B_WIDTH) {
            inGame = false;
        }

        if (x[0] < 0) {
            inGame = false;
        }
        
        if (!inGame) {
            timer.stop();
        }
    }

    private void locateApple() {

        int r = (int) (Math.random() * RAND_POS);
        apple_x = ((r * DOT_SIZE));

        r = (int) (Math.random() * RAND_POS);
        apple_y = ((r * DOT_SIZE));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (inGame) {

            checkApple();
            checkCollision();
            move();
        }

        repaint();
    }

    public void playSound(String pathName, boolean isLoop) {
        try {
            clip = AudioSystem.getClip();
            File audioFile = new File(pathName);
            audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip.open(audioStream);
            clip.start();

            if(pathName.equals("src/com/audio/backgroundMusic.wav")){
                clip1 = clip;
            }

            // backgroundmusic 파일명 인지 아닌지를 검사해서 맞다면 전역변수 clip1에 넣고 gameOver에서 clip1의 음악을 정지+루프멈춤 어때
            if (isLoop)
                clip.loop(Clip.LOOP_CONTINUOUSLY);

    }
        catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if ((key == KeyEvent.VK_LEFT) && (!rightDirection)) {
                leftDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if ((key == KeyEvent.VK_RIGHT) && (!leftDirection)) {
                rightDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if ((key == KeyEvent.VK_UP) && (!downDirection)) {
                upDirection = true;
                rightDirection = false;
                leftDirection = false;
            }

            if ((key == KeyEvent.VK_DOWN) && (!upDirection)) {
                downDirection = true;
                rightDirection = false;
                leftDirection = false;
            }
        }
    }
}

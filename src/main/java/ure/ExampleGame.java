package ure;

import java.awt.*;
import java.io.IOException;
import javax.swing.*;


public class ExampleGame implements UTimeListener {

    static UREArea area;
    static URECamera camera;
    static URECommander commander;
    static UREActor player;
    static JFrame frame;
    static UREStatusPanel statusPanel;
    static Font font;

    private JFrame makeWindow() {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/Px437_Phoenix_BIOS-2y.ttf")).deriveFont(Font.PLAIN, 16);
        } catch (Exception e) {
            System.out.println("Failed to load font");
        }
        //font = new Font("Px437 Verite 9x14", Font.PLAIN, 16);

        frame = new JFrame("Rogue");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(null);
        frame.getContentPane().setLayout(null);
        frame.setBounds(0,0,1200,800);

        camera = new URECamera(new URERenderer(font), 900, 800 , frame);
        camera.moveTo(area, 40,20);
        player.attachCamera(camera);
        camera.setBounds(0,0,900,800);
        camera.renderImage();

        statusPanel = new UREStatusPanel(font, 15, 10, 16, 16, 5, 5, new UColor(1f,1f,1f), new UColor(0f,0f,0f));
        statusPanel.addText("name", "Player 1",0,0);
        statusPanel.addText("race", "Elf",0,1);
        statusPanel.addText("class", "Homo",0,2);
        statusPanel.addText("turn", "T 1", 0, 5);
        statusPanel.setBounds(900,0,300,800);

        frame.getContentPane().add(statusPanel);
        frame.getContentPane().add(camera);

        frame.setLocationRelativeTo(null);
        frame.setSize(1200, 800);
        frame.getContentPane().setFocusable(true);
        frame.setVisible(true);
        frame.getContentPane().requestFocusInWindow();
        return frame;
    }

    public void startUp()  {
        URETerrainCzar terrainCzar = new URETerrainCzar();
        area = new UREArea("/samplemap.txt", terrainCzar);
        URELight light = new URELight(new UColor(Color.WHITE), 25);
        light.moveTo(area, 45,25);
        player = new UREActor("Player", '@', new UColor(Color.WHITE), true);
        player.moveToCell(area, 11, 9);
        commander = new URECommander(player);
        commander.addAnimator(player);
        makeWindow().getContentPane().addKeyListener(commander);

        commander.registerTimeListener(area);
        commander.registerTimeListener(this);
        //while (true) {
        //  commander.animationLoop();
        //    camera.repaint();
        //    frame.repaint();
        //}
    }

    public void hearTick() {
        statusPanel.setText("turn", "T " + Integer.toString(commander.getTurn()));
    }
}
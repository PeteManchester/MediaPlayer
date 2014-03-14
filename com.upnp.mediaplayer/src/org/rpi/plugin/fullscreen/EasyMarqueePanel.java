package org.rpi.plugin.fullscreen;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EasyMarqueePanel extends JPanel {
    private JLabel textLabel;
    private int panelLocation;
    private ActionListener taskPerformer;
    private boolean isRunning = false;

    public static final int FRAMES_PER_SECOND = 24;
    public static final int MOVEMENT_PER_FRAME = 5;

    /**
     * Class constructor creates a marquee panel.
     */

    public EasyMarqueePanel(String initialText) {
        this.setLayout(null);
        this.setBackground(Color.BLACK);
        this.textLabel = new JLabel(initialText);
        this.textLabel.setBackground(Color.BLACK);
        this.textLabel.setForeground(Color.YELLOW);
//        this.textLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 30));

        this.panelLocation = 0;
        this.add(textLabel);
        this.taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                EasyMarqueePanel.this.tickAnimation();
            }
        };
    }

    public void setText(String text) {
        this.textLabel.setText(text);
    }

    /**
     * Starts the animation.
     */

    public void start() {
        this.isRunning = true;
        this.tickAnimation();
    }

    /**
     * Stops the animation.
     */

    public void stop() {
        this.isRunning = false;
    }

    /**
     * Moves the label one frame to the left.  If it's out of display range, move it back
     * to the right, out of display range.
     */

    private void tickAnimation() {
        this.panelLocation -= EasyMarqueePanel.MOVEMENT_PER_FRAME;
        if (this.panelLocation < this.textLabel.getWidth()) {
            this.panelLocation = this.getWidth();
        }

        this.textLabel.setLocation(this.panelLocation, 0);
        this.repaint();
        if (this.isRunning) {
            Timer t = new Timer(1000 / EasyMarqueePanel.FRAMES_PER_SECOND, this.taskPerformer);
            t.setRepeats(false);
            t.start();
        }
    }
}
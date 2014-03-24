package org.rpi.plugin.fullscreen;

import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.rpi.utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 *
 */
public class FullscreenDisplayView extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(FullscreenDisplayView.class);
    private static final URL defaultImageURL = FullscreenDisplayView.class.getResource("/org/rpi/image/mediaplayer240.png");

    private JDesktopPane d_pane;

    private MarqueePanel trackPanel;
    private MarqueePanel albumPanel;
    private MarqueePanel artistPanel;
    private MarqueePanel genrePanel;
    private MarqueePanel notYetUsedPanel;
    private MarqueePanel notYetUsedPanel2;

    private JLabel imageLabel;
    private JLabel playTimeLabel;
    private JLabel trackDurationLabel;

    private JLabel currentDateLabel;
    private JLabel currentTimeLabel;
    private JLabel infoLabel;

    private Boolean debug = Boolean.FALSE;
    private Border border;

    public FullscreenDisplayView(String friendlyName, Boolean debug) throws HeadlessException {

        // initial alignment
        int x = 10;
        int y = 10;

        int xHalf = 275;
        int fullWidth = 636;
        int halfWidth = 371;
        int lineHeight = 50;
        int separator = 0;

        this.debug = debug;

        border = BorderFactory.createEmptyBorder();
        if (this.debug) {
            border = BorderFactory.createLineBorder(Color.GREEN, 2);
        }

        Font timeFont = new Font(Font.SANS_SERIF, Font.PLAIN, 20);

        this.setLayout(new BorderLayout());
        this.setSize(656, 416);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setUndecorated(true);

        d_pane = new JDesktopPane();
        d_pane.setBackground(Color.BLACK); // prevent unexpected LaF settings

        // label with current date
        currentDateLabel = new JLabel(LocalDate.now().toString("dd.MM.yyyy"));
        currentDateLabel.setForeground(Color.YELLOW);
        currentDateLabel.setBackground(Color.BLACK);
        currentDateLabel.setFont(timeFont);
        currentDateLabel.setBounds(x, y, 130, 30);
        currentDateLabel.setBorder(border);
        d_pane.add(currentDateLabel);

        // label with current date
        currentTimeLabel = new JLabel(LocalTime.now().toString("HH:mm"));
        currentTimeLabel.setForeground(Color.YELLOW);
        currentTimeLabel.setBackground(Color.BLACK);
        currentTimeLabel.setFont(timeFont);
        currentTimeLabel.setBounds(140, y, 80, 30);
        currentTimeLabel.setBorder(border);
        d_pane.add(currentTimeLabel);

        // label with mediaplayer info
        infoLabel = new JLabel(friendlyName);
        infoLabel.setForeground(Color.YELLOW);
        infoLabel.setBackground(Color.BLACK);
        infoLabel.setFont(timeFont);
        infoLabel.setBounds(220, y, 426, 30);
        infoLabel.setBorder(border);
        d_pane.add(infoLabel);

        y = y + 30;

        // track text
        String trackText = "TrackTitle";
        trackPanel = this.addTextToPane(trackText, null, x, y, fullWidth, lineHeight);
        d_pane.add(trackPanel);

        y = y + lineHeight + separator;

        // Image Panel
        try {
            ImageIcon icon = this.calculateImageLabel(defaultImageURL);
            imageLabel = new JLabel(icon);
            imageLabel.setBorder(border);
            imageLabel.setBounds(new Rectangle(new Point(x, y), imageLabel.getPreferredSize()));

            d_pane.add(imageLabel);
        } catch (IOException e) {
            LOGGER.error("Cannot determine Track Image", e);
        }

        // artist text
        String artistText = "Artist";
        artistPanel = this.addTextToPane(artistText, null, xHalf, y, halfWidth, lineHeight);
        d_pane.add(artistPanel);

        Font smallFont = new Font(Font.SANS_SERIF, Font.PLAIN, 30);

        // album text
        y = y + lineHeight + separator;
        String albumText = "Album";
        albumPanel = this.addTextToPane(albumText, smallFont, xHalf, y, halfWidth, lineHeight);
        d_pane.add(albumPanel);

        // album text
        y = y + lineHeight + separator;
        String genreText = "Genre";
        genrePanel = this.addTextToPane(genreText, smallFont, xHalf, y, halfWidth, lineHeight);
        d_pane.add(genrePanel);

        // not yet used text
        y = y + lineHeight + separator;
        String nyuText = "";
        notYetUsedPanel = this.addTextToPane(nyuText, smallFont, xHalf, y, halfWidth, lineHeight);
        d_pane.add(notYetUsedPanel);

        // not yet used text 2
        y = y + lineHeight + separator;
        String nyuText2 = "";
        notYetUsedPanel2 = this.addTextToPane(nyuText2, smallFont, xHalf, y, halfWidth, lineHeight);
        d_pane.add(notYetUsedPanel2);

        String initialTime = "00:00:00";

        // set y to the bottom of the panel
        y = 350;

        // label with playing time
        playTimeLabel = new JLabel(initialTime);
        playTimeLabel.setForeground(Color.YELLOW);
        playTimeLabel.setBackground(Color.BLACK);
        playTimeLabel.setFont(timeFont);
        playTimeLabel.setBounds(x, y, 100, lineHeight);
        playTimeLabel.setBorder(border);
        d_pane.add(playTimeLabel);

        // label with track time
        trackDurationLabel = new JLabel(initialTime);
        trackDurationLabel.setForeground(Color.YELLOW);
        trackDurationLabel.setBackground(Color.BLACK);
        trackDurationLabel.setFont(timeFont);
        trackDurationLabel.setBounds(170, y, 100, lineHeight);
        trackDurationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        trackDurationLabel.setBorder(border);
        d_pane.add(trackDurationLabel);

        this.add(d_pane);
    }

    private MarqueePanel addTextToPane(String initialText, Font font, int x, int y, int width, int height) {
        MarqueePanel mp = new MarqueePanel(8, 15);
        mp.setBounds(x, y, width, height);
        mp.setBackground(Color.BLACK);
        mp.setForeground(Color.BLACK);
        mp.setWrap(true);
        mp.setBorder(border);

        if (font != null) {
            mp.getLabel().setFont(font);
        }

        if (!Utils.isEmpty(initialText)) {
            mp.getLabel().setText(initialText);
        }

        return mp;
    }

    public MarqueePanel getTrackPanel() {
        return trackPanel;
    }

    public MarqueePanel getAlbumPanel() {
        return albumPanel;
    }

    public MarqueePanel getArtistPanel() {
        return artistPanel;
    }

    public JLabel getPlayTimeLabel() {
        return playTimeLabel;
    }

    public JLabel getTrackDurationLabel() {
        return trackDurationLabel;
    }

    public JLabel getCurrentTimeLabel() {
        return currentTimeLabel;
    }

    public MarqueePanel getGenrePanel() {
        return genrePanel;
    }

    public ImageIcon calculateImageLabel(URL url) throws IOException {
        BufferedImage image = ImageIO.read(url);
        image = Scalr.resize(image, Scalr.Method.QUALITY, 260, Scalr.OP_ANTIALIAS);
        ImageIcon icon = new ImageIcon(image);

        return icon;
    }

    public void setImage(String urlString) {

        URL url = null;
        ImageIcon icon = null;

        try {
            if (Utils.isEmpty(urlString)) {
                url = defaultImageURL;
            } else {
                url = new URL(urlString);
            }

            icon = this.calculateImageLabel(url);
        } catch (MalformedURLException e) {
            LOGGER.debug("Wrong URL format", e);
        } catch (IOException e) {
            LOGGER.debug("Cannot calculate Image...", e);
        }


        this.imageLabel.setIcon(icon);
    }
}
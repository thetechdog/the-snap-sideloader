package org.sideloader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;

public class HyperlinkJLabel extends JLabel {

    private final String url;

    public HyperlinkJLabel(String text, String url) {
        super("<html><u>" + text + "</u></html>"); //initialize normal JLabel with text and underline
        this.url = url;
        setForeground(Color.BLUE.darker()); //set text color
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(HyperlinkJLabel.this.url)); //open URL on click
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("No default browser detected.");
                }
            }
        });
    }

    public HyperlinkJLabel(String outerText, String linkText, String url) {
        super("<html>"+outerText+"<u><font color=\"#0000B2\">" + linkText + "</font></u></html>"); //initialize normal JLabel with text and underline, set the link to darker blue
        this.url = url;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override //click event
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(HyperlinkJLabel.this.url)); //open URL on click
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("No default browser detected.");
                }
            }
        });
    }
}

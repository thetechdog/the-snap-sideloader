package org.sideloader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class PasswordPrompter implements KeyListener {

    private static JPasswordField passwordField;
    private static JDialog passwordDialog;
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {//pressing enter key
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            Main.setAuth(new String(passwordField.getPassword()));
            passwordDialog.dispose();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private void realPasswordPrompt(JFrame frame){//instance method
        JButton okButton = new JButton("OK");
        passwordDialog = new JDialog(frame, "Enter your password", true);
        passwordDialog.setModal(true);
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new GridLayout(4,1,1,1));
        passwordField = new JPasswordField();
        Font boldFont = new Font(okButton.getFont().getName(), Font.BOLD, 16);
        JLabel passwordLabel = new JLabel("Enter your password:");
        passwordLabel.setFont(boldFont);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(new JLabel("To add or remove software, you need administrative privileges."));
        passwordPanel.add(passwordField);
        passwordPanel.add(okButton);
        passwordPanel.setBorder(new EmptyBorder(10,7,12,7));
        passwordField.grabFocus();
        passwordDialog.add(passwordPanel);
        passwordDialog.pack();
        passwordDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        passwordDialog.setSize(400,200);
        passwordDialog.setMinimumSize(passwordDialog.getSize());
        passwordDialog.setLocationRelativeTo(frame);
        okButton.addActionListener(e -> {Main.setAuth(new String(passwordField.getPassword()));passwordDialog.dispose();});
        passwordField.addKeyListener(this); //requires instance of PasswordPrompter
        passwordDialog.setVisible(true);


    }
    public static void passwordPrompt(JFrame frame){//class method
        PasswordPrompter passwordPrompter = new PasswordPrompter();
        passwordPrompter.realPasswordPrompt(frame);
    }

}

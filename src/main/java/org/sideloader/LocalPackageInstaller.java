package org.sideloader;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

public class LocalPackageInstaller extends JPanel {
    private JLabel packageName;
    private JButton actionButton;
    private JPanel LPIGUI;
    private JLabel packageSummary;
    private JButton removePackageButton;
    private JLabel packageVersion;
    private JLabel packageLicense;
    private JButton storeButton;
    private JLabel packagePath;
    private JScrollPane descScrollPane;
    private JCheckBox classicInstallCheck;
    private JButton clearButton;
    private JFrame LPIFrame;
    private int status = 0;
    private PackagePropertyInterrogator packageInfo;

    public LocalPackageInstaller(SnapSideloaderStore storeWindow) {
        LPIFrame = storeWindow; // setting the frame to the same as the store one
        LPIFrame.setTitle("The Snap Sideloader - Local Package Mode");
        setMinimumSize(new Dimension(680, 480));
        Dimension d = new Dimension(700, 600);
        setSize(d);
        setMinimumSize(d);
        JTextArea packageDescription = new JTextArea(" Open a package to see its description here!");
        packageDescription.setEditable(false);
        packageDescription.setFont(new Font("Ubuntu Sans", Font.ITALIC, 16));
        descScrollPane.setViewportView(packageDescription);//setting scrollbar for description textbox
        add(LPIGUI);


        storeButton.addActionListener(back -> {
            storeWindow.backToStore();
        });

        removePackageButton.addActionListener(re -> {
            try {
                int answer = JOptionPane.showConfirmDialog(LPIFrame, "Do you want to back up user data?", "Package Removal",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.CANCEL_OPTION) return;
                if (Main.getAuth().isEmpty()) PasswordPrompter.passwordPrompt(LPIFrame);
                if (answer == JOptionPane.YES_OPTION) {//normal removal with snapshot
                    String removeCommand = " echo " + Main.getAuth() + " | sudo -S -k " + "snap remove " + packageInfo.getName();
                    int exitCode = Main.pendingActionDialog(LPIFrame, removeCommand, "The package is being removed. Please wait...", "Removing Package");
                    if (exitCode == 0) {
                        JOptionPane.showMessageDialog(LPIFrame, "Package removed successfully!\nYou can use the snapshot manager if you want to restore user data.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        status = 1;
                        actionButton.setText("Install Package");
                        classicInstallCheck.setEnabled(true);
                        classicInstallCheck.setSelected(false);
                        removePackageButton.setEnabled(false);
                        classicInstallCheck.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(LPIFrame, "Package removal error!", "Error", JOptionPane.ERROR_MESSAGE);
                        Main.setAuth("");
                    }
                    return;
                }
                if (answer == JOptionPane.NO_OPTION) {//purge
                    String removeCommand = " echo " + Main.getAuth() + " | sudo -S -k " + "snap remove --purge " + packageInfo.getName();
                    int exitCode = Main.pendingActionDialog(LPIFrame, removeCommand, "The package is being removed. Please wait...", "Removing Package");
                    if (exitCode == 0) {
                        JOptionPane.showMessageDialog(LPIFrame, "Package removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        status = 1;
                        actionButton.setText("Install Package");
                        classicInstallCheck.setEnabled(true);
                        classicInstallCheck.setSelected(false);
                        removePackageButton.setEnabled(false);
                    } else {
                        JOptionPane.showMessageDialog(LPIFrame, "Package removal error!", "Error", JOptionPane.ERROR_MESSAGE);
                        Main.setAuth("");
                    }
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(LPIFrame, "An exception occurred:\n" + e.getClass().getName());
                e.printStackTrace();
            }
        });

        actionButton.addActionListener(ae -> {
            //status: 0=nothing opened, 1=package opened and not installed, 2=package opened and installed
            if (status == 0) {
                String user = System.getProperty("os.user");
                String path = "";
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Snap package", "snap");
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setDialogTitle("Select a snap package file");
                fileChooser.setFileFilter(fileFilter);
                int answer = fileChooser.showOpenDialog(LPIFrame);
                if (answer == JFileChooser.APPROVE_OPTION) {
                    try {
                        path = fileChooser.getSelectedFile().getAbsolutePath();

                        status = 1;
                        String packageDetails = CommandOutputter.getOutput("snap info " + path);
                        packageInfo = new PackagePropertyInterrogator(packageDetails);
                        packageInfo.setPath(path);
                        packageName.setText(packageInfo.getName());
                        packageSummary.setText(packageInfo.getSummary());
                        packageVersion.setText("Version: " + packageInfo.getVersion());
                        packageLicense.setText("License: " + packageInfo.getLicense());
                        packageDescription.setText("  " + packageInfo.getDescription());
                        packagePath.setText("Path: " + "\"" + path + "\"");
                        actionButton.setText("Install Package");
                        classicInstallCheck.setEnabled(true);
                        clearButton.setEnabled(true);


                        //check if package is already installed (checks snap packages install path) and set to status 2 instead
                        String check = "test -f /snap/bin/" + packageInfo.getName() + " && echo y || echo n";
                        if (CommandOutputter.getOutput(check).contains("y")) {//package is indeed installed already
                            status = 2;
                            actionButton.setText("Upgrade Package");
                            removePackageButton.setEnabled(true);
                            classicInstallCheck.setVisible(false);
                            clearButton.setEnabled(true);
                        }
                    } catch (Exception e) {
                        status = 0;
                        actionButton.setText("Open Package");
                        JOptionPane.showMessageDialog(LPIFrame, "An exception occurred:\n" + e.getClass().getName() +
                                "\nIt's highly likely that the selected file is not a valid snap package.", "Aw, snap!", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }


                }
                return;
            }

            if (status == 1) {
                try {
                    if (Main.getAuth().isEmpty()) PasswordPrompter.passwordPrompt(LPIFrame);

                    String installCommand = " echo " + Main.getAuth() + " | sudo -S -k " + "snap install " + packageInfo.getPath() + " --dangerous";
                    if (classicInstallCheck.isSelected()) {//classic confinement
                        int answerC = JOptionPane.showConfirmDialog(LPIFrame, "Installing a classic confinement package means giving it full access to the system.\nProceed only if you trust the package source.",
                                "Classic Confinement", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (answerC == JOptionPane.CANCEL_OPTION) return;
                        installCommand = " echo " + Main.getAuth() + " | sudo -S -k " + "snap install " + packageInfo.getPath() + " --dangerous --classic";
                    }
                    int exitCode = Main.pendingActionDialog(LPIFrame, installCommand, "The package is being installed. Please wait...", "Installing Package");
                    if (exitCode == 0) {
                        JOptionPane.showMessageDialog(LPIFrame, "Package Installed!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        status = 2;
                        actionButton.setText("Upgrade Package");
                        classicInstallCheck.setVisible(false);
                        removePackageButton.setEnabled(true);
                        clearButton.setEnabled(true);
                    } else {
                        JOptionPane.showMessageDialog(LPIFrame, "Package install error!\nCheck your password and try again.\nIf your package requires classic confinement, check the box and try again.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        Main.setAuth("");//reset auth in case of wrong password
                    }


                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LPIFrame, "An exception occurred:\n" + e.getClass().getName());
                    e.printStackTrace();
                }
                return;


            }

            if (status == 2) {
                try {
                    if (Main.getAuth().isEmpty()) PasswordPrompter.passwordPrompt(LPIFrame);

                    String upgCommand = " echo " + Main.getAuth() + " | sudo -S -k " + "snap install " + packageInfo.getPath() + " --dangerous";
                    if (classicInstallCheck.isSelected()) {//confirmation to proceed for classic confinement
                        int answerC = JOptionPane.showConfirmDialog(LPIFrame, "Installing a classic confinement package means giving it full access to the system.\nProceed only if you trust the package source.",
                                "Classic Confinement", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (answerC == JOptionPane.CANCEL_OPTION) return;
                        upgCommand = " echo " + Main.getAuth() + " | sudo -S -k " + "snap install " + packageInfo.getPath() + " --dangerous --classic";
                    }
                    int exitCode = Main.pendingActionDialog(LPIFrame, upgCommand, "The package is being upgraded. Please wait...", "Upgrading Package");
                    if (exitCode == 0) {
                        JOptionPane.showMessageDialog(LPIFrame, "Package upgraded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(LPIFrame, "Package upgrade error!", "Error", JOptionPane.ERROR_MESSAGE);
                        Main.setAuth("");
                    }


                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LPIFrame, "An exception occurred:\n" + e.getClass().getName());
                    e.printStackTrace();
                }


            }

        });

        clearButton.addActionListener(ce -> {//clear all values
            status = 0;
            packageName.setText("No package file open. Please open a package file.");
            packageSummary.setText("Summary of snap package");
            packageVersion.setText("Version: " + "(open a package first)");
            packageLicense.setText("License: " + "(open a package first)");
            packageDescription.setText(" Open a package to see its description here!");
            packagePath.setText("Path: ???");
            actionButton.setText("Open Package");
            removePackageButton.setEnabled(false);
            classicInstallCheck.setEnabled(false);
            classicInstallCheck.setVisible(true);
            classicInstallCheck.setSelected(false);
            clearButton.setEnabled(false);


        });

    }

    public JPanel getLocalPanel() {
        return LPIGUI;
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        LPIGUI = new JPanel();
        LPIGUI.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:118px:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        LPIGUI.setMinimumSize(new Dimension(700, 600));
        LPIGUI.setPreferredSize(new Dimension(700, 600));
        LPIGUI.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        removePackageButton = new JButton();
        removePackageButton.setEnabled(false);
        removePackageButton.setText("Remove Package");
        CellConstraints cc = new CellConstraints();
        LPIGUI.add(removePackageButton, cc.xyw(5, 3, 2));
        actionButton = new JButton();
        actionButton.setText("Open Package");
        LPIGUI.add(actionButton, cc.xyw(5, 1, 2));
        packageName = new JLabel();
        Font packageNameFont = this.$$$getFont$$$("Ubuntu", Font.BOLD, 18, packageName.getFont());
        if (packageNameFont != null) packageName.setFont(packageNameFont);
        packageName.setText("No package file open. Please open a package file.");
        LPIGUI.add(packageName, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        packageSummary = new JLabel();
        Font packageSummaryFont = this.$$$getFont$$$("Ubuntu", -1, 16, packageSummary.getFont());
        if (packageSummaryFont != null) packageSummary.setFont(packageSummaryFont);
        packageSummary.setText("Summary of snap package");
        LPIGUI.add(packageSummary, cc.xy(1, 3));
        packageVersion = new JLabel();
        packageVersion.setFocusTraversalPolicyProvider(false);
        Font packageVersionFont = this.$$$getFont$$$("Ubuntu", -1, 14, packageVersion.getFont());
        if (packageVersionFont != null) packageVersion.setFont(packageVersionFont);
        packageVersion.setText("Version: (open a package first)");
        LPIGUI.add(packageVersion, cc.xy(1, 5));
        packageLicense = new JLabel();
        Font packageLicenseFont = this.$$$getFont$$$("Ubuntu", -1, 14, packageLicense.getFont());
        if (packageLicenseFont != null) packageLicense.setFont(packageLicenseFont);
        packageLicense.setText("License: (open a package first)");
        LPIGUI.add(packageLicense, cc.xy(1, 7));
        packagePath = new JLabel();
        Font packagePathFont = this.$$$getFont$$$("Ubuntu", -1, 12, packagePath.getFont());
        if (packagePathFont != null) packagePath.setFont(packagePathFont);
        packagePath.setText("Path: ???");
        LPIGUI.add(packagePath, cc.xyw(1, 11, 6));
        descScrollPane = new JScrollPane();
        LPIGUI.add(descScrollPane, cc.xyw(1, 9, 7, CellConstraints.FILL, CellConstraints.FILL));
        clearButton = new JButton();
        clearButton.setEnabled(false);
        clearButton.setText("Clear");
        LPIGUI.add(clearButton, new CellConstraints(5, 5, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 5)));
        storeButton = new JButton();
        storeButton.setText("Back to Store");
        LPIGUI.add(storeButton, cc.xy(6, 5));
        classicInstallCheck = new JCheckBox();
        classicInstallCheck.setEnabled(false);
        classicInstallCheck.setText("Install in Classic Mode");
        LPIGUI.add(classicInstallCheck, cc.xyw(5, 7, 2));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return LPIGUI;
    }
}

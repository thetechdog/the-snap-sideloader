package org.sideloader;

import com.jgoodies.forms.layout.*;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class InstalledAndUpdatesPanel extends JPanel {
    private JPanel panel1;
    private JButton updateAllButton;
    private JLabel updatesNoLabel;
    private JPanel programsPanel;
    private JButton appButton;
    private JLabel appLabel;
    private ArrayList<String> appsToUpdate = new ArrayList<>();
    private JScrollPane storeContentPane;

    //TODO: add listeners
    public InstalledAndUpdatesPanel(JScrollPane storeContentPane) {
        add(panel1);
        panel1.setPreferredSize(new Dimension(900, 750));
        this.storeContentPane = storeContentPane;
    }

    public void setUpPanel(HashMap<String, String> installedAppsCurrentRepo, DBOperator dbop) {
        int noOfUpdates = 0;
        if (installedAppsCurrentRepo.isEmpty()) {
            System.out.println("No apps installed yet.");
            updatesNoLabel.setText("Install a program to get started!");
        }//no apps from this repo
        else {//apps are installed from this repo
            Iterator<HashMap.Entry<String, String>> iterator = installedAppsCurrentRepo.entrySet().iterator();
            String app = iterator.next().getKey();
            appButton.setText(app);
            appButton.setEnabled(true);
            int appVer = Integer.parseInt(installedAppsCurrentRepo.get(app));
            int latestAppVer = Integer.parseInt(dbop.simpleQueryExecutor("SELECT VersionNumber " +
                    "FROM PACKAGES P INNER JOIN VERSIONS V ON P.PackageID=V.Package " +
                    "WHERE PackageName=\"" + app + "\" " +
                    "ORDER BY VersionNumber DESC " +
                    "LIMIT 1"));
            if (latestAppVer == appVer) appLabel.setText("Latest Version");
            else {
                appLabel.setText("Update Available: " + latestAppVer);
                noOfUpdates++;
            }
            //add action listener
            final String finalApp = app;//required because of lambda expression
            appButton.addActionListener(e -> {
                storeContentPane.setViewportView(new LoadingPanel(storeContentPane)); //show loading panel while loading


                //create dummy button to ensure compatibility with existing logic and load app
                ArrayList[] appDetails = dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon FROM PACKAGES P WHERE PackageName=\"" + finalApp + "\"");
                JButton dummyButton = new JButton();
                SnapSideloaderStore.styleButton(dummyButton, (String) appDetails[0].getFirst(), (String) appDetails[1].getFirst(), (String) appDetails[2].getFirst(), (String) appDetails[3].getFirst());
                SnapSideloaderStore.loadSelectedProgram(dummyButton, storeContentPane, this);
            });
            //add buttons and label for other apps if available
            int noOfAppsCR = installedAppsCurrentRepo.size();
            FormLayout layout = (FormLayout) programsPanel.getLayout();
            RowSpec rowSpec = layout.getRowSpec(3);//first manually added row in UI designer
            ColumnSpec columnSpec = layout.getColumnSpec(3);
            for (int i = 1; i < noOfAppsCR; i++) {
                app = iterator.next().getKey();
                appVer = Integer.parseInt(installedAppsCurrentRepo.get(app));
                latestAppVer = Integer.parseInt(dbop.simpleQueryExecutor("SELECT VersionNumber " +
                        "FROM PACKAGES P INNER JOIN VERSIONS V ON P.PackageID=V.Package " +
                        "WHERE PackageName=\"" + app + "\" " +
                        "ORDER BY VersionNumber DESC " +
                        "LIMIT 1"));
                JButton appButton = new JButton(app);
                JLabel appLabel = new JLabel("Latest Version");
                if (appVer < latestAppVer) {
                    appLabel.setText("Update Available: rev" + latestAppVer);
                    noOfUpdates++;
                    appsToUpdate.add(app);
                }
                //add action listener
                final String finalApp_ = app;//required because of lambda expression
                appButton.addActionListener(e -> {
                    storeContentPane.setViewportView(new LoadingPanel(storeContentPane)); //show loading panel while loading


                    //create dummy button to ensure compatibility with existing logic and load app
                    ArrayList[] appDetails = dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon FROM PACKAGES P WHERE PackageName=\"" + finalApp_ + "\"");
                    JButton dummyButton = new JButton();
                    SnapSideloaderStore.styleButton(dummyButton, (String) appDetails[0].getFirst(), (String) appDetails[1].getFirst(), (String) appDetails[2].getFirst(), (String) appDetails[3].getFirst());
                    SnapSideloaderStore.loadSelectedProgram(dummyButton, storeContentPane, this);
                });
                //adding row
                layout.appendRow(rowSpec);
                CellConstraints cc = new CellConstraints();//col, row
                programsPanel.add(appButton, cc.xy(1, i + 1 + 2));//+1 because index starts from zero, while column numbering starts from 1, and 1 is already used, +2 to account for the actual column starting from 3
                programsPanel.add(appLabel, cc.xy(3, i + 1 + 2));//3 is the second actually populated column in the UI designer
            }
            if (noOfUpdates >= 1) updateAllButton.setEnabled(true);
            updatesNoLabel.setText(noOfUpdates + " update(s) available");
            programsPanel.revalidate();
            programsPanel.repaint();
            System.out.println(appsToUpdate);

        }
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
        panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(p;500px):noGrow"));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, Font.BOLD, 20, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Installed Programs:");
        CellConstraints cc = new CellConstraints();
        panel1.add(label1, cc.xy(1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
        updatesNoLabel = new JLabel();
        Font updatesNoLabelFont = this.$$$getFont$$$(null, Font.ITALIC, 16, updatesNoLabel.getFont());
        if (updatesNoLabelFont != null) updatesNoLabel.setFont(updatesNoLabelFont);
        updatesNoLabel.setText("_ Updates Available:");
        panel1.add(updatesNoLabel, cc.xy(1, 3));
        updateAllButton = new JButton();
        updateAllButton.setEnabled(false);
        updateAllButton.setText("Update All");
        updateAllButton.setVisible(false);
        panel1.add(updateAllButton, cc.xy(3, 3));
        programsPanel = new JPanel();
        programsPanel.setLayout(new FormLayout("fill:max(d;300px):grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:grow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        programsPanel.setEnabled(true);
        programsPanel.setVisible(true);
        panel1.add(programsPanel, cc.xyw(1, 5, 3, CellConstraints.DEFAULT, CellConstraints.TOP));
        appButton = new JButton();
        appButton.setEnabled(false);
        appButton.setText("<...>");
        appButton.setVisible(true);
        programsPanel.add(appButton, cc.xy(1, 3));
        appLabel = new JLabel();
        appLabel.setText("<...>");
        appLabel.setVisible(true);
        programsPanel.add(appLabel, cc.xy(3, 3));
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
        return panel1;
    }
}

package org.sideloader;

import com.jgoodies.forms.layout.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class InstalledAndUpdatesPanel extends JPanel{
    private JPanel panel1;
    private JButton updateAllButton;
    private JLabel updatesNoLabel;
    private JPanel programsPanel;
    private JButton appButton;
    private JLabel appLabel;
    private ArrayList<String> appsToUpdate=new ArrayList<>();
    private JScrollPane storeContentPane;
//TODO: add listeners
    public InstalledAndUpdatesPanel(JScrollPane storeContentPane) {
        add(panel1);
        panel1.setPreferredSize(new Dimension(900,750));
        this.storeContentPane=storeContentPane;
    }
    public void setUpPanel(HashMap<String, String> installedAppsCurrentRepo, DBOperator dbop){
        int noOfUpdates=0;
        if (installedAppsCurrentRepo.isEmpty()) {System.out.println("No apps installed yet."); updatesNoLabel.setText("Install a program to get started!");}//no apps from this repo
        else{//apps are installed from this repo
            Iterator<HashMap.Entry<String, String>> iterator = installedAppsCurrentRepo.entrySet().iterator();
            String app=iterator.next().getKey();
            appButton.setText(app);
            appButton.setEnabled(true);
            int appVer=Integer.parseInt(installedAppsCurrentRepo.get(app));
            int latestAppVer=Integer.parseInt(dbop.simpleQueryExecutor("SELECT VersionNumber " +
                    "FROM PACKAGES P INNER JOIN VERSIONS V ON P.PackageID=V.Package " +
                    "WHERE PackageName=\"" + app + "\" "+
                    "ORDER BY VersionNumber DESC " +
                    "LIMIT 1"));
            if(latestAppVer==appVer)appLabel.setText("Latest Version");
            else {appLabel.setText("Update Available: " + latestAppVer); noOfUpdates++;}
            //add action listener
            final String finalApp = app;//required because of lambda expression
            appButton.addActionListener(e -> {
                storeContentPane.setViewportView(new LoadingPanel(storeContentPane)); //show loading panel while loading


                //create dummy button to ensure compatibility with existing logic and load app
                ArrayList[] appDetails=dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon FROM PACKAGES P WHERE PackageName=\"" + finalApp + "\"");
                JButton dummyButton=new JButton();
                SnapSideloaderStore.styleButton(dummyButton,(String)appDetails[0].getFirst(),(String)appDetails[1].getFirst(),(String)appDetails[2].getFirst(),(String)appDetails[3].getFirst());
                SnapSideloaderStore.loadSelectedProgram(dummyButton, storeContentPane, this);
            });
            //add buttons and label for other apps if available
            int noOfAppsCR=installedAppsCurrentRepo.size();
            FormLayout layout = (FormLayout) programsPanel.getLayout();
            RowSpec rowSpec = layout.getRowSpec(3);//first manually added row in UI designer
            ColumnSpec columnSpec = layout.getColumnSpec(3);
            for(int i=1;i<noOfAppsCR;i++){
                app=iterator.next().getKey();
                appVer=Integer.parseInt(installedAppsCurrentRepo.get(app));
                latestAppVer=Integer.parseInt(dbop.simpleQueryExecutor("SELECT VersionNumber " +
                        "FROM PACKAGES P INNER JOIN VERSIONS V ON P.PackageID=V.Package " +
                        "WHERE PackageName=\"" + app + "\" "+
                        "ORDER BY VersionNumber DESC " +
                        "LIMIT 1"));
                JButton appButton=new JButton(app);
                JLabel appLabel=new JLabel("Latest Version");
                if(appVer<latestAppVer) {appLabel.setText("Update Available: rev" + latestAppVer); noOfUpdates++; appsToUpdate.add(app);}
                //add action listener
                final String finalApp_ = app;//required because of lambda expression
                appButton.addActionListener(e -> {
                    storeContentPane.setViewportView(new LoadingPanel(storeContentPane)); //show loading panel while loading


                    //create dummy button to ensure compatibility with existing logic and load app
                    ArrayList[] appDetails=dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon FROM PACKAGES P WHERE PackageName=\"" + finalApp_ + "\"");
                    JButton dummyButton=new JButton();
                    SnapSideloaderStore.styleButton(dummyButton,(String)appDetails[0].getFirst(),(String)appDetails[1].getFirst(),(String)appDetails[2].getFirst(),(String)appDetails[3].getFirst());
                    SnapSideloaderStore.loadSelectedProgram(dummyButton, storeContentPane, this);
                });
                //adding row
                layout.appendRow(rowSpec);
                CellConstraints cc = new CellConstraints();//col, row
                programsPanel.add(appButton, cc.xy(1,i+1+2));//+1 because index starts from zero, while column numbering starts from 1, and 1 is already used, +2 to account for the actual column starting from 3
                programsPanel.add(appLabel, cc.xy(3,i+1+2));//3 is the second actually populated column in the UI designer
            }
            if(noOfUpdates>=1)updateAllButton.setEnabled(true);
            updatesNoLabel.setText(noOfUpdates + " update(s) available");
            programsPanel.revalidate();
            programsPanel.repaint();
            System.out.println(appsToUpdate);

        }
    }
}

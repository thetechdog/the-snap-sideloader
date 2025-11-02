package org.sideloader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

class FeaturedAppsLoader extends SwingWorker<JPanel, Void> {
    private FeaturedApps featuredAppsPanel;
    private DBOperator dbop;
    private JScrollPane storeContentPane;
    public FeaturedAppsLoader(FeaturedApps featuredAppsPanel, DBOperator dbop, JScrollPane storeContentPane){
        this.featuredAppsPanel=featuredAppsPanel;
        this.dbop=dbop;
        this.storeContentPane=storeContentPane;
    }
    @Override
    protected JPanel doInBackground() throws Exception {
        System.out.println("Loading featured apps...");
        String welcomeMessage = dbop.simpleQueryExecutor("SELECT Text FROM FLAVOUR_TEXT LIMIT 1");
        ArrayList[] featuredApps = dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon FROM FEATURED F INNER JOIN PACKAGES P ON F.APP_ID=P.PackageID LIMIT 6");
        featuredAppsPanel.setWelcomeText(welcomeMessage);
        System.out.println("Retrieving app resources...");
        featuredAppsPanel.populateFeaturedApps(featuredApps[0], featuredApps[1], featuredApps[2], featuredApps[3]);//program name, package name, program summary, program icon
        return featuredAppsPanel;
    }

    @Override
    protected void done() {
        try {
            //get the created panel and display it in the storeContentPane
            featuredAppsPanel = (FeaturedApps) get();
            storeContentPane.setViewportView(featuredAppsPanel);//set viewport


            storeContentPane.getVerticalScrollBar().setValue(0);//reset scrollbar to top;


        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Couldn't load Featured Apps...\nCheck if the repository file isn't corrupt...\nAn exception occurred:\n"+e.getClass().getName());
            JOptionPane.showMessageDialog(null,"Couldn't load Featured Apps...\nCheck if the repository file isn't corrupt...\nAn exception occurred:\n"+e.getClass().getName(), "Error",JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            storeContentPane.setViewportView(null);//set viewport
        }
        finally {
            //enable storePanel components after loading
            Container StorePanelContainer=storeContentPane.getParent();
            JPanel StorePanel=(JPanel)StorePanelContainer;
            Component[] components=StorePanel.getComponents();
            for(Component component:components){
                component.setEnabled(true);
            }
            System.out.println("Featured Apps loading routine is done..");
        }
    }

}

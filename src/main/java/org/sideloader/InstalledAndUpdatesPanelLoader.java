package org.sideloader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InstalledAndUpdatesPanelLoader extends SwingWorker<JPanel, Void>{
    HashMap<String, String> installedAppsCurrentRepo=new HashMap<>();
    HashMap<String, ArrayList<String>> installedAppsList;
    String currentRepo;
    DBOperator dbop;
    JScrollPane storeContentPane;
    public InstalledAndUpdatesPanelLoader(String currentRepo, HashMap<String, ArrayList<String>> installedAppsList, DBOperator dbop, JScrollPane storeContentPane){
        this.installedAppsList=installedAppsList;
        this.currentRepo=currentRepo;
        this.dbop=dbop;
        this.storeContentPane=storeContentPane;
        storeContentPane.getVerticalScrollBar().setValue(0);//reset scrollbar to top;
    }

    @Override
    protected JPanel doInBackground() throws Exception {
        for(Map.Entry<String, ArrayList<String>> entry: installedAppsList.entrySet()){
            if(entry.getValue().get(2).equals(currentRepo)) installedAppsCurrentRepo.put(entry.getKey(),entry.getValue().get(1)); //package name:versionNumber
        }
        InstalledAndUpdatesPanel installedAndUpdatesPanel=new InstalledAndUpdatesPanel(storeContentPane);
        installedAndUpdatesPanel.setUpPanel(installedAppsCurrentRepo,dbop); //populate installed and updates panel
        return installedAndUpdatesPanel;
    }
    protected void done(){
        try{
            storeContentPane.setViewportView(get());


        }
        catch (Exception e){
            System.out.println("Couldn't load installed and updates panel...");
            JOptionPane.showMessageDialog(null,"Couldn't load installed programs panel...", "Error",JOptionPane.ERROR_MESSAGE);
            storeContentPane.setViewportView(null);//set viewport
            e.printStackTrace();}
        finally {
            //enable storePanel components after loading
            Container StorePanelContainer=storeContentPane.getParent();
            JPanel StorePanel=(JPanel)StorePanelContainer;
            Component[] components=StorePanel.getComponents();
            for(Component component:components){
                component.setEnabled(true);
            }
            System.out.println("Installed and updates panel loading routine is done..");
        }
    }
}

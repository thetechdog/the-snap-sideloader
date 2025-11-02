package org.sideloader;

import javax.swing.*;
import java.awt.*;

public class LoadingPanel extends JPanel {
    private JPanel panel1;
    private JProgressBar progressBar1;


    public LoadingPanel(JScrollPane storeContentPane) {
        add(panel1);
        //disable storePanel components while loading, the loader/worker classes will enable them later
        Container StorePanelContainer=storeContentPane.getParent();
        JPanel StorePanel=(JPanel)StorePanelContainer;
        Component[] components=StorePanel.getComponents();
        for(Component component:components){
            component.setEnabled(false);
        }
    }
}

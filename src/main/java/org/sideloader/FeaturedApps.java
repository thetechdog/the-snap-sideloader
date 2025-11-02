package org.sideloader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class FeaturedApps extends JPanel{
    private JPanel panel1;
    private JButton program1;
    private JButton program2;
    private JButton program3;
    private JButton program4;
    private JButton program5;
    private JButton program6;
    private JLabel attractText;
    public FeaturedApps(JScrollPane storeContentPane) {
        panel1.setPreferredSize(new Dimension(900,900));
        add(panel1);

        for(JButton button : new JButton[]{program1,program2,program3,program4,program5,program6}){
            button.addActionListener((e)->{
                storeContentPane.setViewportView(new LoadingPanel(storeContentPane)); //show loading panel while loading
                SnapSideloaderStore.loadSelectedProgram(button, storeContentPane, this);
            });
        }

    }

    public void setWelcomeText(String text) {
        attractText.setText(text);
    }

    public void populateFeaturedApps(ArrayList<String> programs, ArrayList<String> packageNames, ArrayList<String> summaries, ArrayList<String> iconURLs){ //populate each button depending on how many featured apps/programs there are
        JButton[] buttons = {program1,program2,program3,program4,program5,program6};
        for(int i=0;i<programs.size();i++){
            SnapSideloaderStore.styleButton(buttons[i],programs.get(i), packageNames.get(i), summaries.get(i),iconURLs.get(i));
        }
    }
}

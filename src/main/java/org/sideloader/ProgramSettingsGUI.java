package org.sideloader;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ProgramSettingsGUI extends JDialog {
    private JButton saveButton;
    private JPanel settingsPanel;
    private JButton cancelButton;
    private JComboBox themeBox;
    private JComboBox frequencyBox;
    private JButton manageRepositoriesButton;
    private JCheckBox featuredCheckBox;
    private JCheckBox alertsCheckBox;
    private JButton refreshButton;
    private JLabel lastUpdateLabel;
    private JCheckBox suggCheckBox;
    private JCheckBox imgCheckBox;
    private ProgramSettings stConfig;
    private String lastUpdateShowAndSet;
    private Map<String, String> repositories;
    private String dbDirectory=System.getProperty("user.home")+"/.local/share/snap-sideloader/";

    public ProgramSettingsGUI(ProgramSettings stConfig, Map<String, String> repositories, String currentRepo){
        this.stConfig=stConfig;
        this.repositories=repositories;
        setTitle("Store Settings");
        setContentPane(settingsPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loadSettings();
        lastUpdateLabel.setText(lastUpdateLabel.getText()+' '+lastUpdateShowAndSet);
        pack();
        saveButton.addActionListener(e->{
            saveSettings();
            JOptionPane.showMessageDialog(this,"Restart the program for changes to take effect.","Settings Saved",JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });
        cancelButton.addActionListener(e->dispose());
        refreshButton.addActionListener(e->refreshRepos());
        manageRepositoriesButton.addActionListener(e->{RepoManager manager=new RepoManager(repositories,false, currentRepo); manager.setModal(true);
                                manager.setLocationRelativeTo(this); manager.setVisible(true);});

    }

    private void refreshRepos(){
        for(Map.Entry<String,String> repo: repositories.entrySet()){ //iterating over each key-value pair
            String filename=repo.getKey()+".sqlite";
            String url=repo.getValue();
            try{SnapSideloaderStore.downloadFile(url,dbDirectory,filename);} catch (IOException e) {
                JOptionPane.showMessageDialog(this,"Couldn't download file for repository named "+repo.getKey()
                        +".\nCheck if the URL is correct, or if your internet connection is stable.","Error",JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            System.out.println("Saved "+filename);
        }//saving the db files for each repo locally

        lastUpdateShowAndSet=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        lastUpdateLabel.setText("Last refreshed at: " +lastUpdateShowAndSet);

        stConfig.lastUpdate=lastUpdateShowAndSet;
        Main.createConfigFile(stConfig);//also save refresh date automatically
        JOptionPane.showMessageDialog(this,"Repositories refreshed successfully!","Success",JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadSettings(){
        themeBox.setSelectedIndex(stConfig.theme);
        frequencyBox.setSelectedIndex(stConfig.frequency);
        alertsCheckBox.setSelected(stConfig.updateAlerts);
        featuredCheckBox.setSelected(stConfig.showFeaturedPage);
        suggCheckBox.setSelected(stConfig.programSuggestions);
        imgCheckBox.setSelected(stConfig.showImgs);
        lastUpdateShowAndSet=stConfig.lastUpdate;
    }

    private void saveSettings(){
        stConfig.theme=(byte)themeBox.getSelectedIndex();
        stConfig.frequency=(byte)frequencyBox.getSelectedIndex();
        stConfig.updateAlerts=alertsCheckBox.isSelected();
        stConfig.showFeaturedPage=featuredCheckBox.isSelected();
        stConfig.programSuggestions=suggCheckBox.isSelected();
        stConfig.showImgs=imgCheckBox.isSelected();
        stConfig.lastUpdate=lastUpdateShowAndSet;
        Main.createConfigFile(stConfig); //create config file when clicking save based on the settings
    }
}

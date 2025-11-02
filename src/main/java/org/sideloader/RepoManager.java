package org.sideloader;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class RepoManager extends JDialog {
    private JPanel repomanPanel;
    private JComboBox repoBox;
    private JButton addNewRepositoryButton;
    private JTextField reponameField;
    private JTextField repoaddrField;
    private JButton actionRepositoryButton;
    private JButton cancelButton;
    private JRadioButton addRemoveModeRadioButton;
    private JRadioButton editModeRadioButton;
    private Map<String, String> repositories;
    private String dbDirectory=System.getProperty("user.home")+"/.local/share/snap-sideloader/";


    public RepoManager(Map<String, String> repositories, boolean isGuided, String currentRepo){
        if(isGuided)JOptionPane.showMessageDialog(null,"Welcome to Snap Sideloader Store!\nThere are no available repositories. \n" +
                "You must add a repository from the Repository Manager.","No Repositories!",JOptionPane.WARNING_MESSAGE); //walk through setting up a repo
        this.repositories=repositories;
        setTitle("Repository Manager");
        setContentPane(repomanPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(true);
        updateRepoBox();//adds existing repos to box
        repoBox.setSelectedItem(null);
        pack();


        addNewRepositoryButton.addActionListener(e->{
            String repoURL=""; boolean syntaxFlag=false;
            while (repoURL.isBlank() || !syntaxFlag){
                repoURL=JOptionPane.showInputDialog(this,"Write the repository URL:","Add New Repository",JOptionPane.QUESTION_MESSAGE);
                if(repoURL.isBlank()){JOptionPane.showMessageDialog(this,"Repository URL cannot be empty","Error",JOptionPane.ERROR_MESSAGE); continue;}
                syntaxFlag=checkSyntax(repoURL);
                if(!syntaxFlag)JOptionPane.showMessageDialog(this,"Repository URL is not valid","Error",JOptionPane.ERROR_MESSAGE);
            }

            String repoName="";
            while (repoName.contains("/") || repoName.isEmpty() || repositories.containsKey(repoName)){
                repoName=JOptionPane.showInputDialog(this,"Give this repository a name (can't change later):","Add New Repository",JOptionPane.QUESTION_MESSAGE);
                //if(repoURL==null)return;
                if(repoName.contains("/"))JOptionPane.showMessageDialog(this,"Repository name cannot contain '/'","Error",JOptionPane.ERROR_MESSAGE);
                if(repoName.isEmpty())JOptionPane.showMessageDialog(this,"Repository name cannot be empty","Error",JOptionPane.ERROR_MESSAGE);
                if(repositories.containsKey(repoName))JOptionPane.showMessageDialog(this,"Repository with this name already exists","Error",JOptionPane.ERROR_MESSAGE);
            }
            //^validating inputs

            //tries to dowload repo file
            try{SnapSideloaderStore.downloadFile(repoURL,dbDirectory,repoName+".sqlite");
                repositories.put(repoName,repoURL);//add repo
                SnapSideloaderStore.createRepoList(repositories);//save to list file
                updateRepoBox();//update box
                JOptionPane.showMessageDialog(this,"Repository added successfully!","Repository Added",JOptionPane.INFORMATION_MESSAGE);}
            catch (IOException exception) {
                JOptionPane.showMessageDialog(this,"Couldn't download file for repository named "+repoName+" at URL:\n"+repoURL
                        +"\nCheck if the URL is correct, or if your internet connection is stable.\n"+exception.getMessage(),"Repository not added",JOptionPane.ERROR_MESSAGE);
            }
            //if can't download, do not add to list

            repoBox.setSelectedItem(null);


        });
        cancelButton.addActionListener(e->dispose());
        actionRepositoryButton.addActionListener(e->{
            if(addRemoveModeRadioButton.isSelected()){//REMOVE mode
                if(repoBox.getSelectedItem().toString().equals(currentRepo)){
                    JOptionPane.showMessageDialog(this,"You cannot remove the repository that is currently in use.","Illegal Action",JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this repository?", "Removal Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
                if(answer==JOptionPane.YES_OPTION){
                    repositories.remove(repoBox.getSelectedItem().toString());//remove repo from HashMap
                    SnapSideloaderStore.createRepoList(repositories);//save changes to repo file
                    reponameField.setText("");
                    repoaddrField.setText("");
                    File repoFile=new File(dbDirectory+repoBox.getSelectedItem().toString()+".sqlite");
                    repoFile.delete();
                    updateRepoBox();
                }

            }
            else{repositories.put(reponameField.getText(),repoaddrField.getText()); //EDIT mode
                try{SnapSideloaderStore.downloadFile(repoaddrField.getText(),dbDirectory,reponameField.getText()+".sqlite");
                    repositories.put(reponameField.getText(),repoaddrField.getText());//add repo if can download
                    SnapSideloaderStore.createRepoList(repositories);//save to list file
                    JOptionPane.showMessageDialog(this,"Repository modified successfully!","Repository Saved",JOptionPane.INFORMATION_MESSAGE);}
                catch (IOException exception) {
                    JOptionPane.showMessageDialog(this,"Couldn't download file for repository named "+reponameField.getText()+" at URL:\n"+repoaddrField.getText()
                            +"\nCheck if the URL is correct, or if your internet connection is stable.\n Make sure the original file isn't open elsewhere.","Repository not added",JOptionPane.ERROR_MESSAGE);
                }

            }//edit current entry
        });
        repoBox.addActionListener(e->{ //aici continua
            String selectedreponame=repoBox.getSelectedItem().toString(); //TODO: add try catch
            reponameField.setText(selectedreponame);
            repoaddrField.setText(repositories.get(selectedreponame));
            actionRepositoryButton.setEnabled(true);
        });
        editModeRadioButton.addActionListener(e->{
            //reponameField.setEditable(true);
            repoaddrField.setEditable(true);
            addRemoveModeRadioButton.setSelected(false);
            addNewRepositoryButton.setVisible(false);
            actionRepositoryButton.setText("Save Changes");
        });
        addRemoveModeRadioButton.addActionListener(e->{
            //reponameField.setEditable(false);
            repoaddrField.setEditable(false);
            editModeRadioButton.setSelected(false);
            addNewRepositoryButton.setVisible(true);
            actionRepositoryButton.setText("Remove Repository");
        });


    }
    private void updateRepoBox(){
        String[] repoNames=repositories.keySet().toArray(new String[0]);
        DefaultComboBoxModel model=new DefaultComboBoxModel(repoNames);
        repoBox.setModel(model);
    }

    private boolean checkSyntax(String repoURL){
        try{
            new URI(repoURL).toURL();//this way checks if it's a valid URI, then checks if it's a valid URL
            return true;
        }
        catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            return false;
        }
    }

}

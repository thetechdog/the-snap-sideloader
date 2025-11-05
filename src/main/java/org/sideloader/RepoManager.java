package org.sideloader;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
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
    private String dbDirectory = System.getProperty("user.home") + "/.local/share/snap-sideloader/";


    public RepoManager(Map<String, String> repositories, boolean isGuided, String currentRepo) {
        if (isGuided)
            JOptionPane.showMessageDialog(null, "Welcome to Snap Sideloader Store!\nThere are no available repositories. \n" +
                    "You must add a repository from the Repository Manager.", "No Repositories!", JOptionPane.WARNING_MESSAGE); //walk through setting up a repo
        this.repositories = repositories;
        setTitle("Repository Manager");
        setContentPane(repomanPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(true);
        updateRepoBox();//adds existing repos to box
        repoBox.setSelectedItem(null);
        pack();


        addNewRepositoryButton.addActionListener(e -> {
            String repoURL = "";
            boolean syntaxFlag = false;
            while (repoURL.isBlank() || !syntaxFlag) {
                repoURL = JOptionPane.showInputDialog(this, "Write the repository URL:", "Add New Repository", JOptionPane.QUESTION_MESSAGE);
                if (repoURL.isBlank()) {
                    JOptionPane.showMessageDialog(this, "Repository URL cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                syntaxFlag = checkSyntax(repoURL);
                if (!syntaxFlag)
                    JOptionPane.showMessageDialog(this, "Repository URL is not valid", "Error", JOptionPane.ERROR_MESSAGE);
            }

            String repoName = "";
            while (repoName.contains("/") || repoName.isEmpty() || repositories.containsKey(repoName)) {
                repoName = JOptionPane.showInputDialog(this, "Give this repository a name (can't change later):", "Add New Repository", JOptionPane.QUESTION_MESSAGE);
                //if(repoURL==null)return;
                if (repoName.contains("/"))
                    JOptionPane.showMessageDialog(this, "Repository name cannot contain '/'", "Error", JOptionPane.ERROR_MESSAGE);
                if (repoName.isEmpty())
                    JOptionPane.showMessageDialog(this, "Repository name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                if (repositories.containsKey(repoName))
                    JOptionPane.showMessageDialog(this, "Repository with this name already exists", "Error", JOptionPane.ERROR_MESSAGE);
            }
            //^validating inputs

            //tries to dowload repo file
            try {
                SnapSideloaderStore.downloadFile(repoURL, dbDirectory, repoName + ".sqlite");
                repositories.put(repoName, repoURL);//add repo
                SnapSideloaderStore.createRepoList(repositories);//save to list file
                updateRepoBox();//update box
                JOptionPane.showMessageDialog(this, "Repository added successfully!", "Repository Added", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(this, "Couldn't download file for repository named " + repoName + " at URL:\n" + repoURL
                        + "\nCheck if the URL is correct, or if your internet connection is stable.\n" + exception.getMessage(), "Repository not added", JOptionPane.ERROR_MESSAGE);
            }
            //if can't download, do not add to list

            repoBox.setSelectedItem(null);


        });
        cancelButton.addActionListener(e -> dispose());
        actionRepositoryButton.addActionListener(e -> {
            if (addRemoveModeRadioButton.isSelected()) {//REMOVE mode
                if (repoBox.getSelectedItem().toString().equals(currentRepo)) {
                    JOptionPane.showMessageDialog(this, "You cannot remove the repository that is currently in use.", "Illegal Action", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this repository?", "Removal Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    repositories.remove(repoBox.getSelectedItem().toString());//remove repo from HashMap
                    SnapSideloaderStore.createRepoList(repositories);//save changes to repo file
                    reponameField.setText("");
                    repoaddrField.setText("");
                    File repoFile = new File(dbDirectory + repoBox.getSelectedItem().toString() + ".sqlite");
                    repoFile.delete();
                    updateRepoBox();
                }

            } else {
                repositories.put(reponameField.getText(), repoaddrField.getText()); //EDIT mode
                try {
                    SnapSideloaderStore.downloadFile(repoaddrField.getText(), dbDirectory, reponameField.getText() + ".sqlite");
                    repositories.put(reponameField.getText(), repoaddrField.getText());//add repo if can download
                    SnapSideloaderStore.createRepoList(repositories);//save to list file
                    JOptionPane.showMessageDialog(this, "Repository modified successfully!", "Repository Saved", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException exception) {
                    JOptionPane.showMessageDialog(this, "Couldn't download file for repository named " + reponameField.getText() + " at URL:\n" + repoaddrField.getText()
                            + "\nCheck if the URL is correct, or if your internet connection is stable.\n Make sure the original file isn't open elsewhere.", "Repository not added", JOptionPane.ERROR_MESSAGE);
                }

            }//edit current entry
        });
        repoBox.addActionListener(e -> { //aici continua
            String selectedreponame = repoBox.getSelectedItem().toString(); //TODO: add try catch
            reponameField.setText(selectedreponame);
            repoaddrField.setText(repositories.get(selectedreponame));
            actionRepositoryButton.setEnabled(true);
        });
        editModeRadioButton.addActionListener(e -> {
            //reponameField.setEditable(true);
            repoaddrField.setEditable(true);
            addRemoveModeRadioButton.setSelected(false);
            addNewRepositoryButton.setVisible(false);
            actionRepositoryButton.setText("Save Changes");
        });
        addRemoveModeRadioButton.addActionListener(e -> {
            //reponameField.setEditable(false);
            repoaddrField.setEditable(false);
            editModeRadioButton.setSelected(false);
            addNewRepositoryButton.setVisible(true);
            actionRepositoryButton.setText("Remove Repository");
        });


    }

    private void updateRepoBox() {
        String[] repoNames = repositories.keySet().toArray(new String[0]);
        DefaultComboBoxModel model = new DefaultComboBoxModel(repoNames);
        repoBox.setModel(model);
    }

    private boolean checkSyntax(String repoURL) {
        try {
            new URI(repoURL).toURL();//this way checks if it's a valid URI, then checks if it's a valid URL
            return true;
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            return false;
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
        repomanPanel = new JPanel();
        repomanPanel.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        final JLabel label1 = new JLabel();
        label1.setText("Select a repo to edit its properties:");
        CellConstraints cc = new CellConstraints();
        repomanPanel.add(label1, cc.xy(1, 3));
        repoBox = new JComboBox();
        repomanPanel.add(repoBox, cc.xy(1, 5));
        addNewRepositoryButton = new JButton();
        addNewRepositoryButton.setHorizontalAlignment(0);
        addNewRepositoryButton.setHorizontalTextPosition(11);
        addNewRepositoryButton.setText("Add New Repository");
        repomanPanel.add(addNewRepositoryButton, cc.xy(3, 5));
        final JLabel label2 = new JLabel();
        label2.setText("Name:");
        repomanPanel.add(label2, cc.xy(1, 7));
        reponameField = new JTextField();
        reponameField.setEditable(false);
        reponameField.setEnabled(true);
        reponameField.setForeground(new Color(-1740253));
        repomanPanel.add(reponameField, cc.xyw(1, 9, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label3 = new JLabel();
        label3.setText("Repository Address:");
        repomanPanel.add(label3, cc.xy(1, 11));
        repoaddrField = new JTextField();
        repoaddrField.setEditable(false);
        repomanPanel.add(repoaddrField, cc.xyw(1, 13, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        repomanPanel.add(panel1, cc.xyw(1, 15, 3));
        cancelButton = new JButton();
        cancelButton.setText("Return");
        panel1.add(cancelButton);
        actionRepositoryButton = new JButton();
        actionRepositoryButton.setEnabled(false);
        actionRepositoryButton.setText("Remove Repository");
        panel1.add(actionRepositoryButton);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        repomanPanel.add(panel2, cc.xyw(1, 1, 3));
        addRemoveModeRadioButton = new JRadioButton();
        addRemoveModeRadioButton.setSelected(true);
        addRemoveModeRadioButton.setText("Add/Remove Mode");
        panel2.add(addRemoveModeRadioButton);
        editModeRadioButton = new JRadioButton();
        editModeRadioButton.setText("Edit Mode");
        panel2.add(editModeRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return repomanPanel;
    }
}

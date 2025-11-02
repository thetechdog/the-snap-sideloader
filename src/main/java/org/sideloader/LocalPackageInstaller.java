package org.sideloader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

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
    private int status=0;
    private PackagePropertyInterrogator packageInfo;
    public LocalPackageInstaller(SnapSideloaderStore storeWindow){
        LPIFrame=storeWindow; // setting the frame to the same as the store one
        LPIFrame.setTitle("The Snap Sideloader - Local Package Mode");
        setMinimumSize(new Dimension(680,480));
        Dimension d=new Dimension(700,600);
        setSize(d);
        setMinimumSize(d);
        JTextArea packageDescription = new JTextArea(" Open a package to see its description here!");
        packageDescription.setEditable(false); packageDescription.setFont(new Font("Ubuntu Sans", Font.ITALIC, 16));
        descScrollPane.setViewportView(packageDescription);//setting scrollbar for description textbox
        add(LPIGUI);


        storeButton.addActionListener(back->{
            storeWindow.backToStore();
        });

        removePackageButton.addActionListener(re->{
            try{
                int answer=JOptionPane.showConfirmDialog(LPIFrame,"Do you want to back up user data?","Package Removal",
                        JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
                if(answer==JOptionPane.CANCEL_OPTION) return;
                if(Main.getAuth().isEmpty()) PasswordPrompter.passwordPrompt(LPIFrame);
                if(answer==JOptionPane.YES_OPTION){//normal removal with snapshot
                    String removeCommand=" echo "+Main.getAuth()+" | sudo -S -k "+"snap remove "+packageInfo.getName();
                    int exitCode = Main.pendingActionDialog(LPIFrame, removeCommand, "The package is being removed. Please wait...", "Removing Package");
                    if(exitCode==0){
                        JOptionPane.showMessageDialog(LPIFrame,"Package removed successfully!\nYou can use the snapshot manager if you want to restore user data.",
                                "Success",JOptionPane.INFORMATION_MESSAGE);
                        status=1;
                        actionButton.setText("Install Package");
                        classicInstallCheck.setEnabled(true);
                        classicInstallCheck.setSelected(false);
                        removePackageButton.setEnabled(false);
                        classicInstallCheck.setVisible(true);
                    }
                    else{
                        JOptionPane.showMessageDialog(LPIFrame,"Package removal error!","Error",JOptionPane.ERROR_MESSAGE);
                        Main.setAuth("");
                    }
                    return;
                }
                if(answer==JOptionPane.NO_OPTION){//purge
                    String removeCommand=" echo "+Main.getAuth()+" | sudo -S -k "+"snap remove --purge "+packageInfo.getName();
                    int exitCode = Main.pendingActionDialog(LPIFrame, removeCommand, "The package is being removed. Please wait...", "Removing Package");
                    if(exitCode==0){
                        JOptionPane.showMessageDialog(LPIFrame,"Package removed successfully!", "Success",JOptionPane.INFORMATION_MESSAGE);
                        status=1;
                        actionButton.setText("Install Package");
                        classicInstallCheck.setEnabled(true);
                        classicInstallCheck.setSelected(false);
                        removePackageButton.setEnabled(false);
                    }
                    else{
                        JOptionPane.showMessageDialog(LPIFrame,"Package removal error!","Error",JOptionPane.ERROR_MESSAGE);
                        Main.setAuth("");
                    }
                    return;
                }
            }
            catch(Exception e){JOptionPane.showMessageDialog(LPIFrame,"An exception occurred:\n"+e.getClass().getName());e.printStackTrace();}});

        actionButton.addActionListener(ae-> {
                //status: 0=nothing opened, 1=package opened and not installed, 2=package opened and installed
                if (status==0) {
                    String user = System.getProperty("os.user");
                    String path = "";
                    JFileChooser fileChooser = new JFileChooser();
                    FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("Snap package", "snap");
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setDialogTitle("Select a snap package file");
                    fileChooser.setFileFilter(fileFilter);
                    int answer = fileChooser.showOpenDialog(LPIFrame);
                    if (answer == JFileChooser.APPROVE_OPTION) {
                        try{
                            path = fileChooser.getSelectedFile().getAbsolutePath();

                            status = 1;
                            String packageDetails = CommandOutputter.getOutput("snap info " + path);
                            packageInfo = new PackagePropertyInterrogator(packageDetails);
                            packageInfo.setPath(path);
                            packageName.setText(packageInfo.getName());
                            packageSummary.setText(packageInfo.getSummary());
                            packageVersion.setText("Version: " + packageInfo.getVersion());
                            packageLicense.setText("License: " + packageInfo.getLicense());
                            packageDescription.setText("  "+packageInfo.getDescription());
                            packagePath.setText("Path: " + "\"" + path + "\"");
                            actionButton.setText("Install Package");
                            classicInstallCheck.setEnabled(true);
                            clearButton.setEnabled(true);


                            //check if package is already installed (checks snap packages install path) and set to status 2 instead
                            String check="test -f /snap/bin/"+packageInfo.getName()+" && echo y || echo n";
                            if(CommandOutputter.getOutput(check).contains("y")){//package is indeed installed already
                                status=2;
                                actionButton.setText("Upgrade Package");
                                removePackageButton.setEnabled(true);
                                classicInstallCheck.setVisible(false);
                                clearButton.setEnabled(true);
                            }
                        }
                        catch(Exception e){
                            status=0;actionButton.setText("Open Package");
                            JOptionPane.showMessageDialog(LPIFrame,"An exception occurred:\n"+e.getClass().getName()+
                                    "\nIt's highly likely that the selected file is not a valid snap package.","Aw, snap!",JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }


                    }
                    return;
                }

                if (status==1){
                    try{
                        if(Main.getAuth().isEmpty()) PasswordPrompter.passwordPrompt(LPIFrame);

                        String installCommand=" echo "+Main.getAuth()+" | sudo -S -k "+"snap install "+packageInfo.getPath()+" --dangerous";
                        if(classicInstallCheck.isSelected()){//classic confinement
                            int answerC=JOptionPane.showConfirmDialog(LPIFrame,"Installing a classic confinement package means giving it full access to the system.\nProceed only if you trust the package source.",
                                    "Classic Confinement",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
                            if(answerC==JOptionPane.CANCEL_OPTION)return;
                            installCommand=" echo "+Main.getAuth()+" | sudo -S -k "+"snap install "+packageInfo.getPath()+" --dangerous --classic";
                        }
                        int exitCode=Main.pendingActionDialog(LPIFrame, installCommand, "The package is being installed. Please wait...", "Installing Package");
                        if (exitCode==0){
                            JOptionPane.showMessageDialog(LPIFrame,"Package Installed!","Success",JOptionPane.INFORMATION_MESSAGE);
                            status=2;
                            actionButton.setText("Upgrade Package");
                            classicInstallCheck.setVisible(false);
                            removePackageButton.setEnabled(true);
                            clearButton.setEnabled(true);
                        }
                        else {
                            JOptionPane.showMessageDialog(LPIFrame,"Package install error!\nCheck your password and try again.\nIf your package requires classic confinement, check the box and try again.",
                                    "Error",JOptionPane.ERROR_MESSAGE);
                            Main.setAuth("");//reset auth in case of wrong password
                        }


                    }
                    catch (Exception e){JOptionPane.showMessageDialog(LPIFrame,"An exception occurred:\n"+e.getClass().getName());e.printStackTrace();}
                    return;


                }

                if (status==2){
                    try{
                        if(Main.getAuth().isEmpty()) PasswordPrompter.passwordPrompt(LPIFrame);

                        String upgCommand=" echo "+Main.getAuth()+" | sudo -S -k "+"snap install "+packageInfo.getPath()+" --dangerous";
                        if(classicInstallCheck.isSelected()){//confirmation to proceed for classic confinement
                            int answerC=JOptionPane.showConfirmDialog(LPIFrame,"Installing a classic confinement package means giving it full access to the system.\nProceed only if you trust the package source.",
                                    "Classic Confinement",JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
                            if(answerC==JOptionPane.CANCEL_OPTION)return;
                            upgCommand=" echo "+Main.getAuth()+" | sudo -S -k "+"snap install "+packageInfo.getPath()+" --dangerous --classic";
                        }
                        int exitCode = Main.pendingActionDialog(LPIFrame, upgCommand, "The package is being upgraded. Please wait...", "Upgrading Package");
                        if(exitCode==0){
                            JOptionPane.showMessageDialog(LPIFrame,"Package upgraded successfully!","Success",JOptionPane.INFORMATION_MESSAGE);
                        }
                        else{
                            JOptionPane.showMessageDialog(LPIFrame,"Package upgrade error!","Error",JOptionPane.ERROR_MESSAGE);
                            Main.setAuth("");
                        }


                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(LPIFrame,"An exception occurred:\n"+e.getClass().getName());e.printStackTrace();
                    }


                }

        });

        clearButton.addActionListener(ce->{//clear all values
            status=0;
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

    public JPanel getLocalPanel(){
        return LPIGUI;
    }


}

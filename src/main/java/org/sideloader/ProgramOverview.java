package org.sideloader;

import com.jgoodies.forms.layout.CellConstraints;
import org.imgscalr.Scalr;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ProgramOverview extends JPanel{
    private JPanel mainPanel;
    private JLabel iconLabel;
    private JPanel topPanel;
    private JLabel prettyNameLabel;
    private JLabel devLabel;
    private JComboBox versionBox;
    private JLabel packageLabel;
    private JLabel categoryLabel;
    private JLabel licenseLabel;
    private JLabel sizeLabel;
    private JLabel screenshotLabel;
    private JTextArea descriptionArea;
    private JPanel bottomPanel;
    private JButton backButton;
    private JPanel actionPanel;
    private JButton removeButton;
    private JButton installButton;
    private JLabel summaryLabel;
    private JLabel confinementLabel;
    private JPanel centerPanel;
    private JScrollPane screenshotScroll;
    private JLabel devContactLabel;
    private JLabel devSiteLabel;
    private JScrollPane descriptionScroll;
    private JButton queueButton;
    private JProgressBar progressBar;
    private JPanel suggPanel;
    private JButton suggApp1;
    private JButton suggApp2;
    private JButton suggApp3;
    private JPanel suggAppsPanel;
    private JScrollPane suggAppsScroll;
    private JPanel previousPanel;
    private JScrollPane storeContentPane;
    private HashMap<String, ArrayList<String>> installedApps; //key: package name, value: [version date, version number, repo]
    private boolean installedStatus=false; //0=not installed, 1=installed
    private String previousInstallButtonText;
    private long programDownloadSize;
    private ProgramDownloadWorker downloadWorker;
    private SnapSideloaderStore storeWindow_;
    private ArrayList[] programVersions; //ReleaseDate, VersionName, VersionNumber, DownloadLink, Hash
    private String packageName;
    boolean[] suggAppsButtonsEnabled=new boolean[3];
    //TODO: implement alpha/beta version support

    public ProgramOverview(JPanel previousPanel, JScrollPane storeContentPane, HashMap<String, ArrayList<String>> installedApps) { //key: package name, value: [version date, version number, repo]
        mainPanel.setPreferredSize(new Dimension(1005,1010));
        add(mainPanel);
        this.previousPanel=previousPanel;
        this.storeContentPane=storeContentPane;
        this.installedApps=installedApps;

        Container storeWindow=storeContentPane.getParent().getParent().getParent().getParent(); //get current instance of SnapSideloaderStore
        storeWindow_=(SnapSideloaderStore)storeWindow;
        suggPanel.setVisible(false);//will be visible if there are suggestions

        backButton.addActionListener((e)->{
            //if(previousPanel.getClass().getSimpleName().equals("InstalledAndUpdatesPanel")){new InstalledAndUpdatesPanelLoader(storeWindow_.getCurrentRepo(), SnapSideloaderStore.installedApps, SnapSideloaderStore.dbop).execute(); return;}
            storeContentPane.setViewportView(previousPanel);


            storeContentPane.getVerticalScrollBar().setValue(0);
        });

        installButton.addActionListener(e -> {
            if(installButton.getText().equals("Cancel")){
                boolean cancel = false;
                if(downloadWorker!=null && !downloadWorker.isDone()) cancel=downloadWorker.cancel(true);
                if(cancel) System.out.println("Download cancel request sent.");
                return;
            }
            previousInstallButtonText=installButton.getText();
            //getting current button states
            boolean queueButtonState=queueButton.isEnabled();
            boolean removeButtonState=removeButton.isEnabled();
            //check if the program is installed from the current repo
            try{
                if(!storeWindow_.getCurrentRepo().equals(installedApps.get(packageName).get(2))){
                int result=JOptionPane.showOptionDialog(storeWindow_, "The program is installed from and managed by another repository.\nDo you want to continue anyways?", "Management Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, "No");
                if(result==JOptionPane.NO_OPTION || result==JOptionPane.CLOSED_OPTION){
                    System.out.println("Installation canceled.");
                    return;
                }
            }
            }
            catch(NullPointerException ex){System.out.println("Program is not installed, continuing.");}
            //prevents the window from closing while installing, enable after install is done
            storeWindow_.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            //set password if not set
            if(Main.getAuth().equals("")) PasswordPrompter.passwordPrompt(storeWindow_);
            //setting up UI
            storeWindow_.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            queueButton.setEnabled(false);
            removeButton.setEnabled(false);
            backButton.setEnabled(false);
            versionBox.setEnabled(false);
            //handling suggested app buttons
            suggApp1.setEnabled(false);
            suggApp2.setEnabled(false);
            suggApp3.setEnabled(false);
            //disable storePanel components while loading, the loader/worker classes will enable them later
            Container StorePanelContainer=storeContentPane.getParent();
            JPanel StorePanel=(JPanel)StorePanelContainer;
            Component[] components=StorePanel.getComponents();
            for(Component component:components){
                component.setEnabled(false);
            }
            installButton.setText("Cancel");
            //start the installation (download and install) process
            if(programVersions[4].get(versionBox.getSelectedIndex())==null){
                System.err.println("No hash available for version "+programVersions[1].get(versionBox.getSelectedIndex()));
                downloadWorker= new ProgramDownloadWorker(progressBar,programDownloadSize,programVersions[3].get(versionBox.getSelectedIndex()).toString(),SnapSideloaderStore.getDbDirectory(),this,queueButtonState,removeButtonState, installButton, null);
            }
            else
                downloadWorker= new ProgramDownloadWorker(progressBar,programDownloadSize,programVersions[3].get(versionBox.getSelectedIndex()).toString(),SnapSideloaderStore.getDbDirectory(),this,queueButtonState,removeButtonState, installButton, programVersions[4].get(versionBox.getSelectedIndex()).toString());
            downloadWorker.execute();

        });
        //listener for changing version
        versionBox.addActionListener(e -> {
            programDownloadSize=SnapSideloaderStore.getDownloadSize(programVersions[3].get(versionBox.getSelectedIndex()).toString());
            sizeLabel.setText("Size: "+SnapSideloaderStore.convertDownloadSize(programDownloadSize));
            versionBox.setToolTipText("Release date: "+programVersions[0].get(versionBox.getSelectedIndex()).toString());
            operationVersionChecker(programVersions[2].get(versionBox.getSelectedIndex()).toString(), packageName); //check text to display depending on whether it's up to date relative to selected version
        });

        removeButton.addActionListener(e -> {
            storeWindow_.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            storeWindow_.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            backButton.setEnabled(false);
            removeButton.setEnabled(false); //disable remove button
            versionBox.setEnabled(false);
            //disable storePanel components while loading, the loader/worker classes will enable them later
            Container StorePanelContainer=storeContentPane.getParent();
            JPanel StorePanel=(JPanel)StorePanelContainer;
            Component[] components=StorePanel.getComponents();
            for(Component component:components){
                component.setEnabled(false);
            }
            //handling suggested app buttons
            suggApp1.setEnabled(false);
            suggApp2.setEnabled(false);
            suggApp3.setEnabled(false);
            //set password if not set
            if(Main.getAuth().equals("")) PasswordPrompter.passwordPrompt(storeWindow_);
            //start removal
            new ProgramCommandWorker(" echo "+Main.getAuth()+" | sudo -S -k "+"snap remove --purge "+packageName,progressBar,null,this,queueButton.isEnabled(),removeButton.isEnabled(),false).execute();
        });

    }
    //METHODS
    public static LocalDateTime parseDate(String dateString){
        //trying to get the latest release date
        LocalDateTime selectedDateTime;
        try{selectedDateTime=LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            System.out.println(selectedDateTime);}
        catch(DateTimeParseException e){
            //System.out.println("No time specified.");
            LocalDate selectedDate= LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            selectedDateTime=selectedDate.atStartOfDay();}
        //System.out.println(selectedDateTime);
        return selectedDateTime;
    }

    //if program is installed, check what text to display in install button
    private void operationVersionChecker(String versionNumber, String packageName){
        if(!installedStatus) return;
        int selectedVersionNumber=Integer.parseInt(versionNumber); //get the version number of the selected version
        ArrayList<String> packageInfo=installedApps.get(packageName);
        int installedVersionNumber=Integer.parseInt(packageInfo.get(1)); //get the version number of the installed version
        int latestVersionNumber=Integer.parseInt(programVersions[2].getFirst().toString()); //get the version number of the latest version
        if(installedVersionNumber<selectedVersionNumber) {
            installButton.setText("Update");
            installButton.setEnabled(true);
        }
        else if (installedVersionNumber>selectedVersionNumber) {
            installButton.setText("Downgrade");
            installButton.setEnabled(true);
        }
        else if (installedVersionNumber==latestVersionNumber) {
            installButton.setText("Up to date");
            installButton.setEnabled(false);
        }
        else {installButton.setText("Installed");installButton.setEnabled(false);}
    }

    public void restorePanel(boolean queueButtonState, boolean removeButtonState, int resType){ //resType 1 - download error, 2 - download canceled, 0 - successful install, 3 - successful uninstall, 4 - install error
        //restore control
        storeWindow_.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        storeWindow_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        backButton.setEnabled(true);
        versionBox.setEnabled(true);
        //try handle empty button bug (when button is disabled cannot get text)
        if(previousInstallButtonText==null) operationVersionChecker(programVersions[2].getFirst().toString(), packageName);
        else installButton.setText(previousInstallButtonText);
        //restore remove button state
        queueButton.setEnabled(queueButtonState);
        removeButton.setEnabled(removeButtonState);
        //enable storePanel components
        Container StorePanelContainer=storeContentPane.getParent();
        JPanel StorePanel=(JPanel)StorePanelContainer;
        Component[] components=StorePanel.getComponents();
        for(Component component:components){
            component.setEnabled(true);
        }
        if(suggAppsButtonsEnabled[0]) suggApp1.setEnabled(true); if(suggAppsButtonsEnabled[1]) suggApp2.setEnabled(true);if(suggAppsButtonsEnabled[2]) suggApp3.setEnabled(true);//restore suggestion buttons
        if(resType==2){//JOptionPane.showMessageDialog(storeWindow_,"Installation canceled.");
            installButton.setEnabled(true);}
        else if (resType==1){JOptionPane.showMessageDialog(storeWindow_,"Download failed. Make sure your connection isn't busted and try again.","Unable to connect",JOptionPane.WARNING_MESSAGE); installButton.setEnabled(true);}
        else if (resType==4){ installButton.setEnabled(true);}
        if (resType==3){installedStatus=false; //if uninstall successful
            queueButton.setEnabled(true);
            removeButton.setEnabled(false);
            installButton.setEnabled(true);
            installButton.setText("Install");
            installedApps.remove(packageName);
            SnapSideloaderStore.createInstalledPackageList(installedApps); //update installed programs file
            }
        else if (resType==0){ //if install successful
            queueButton.setEnabled(false);
            removeButton.setEnabled(true);
            installButton.setEnabled(false);
            installButton.setText("Installed");
            installedStatus=true;
            installedApps.put(packageName,new ArrayList<String>(Arrays.asList(programVersions[0].get(versionBox.getSelectedIndex()).toString(),programVersions[2].get(versionBox.getSelectedIndex()).toString(),storeWindow_.getCurrentRepo())));//pkg name, date, versionnumber, repo
            SnapSideloaderStore.createInstalledPackageList(installedApps);

        }
        else{operationVersionChecker(programVersions[2].get(versionBox.getSelectedIndex()).toString(), packageName); removeButton.setEnabled(true);} //for uninstall failure
        if(installButton.getText().equals("Install")) removeButton.setEnabled(false); //hacky way to fix remove button still active after uninstall for some reason when it should be disabled bug
        return;
    }

    public void setUpProgramOverview(Icon programIcon, String packageName, ArrayList<String> programProps, ArrayList[] programVersions, ArrayList<String> suggApps, ArrayList[] suggAppDetails){
        //setting up variables
        this.programVersions=programVersions;
        this.packageName=packageName;

        //preparations to set up install mode based on whether program is installed
        if(installedApps.containsKey(packageName)) {installButton.setText("Up to date"); installButton.setEnabled(false); removeButton.setEnabled(true);
            queueButton.setEnabled(false); installedStatus=true;}
        //setting up the program overview
        //setting up icon
        if(programIcon!=null){//load the icon if not null, otherwise don't display it
            ImageIcon programIcon_=(ImageIcon)programIcon;
            Image programIconImage=programIcon_.getImage();
            //programIcon_=new ImageIcon(programIconImage.getScaledInstance(96,96,Image.SCALE_REPLICATE));
            programIcon_=new ImageIcon(Scalr.resize((BufferedImage) programIconImage, Scalr.Method.BALANCED, 96, 96));
            iconLabel.setText(null); iconLabel.setIcon(programIcon_);
        }
        //setting up main package properties
        packageLabel.setText(packageName);
        prettyNameLabel.setText(programProps.get(0));
        summaryLabel.setText(programProps.get(1));
        if(programProps.get(2) != null) descriptionArea.setText(programProps.get(2));
        devLabel.setText(programProps.get(3));
        //setting up developer site and contact
        CellConstraints cc = new CellConstraints(); //because of JGoodies FormsLayout
        if(programProps.get(4) != null) {
            bottomPanel.remove(devSiteLabel);
            HyperlinkJLabel newDevSiteLabel=new HyperlinkJLabel(devSiteLabel.getText(), programProps.get(4), programProps.get(4)); devSiteLabel.setToolTipText("Open Website");
            newDevSiteLabel.setToolTipText(programProps.get(4));
            bottomPanel.add(newDevSiteLabel,cc.xy(1,9)); //based on the row in the form file
        }
        else devSiteLabel.setText(devSiteLabel.getText()+"Not available.");

        if(programProps.get(5) != null) devContactLabel.setText(devContactLabel.getText()+programProps.get(5));
        else devContactLabel.setText(devContactLabel.getText()+"Not available.");

        //setting up category
        categoryLabel.setText(categoryLabel.getText()+programProps.get(6));

        //setting up license
        if(programProps.get(8) != null){topPanel.remove(licenseLabel);
            HyperlinkJLabel newLicenseLabel=new HyperlinkJLabel(licenseLabel.getText(),programProps.get(7), programProps.get(8));
            newLicenseLabel.setToolTipText(programProps.get(8));
            topPanel.add(newLicenseLabel,cc.xy(9,9));
        }
        else licenseLabel.setText(licenseLabel.getText()+programProps.get(7));

        //setting up confinement
        if(Integer.parseInt(programProps.get(9))==1)confinementLabel.setText("Confined");
        else if (Integer.parseInt(programProps.get(9))==0) confinementLabel.setText("Full Access");
        else confinementLabel.setText("???");

        //setting up screenshot
        if(programProps.get(10)==null) screenshotLabel.setText("No screenshot available.");
        else{
            BufferedImage screenshot=SnapSideloaderStore.grabImageFromUrl(programProps.get(10));
            if(screenshot!=null){//if it can retrieve screenshot (so it's not null) then display it
                try{
                    //Image screenshotImage=screenshot.getScaledInstance(screenshot.getWidth()/2,screenshot.getHeight()/2,Image.SCALE_SMOOTH);
                    Image screenshotImage;
                    if(screenshot.getWidth()<=640 && screenshot.getHeight()<=480) //if screenshot is too small, don't scale it down
                        screenshotImage=screenshot;
                    else if(screenshot.getWidth()<=1600 && screenshot.getHeight()<=900)
                        screenshotImage=Scalr.resize(screenshot, Scalr.Method.AUTOMATIC, (int) (screenshot.getWidth()*0.90), (int) (screenshot.getHeight()*0.90));//10% smaller
                    else
                        screenshotImage=Scalr.resize(screenshot, Scalr.Method.AUTOMATIC, (int) (screenshot.getWidth()*0.50), (int) (screenshot.getHeight()*0.50)); //50% smaller
                    screenshotLabel.setText(null); screenshotLabel.setIcon(new ImageIcon(screenshotImage));}
                catch(Exception e){
                    screenshotLabel.setText("Unsupported screenshot format!");
                }

            }
            else screenshotLabel.setText("Coudn't retrieve screenshot. Your connection or the server might be down.");

        }
        //setting up versions
        if(!programVersions[2].isEmpty()) {
            for(int i=0;i<programVersions[2].size();i++){
                //if(programVersions[1].get(i).toString().contains("alpha") || programVersions[1].get(i).toString().contains("beta"))
                versionBox.addItem("rev"+programVersions[2].get(i).toString()+" | v:"+programVersions[1].get(i).toString());
            }
            //when first opening, latest is automatically selected
            versionBox.setSelectedIndex(0);
            versionBox.setToolTipText("Release date: "+programVersions[0].getFirst().toString());
            programDownloadSize=SnapSideloaderStore.getDownloadSize(programVersions[3].getFirst().toString());
            sizeLabel.setText("Size: "+SnapSideloaderStore.convertDownloadSize(programDownloadSize)); //automatically get the size of the latest version (because they are ordered in descending order)
            //if package is installed, check text to display depending on whether it's up to date
            if(installedStatus) {
                operationVersionChecker(programVersions[2].getFirst().toString(), packageName);
            }
        }
        else{versionBox.setEnabled(false); installButton.setEnabled(false); removeButton.setEnabled(false); installButton.setText("Unavailable"); sizeLabel.setText("Size: unavailable");}//if no versions are available

        this.packageName=packageName;
        //setting up suggested apps if suggestions are enabled
        if(storeWindow_.getSuggestionsStatus()){
            if (suggApps != null) {//check if there are suggestions
                suggPanel.setVisible(true);
                JButton[] suggButtons=new JButton[]{suggApp1,suggApp2,suggApp3};
                for(int i=0; i<suggApps.size();i++){//depending on how many there are, enable them
                    suggButtons[i].setEnabled(true);
                    suggAppsButtonsEnabled[i]=true;//mark suggestion button states
                    SnapSideloaderStore.styleButton(suggButtons[i], (String) suggAppDetails[0].get(i), (String) suggAppDetails[1].get(i), (String) suggAppDetails[2].get(i), (String) suggAppDetails[3].get(i));
                    final int finalI = i;
                    suggButtons[i].addActionListener(e -> {
                                storeContentPane.setViewportView(new LoadingPanel(storeContentPane)); //show loading panel while loading


                                SnapSideloaderStore.loadSelectedProgram(suggButtons[finalI], storeContentPane, previousPanel);
                            }
                            );
                }
            }
        }
    }
    public boolean getConfinedStatus(){
        boolean confinedStatus;
        if(confinementLabel.getText().equals("Confined")) confinedStatus=true;
        else confinedStatus=false;
        return confinedStatus;
    }
}

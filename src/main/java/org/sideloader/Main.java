package org.sideloader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class Main{
    private static String auth="";//used for password storage during current session
    private static final String homePath=System.getProperty("user.home");
    private static final String configPath=homePath+"/.config/snap-sideloader/"; //used in main so must be static
    public static void main(String[] args) {
        OSIntegrityChecker.OSCheck(); //check if OS is Linux
        OSIntegrityChecker.snapdInstallCheck(); //first snapd check
        OSIntegrityChecker.snapRuns(); //second snapd check

        String currentUser = System.getProperty("user.home");
        System.out.println(CommandOutputter.getExitCode("mkdir "+configPath));//creates config directory if it doesn't exist
        System.out.println(CommandOutputter.getExitCode("mkdir "+homePath+"/.local/share/snap-sideloader"));//creates local data directory if it doesn't exist
        System.out.println(CommandOutputter.getExitCode("mkdir "+homePath+"/.local/share/snap-sideloader/cache"));//creates local cache directory if it doesn't exist
        if(CommandOutputter.getExitCode("test -f "+configPath+"/config.json")!=0){//creates config file if it doesn't exist
            ProgramSettings settings=new ProgramSettings((byte)0,(byte)0,false,true,
                    LocalDateTime.of(1990, 1, 1, 0, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),false, true);
            createConfigFile(settings);
        }
        else System.out.println("Config file located.");
        ProgramSettings stConfig=readConfigFile(); //load config file
        //setting theme and making window visible
        setLookAndFeel(stConfig.theme);
        SnapSideloaderStore Store=new SnapSideloaderStore(stConfig);
        Store.setVisible(true);
    }
    public static String getAuth(){
        return auth;
    }

    public static void setAuth(String auth){
        Main.auth=auth;
    }


    public static int pendingActionDialog(JFrame frame, String actionCommand, String dialogMessage, String dialogTitle) throws InterruptedException {
        AtomicInteger exitCode = new AtomicInteger();
        JDialog actionDialog = new JDialog(frame, dialogTitle, true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayout(2,1,10,10));
        actionPanel.add(new JLabel(dialogMessage));
        actionPanel.add(progressBar);
        actionPanel.setBorder(new EmptyBorder(10,7,12,7));
        actionPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        actionDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        actionDialog.add(actionPanel);
        actionDialog.pack();
        actionDialog.setSize(350,120);
        actionDialog.setLocationRelativeTo(frame);

        Thread actionThread = new Thread(() -> {//thread to manage dialog and launch process running the command
            System.out.println("Thread execution started.");
            exitCode.set(CommandOutputter.getExitCode(actionCommand)); //get exit code
            actionDialog.dispose();//close dialog, allowing EDT to continue
        });
        actionThread.start();
        actionDialog.setVisible(true); //blocks further EDT execution while visible
        actionThread.join(); //waiting to make sure the actionThread has finished
        System.out.println("Thread has finished.");
        return exitCode.get();
    }

    public static void createConfigFile(ProgramSettings settings){ //create or modify config file based on received settings
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(settings);
        try {
            FileWriter writer = new FileWriter(configPath+"/config.json");
            writer.write(json);
            writer.close();
            System.out.println("Config file created/modified.");
        } catch (IOException e) {
            System.err.println("Can't create config file.");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static ProgramSettings readConfigFile(){
        try{FileReader reader=new FileReader(configPath+"/config.json");
            Gson gson = new Gson();
            return gson.fromJson(reader,ProgramSettings.class);}
        catch (Exception e){System.err.println("Can't read config file of file invalid.");
            e.printStackTrace();
            createConfigFile(new ProgramSettings((byte)0,(byte)0,false,true,
                    LocalDateTime.of(1990, 1, 1, 0, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),false, true));;//restore defaults
            System.exit(1);return null;}

    }



    public String getWorkingPathPath() {
        return homePath;
    }


    private static void setLookAndFeel(byte themeIndex) {
        try {
            if(themeIndex==0) UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            else if(themeIndex==1) UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            else UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (Exception e) {
            System.err.println("Can't set theme.");
            e.printStackTrace();
        }
    }
}
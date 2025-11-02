package org.sideloader;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class ProgramCommandWorker extends SwingWorker<Integer, Void> {
    private final boolean opType;
    private String filePath;
    private JProgressBar progressBar;
    private ProgramOverview programOverview;
    private boolean q;
    private boolean r;
    private final String command;
    public ProgramCommandWorker(String command, JProgressBar progressBar, String filePath, ProgramOverview programOverview, boolean queueButtonState, boolean removeButtonState, boolean opType) {//opType - true=install, false=uninstall
        this.filePath = filePath;
        this.progressBar = progressBar;
        this.programOverview = programOverview;
        this.opType=opType;
        this.command=command;
        q=queueButtonState;
        r=removeButtonState;
        progressBar.setEnabled(true);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        if(opType)progressBar.setString("Installing...");
        else progressBar.setString("Uninstalling...");
        progressBar.setStringPainted(true);
    }

    @Override
    protected Integer doInBackground() throws Exception {
        int commandReturnCode=CommandOutputter.getExitCode(command);
        return commandReturnCode;
    }

    protected void done() {
        try {
            int commandReturnCode=get();

            if(opType){//when installing
                File file=new File(filePath);
                file.delete(); //clean up
                if(commandReturnCode==0){//success
                    programOverview.restorePanel(q,r,0);

                }
                else{//fail
                    Main.setAuth("");
                    JOptionPane.showMessageDialog(programOverview, "Program installation failed.\nMake sure the password is correct and try again.","Error",JOptionPane.ERROR_MESSAGE);
                    programOverview.restorePanel(q,r,4);
                }
            }
            else{ //when uninstalling
                if(commandReturnCode==0){//success
                    programOverview.restorePanel(q,r,3); //mark uninstalled
                }
                else {Main.setAuth("");//fail
                    JOptionPane.showMessageDialog(programOverview, "Program uninstall failed.\nMake sure the password is correct and try again.","Error",JOptionPane.ERROR_MESSAGE);}
                    programOverview.restorePanel(q,r,-1);
            }
        } catch (InterruptedException | ExecutionException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),"Critical Error",JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
        finally{
            progressBar.setEnabled(false);
            progressBar.setVisible(false);
        }
    }
}

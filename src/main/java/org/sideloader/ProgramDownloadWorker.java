package org.sideloader;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class ProgramDownloadWorker extends SwingWorker<Void, Long> {
    private String fileUrl;
    private String destinationFile;
    private long totalFileSize;
    private JProgressBar progressBar;
    private ProgramOverview programOverview;
    private boolean q;
    private boolean r;
    private JButton installButton;
    private String fileHash;
    public ProgramDownloadWorker(JProgressBar progressBar, long totalFileSize, String fileURL, String downloadPath, ProgramOverview programOverview, boolean queueButtonState, boolean removeButtonState, JButton installButton, String fileHash) {
        this.fileUrl = fileURL;
        this.destinationFile = downloadPath+".temp.snap";
        this.totalFileSize = totalFileSize;
        this.progressBar = progressBar;
        this.programOverview = programOverview;
        this.installButton=installButton;
        this.fileHash=fileHash;
        q=queueButtonState;
        r=removeButtonState;
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setVisible(true);
        progressBar.setEnabled(true);
        progressBar.setString("Downloading..."); //when starting
        progressBar.setStringPainted(true);
    }


    @Override
    protected Void doInBackground() throws Exception {
        URL url = new URL(fileUrl);
        URLConnection connection = url.openConnection();
        InputStream inputStream = new BufferedInputStream(connection.getInputStream());
        //download in 4KB chunks
        byte[] bufferArr = new byte[4096];
        int bytesRead;
        long downloadedBytes = 0;
        OutputStream outputStream = new FileOutputStream(destinationFile);
        //while it can download data chunks (read stores them in bufferArr), if read succeeds it returns number of bytes read, if end of stream is reached then returns -1
        while ((bytesRead = inputStream.read(bufferArr)) != -1 && !isCancelled()) { // Check for cancellation
            outputStream.write(bufferArr, 0, bytesRead);
            downloadedBytes += bytesRead; //update total downloaded bytes
            //publish data chunks as they are downloaded to process method
            publish(downloadedBytes);
        }
        inputStream.close();
        return null;
    }

    @Override
    protected void process(List<Long> chunks) {
        //called periodically through the publish() method
        //System.out.println(chunks);System.out.println(chunks.getClass());
        for (Long downloadedBytes : chunks) {
            int percentage =  (int)(((double)downloadedBytes / totalFileSize) * 100);
            progressBar.setString("Downloading: " + percentage + "%");
            progressBar.setValue(percentage);
        }
    }

    @Override
    protected void done() {
        File file=new File(destinationFile);
        //when doInBackground is done
        try {
            get(); // check for exceptions
            progressBar.setValue(100);
            System.out.println("Download finished successfully.");
            //check hash if available
            if(fileHash!=null) {
                String checkHash=CommandOutputter.getOutput("b2sum "+destinationFile +" | awk '{print $1}'");
                checkHash=checkHash.trim();
                if(!checkHash.equals(fileHash)){
                    throw new Exception("Downloaded file hash doesn't match the expected hash. File may be corrupted.");
                }
            }
            //chainload to ProgramCommandWorker to install the file
            boolean confined=programOverview.getConfinedStatus();
            String installCommand;
            if(confined) {installCommand=" echo "+Main.getAuth()+" | sudo -S -k "+"snap install "+destinationFile+" --dangerous";}
            else{installCommand=" echo "+Main.getAuth()+" | sudo -S -k "+"snap install "+destinationFile+" --dangerous --classic";}
            installButton.setEnabled(false);
            new ProgramCommandWorker(installCommand, progressBar, destinationFile, programOverview, q, r, true).execute();

        } catch (InterruptedException | ExecutionException | CancellationException e) {
            //e.printStackTrace();
            //handle exceptions from doInBackground() or if the task was canceled
            if (isCancelled()) {
                file.delete();
                System.out.println("Download canceled: " + e.getMessage());
                progressBar.setEnabled(false);
                progressBar.setVisible(false);
                programOverview.restorePanel(q,r,2);

            } else {
                file.delete();
                System.err.println("Download failed: " + e.getMessage());
                progressBar.setEnabled(false);
                progressBar.setVisible(false);
                programOverview.restorePanel(q,r,1);
            }
        }
        catch (Exception e) {
            file.delete();
            System.err.println("Download failed: " + e.getMessage());
            progressBar.setEnabled(false);
            progressBar.setVisible(false);
            programOverview.restorePanel(q, r, 1);
        }
    }


}

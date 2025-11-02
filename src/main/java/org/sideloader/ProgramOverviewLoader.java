package org.sideloader;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ProgramOverviewLoader extends SwingWorker<JPanel, Void>{
    private final DBOperator dbop;
    private final JScrollPane storeContentPane;
    private final JPanel previousPanel;
    private final String packageName;
    private final Icon programIcon;
    ProgramOverview programOverviewPanel;
    HashMap<String, ArrayList<String>> installedApps;


    public ProgramOverviewLoader(DBOperator dbop, JScrollPane storeContentPane, JPanel previousPanel, String packageName, Icon programIcon, HashMap<String, ArrayList<String>> installedApps){
        this.dbop=dbop;
        this.storeContentPane=storeContentPane;
        this.previousPanel=previousPanel;
        this.packageName=packageName;
        this.programIcon=programIcon;
        this.installedApps=installedApps;
    }


    @Override
    protected JPanel doInBackground() throws Exception {

        ArrayList<String> programProps=dbop.programQueryExecutor("SELECT PrettyName, Summary, Description, DevName, DevSite, DevContact, CategoryName, LicenseName, LicenseLink, Confinement, Screenshot " +
                "FROM (((PACKAGES P INNER JOIN DEVELOPERS D ON P.Developer=D.DevID) " +
                "INNER JOIN CATEGORIES C ON P.Category=C.CategoryID) " +
                "INNER JOIN LICENSES L ON P.License=L.LicenseID) " +
                "WHERE PackageName=\""+packageName+"\"");
        ArrayList[] programVersions=dbop.queryExecutorVersions("SELECT ReleaseDate, VersionName, VersionNumber, DownloadLink, Hash FROM VERSIONS V INNER JOIN PACKAGES P " +
                "ON V.Package=P.PackageID WHERE P.PackageName=\""+packageName+"\" ORDER BY VersionNumber DESC");

        //System.out.println("Program props: "+programProps.toString());
        //System.out.println("Program versions: "+programVersions[0].toString());
        //System.out.println(SnapSideloaderStore.getDownloadSize(programVersions[3].get(0).toString()));
        Container storeWindow=storeContentPane.getParent().getParent().getParent().getParent(); //get current instance of SnapSideloaderStore
        SnapSideloaderStore storeWindow_=(SnapSideloaderStore)storeWindow;
        //program suggestions implementation
        ArrayList<String> top3suggestedApps=new ArrayList<>();
        if(storeWindow_.getSuggestionsStatus()){
            ArrayList[] suggestedApps= dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon FROM PACKAGES P INNER JOIN CATEGORIES C ON P.Category=C.CategoryID WHERE CategoryName=\""+programProps.get(6)+"\" AND PackageName!=\""+packageName+"\" ORDER BY RANDOM() LIMIT 10");
            SimilarAppsFinder similarAppsFinder=new SimilarAppsFinder(programProps.get(1), programProps.get(2), suggestedApps, dbop);//summary and description of current program, and top 10 suggested apps
            top3suggestedApps=similarAppsFinder.startSimilarityFinder();
            //System.out.println("Top 3 suggested apps: "+top3suggestedApps);
        }
        //creating and setting up a program overview panel
        ProgramOverview programOverviewPanel=new ProgramOverview(previousPanel, storeContentPane, installedApps);
        if(storeWindow_.getSuggestionsStatus()){
            ArrayList[] sugg3AppDetails=new ArrayList[4];
            if(top3suggestedApps.size()==3)
                sugg3AppDetails= dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon FROM PACKAGES P WHERE PackageName=\""+top3suggestedApps.get(0)+"\" OR PackageName=\""+top3suggestedApps.get(1)+"\" OR PackageName=\""+top3suggestedApps.get(2)+"\"");
            else if(top3suggestedApps.size()==2)
                sugg3AppDetails= dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon FROM PACKAGES P WHERE PackageName=\""+top3suggestedApps.get(0)+"\" OR PackageName=\""+top3suggestedApps.get(1)+"\"");
            else if(top3suggestedApps.size()==1)
                sugg3AppDetails= dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon FROM PACKAGES P WHERE PackageName=\""+top3suggestedApps.get(0)+"\"");
            else sugg3AppDetails=null;
            programOverviewPanel.setUpProgramOverview(programIcon, packageName, programProps, programVersions, top3suggestedApps, sugg3AppDetails);}
        else programOverviewPanel.setUpProgramOverview(programIcon, packageName, programProps, programVersions, null, null);



        return programOverviewPanel;
    }
    @Override
    protected void done() {
        try {
            //get the created panel and display it in the storeContentPane
            programOverviewPanel = (ProgramOverview) get();
            storeContentPane.setViewportView(programOverviewPanel);//set viewport


            storeContentPane.getVerticalScrollBar().setValue(0);//reset scrollbar to top;

        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Couldn't load program overview...\nCheck if the repository file isn't corrupt...\nAn exception occurred:\n"+e.getClass().getName());
            JOptionPane.showMessageDialog(null,"Couldn't load program overview...\nCheck if the repository file isn't corrupt...\nAn exception occurred:\n"+e.getClass().getName(), "Error",JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();


            storeContentPane.setViewportView(null);//set viewport
        }
        finally {
            //enable storePanel components after loading
            Container StorePanelContainer=storeContentPane.getParent();
            JPanel StorePanel=(JPanel)StorePanelContainer;
            Component[] components=StorePanel.getComponents();
            for(Component component:components){
                component.setEnabled(true);
            }
            System.out.println("Program overview loading routine is done..");
        }
    }
}

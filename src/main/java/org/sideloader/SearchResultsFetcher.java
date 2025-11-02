package org.sideloader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class SearchResultsFetcher extends SwingWorker<JPanel, Void>{
    JScrollPane storeContentPane;
    String searchText;
    String categoryFilter;
    DBOperator dbop;
    int pageNumber;

    public SearchResultsFetcher(JScrollPane storeContentPane, String searchText, String categoryFilter, DBOperator dbop, int pageNumber){
        this.searchText=searchText;
        this.categoryFilter=categoryFilter;
        this.storeContentPane=storeContentPane;
        this.dbop=dbop;
        this.pageNumber=pageNumber;
    }

    @Override
    protected JPanel doInBackground() throws Exception {
        //calculate the starting and ending index on this page
        int upperLimit=pageNumber*5;
        int lowerLimit=upperLimit-5;
        ArrayList[] searchResults;
        long totalApps;//perform search
        if(categoryFilter.equals("All Programs")){
            if(searchText.isBlank() || searchText.equals("Type here to search")){//nothing of substance written
                searchResults= dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon " +
                        "FROM PACKAGES ORDER BY PrettyName " +
                        "LIMIT 5 OFFSET "+lowerLimit); //LIMIT to 5 results and OFFSET to start from entered number+1, DOH!!!
                totalApps= Long.parseLong(dbop.simpleQueryExecutor("SELECT COUNT(*) FROM PACKAGES"));
            }
            else{searchResults= dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon " +
                    "FROM PACKAGES " +
                    "WHERE PrettyName LIKE '%"+searchText+"%' ORDER BY PrettyName "+
                    "LIMIT 5 OFFSET "+lowerLimit);
                totalApps= Long.parseLong(dbop.simpleQueryExecutor("SELECT COUNT(*) FROM PACKAGES WHERE PrettyName LIKE '%"+searchText+"%'"));
            }
        }
        else{
            if(searchText.isBlank() || searchText.equals("Type here to search")){searchResults= dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon " +
                    "FROM PACKAGES P INNER JOIN CATEGORIES C ON P.Category=C.CategoryID " +
                    "WHERE CategoryName =\""+categoryFilter+"\" ORDER BY PrettyName "+
                    "LIMIT 5 OFFSET "+lowerLimit);
                totalApps= Long.parseLong(dbop.simpleQueryExecutor("SELECT COUNT(*) FROM PACKAGES P INNER JOIN CATEGORIES C ON P.Category=C.CategoryID WHERE CategoryName =\""+categoryFilter+"\""));
            }
            else{searchResults= dbop.queryExecutorBrowseView("SELECT PrettyName, PackageName, Summary, Icon " +
                    "FROM PACKAGES P INNER JOIN CATEGORIES C ON P.Category=C.CategoryID " +
                    "WHERE PrettyName LIKE '%"+searchText+"%' AND CategoryName =\""+categoryFilter+"\" ORDER BY PrettyName "+
                    "LIMIT 5 OFFSET "+lowerLimit);
                totalApps=Long.parseLong(dbop.simpleQueryExecutor("SELECT COUNT(*) FROM PACKAGES P INNER JOIN CATEGORIES C ON P.Category=C.CategoryID WHERE PrettyName LIKE '%"+searchText+"%' AND CategoryName =\""+categoryFilter+"\""));
            }
            }
        SearchResultsPanel searchResultsPanel=new SearchResultsPanel(storeContentPane, pageNumber, totalApps, searchText, categoryFilter, dbop);
        searchResultsPanel.setUpButtons(searchResults[0], searchResults[1], searchResults[2], searchResults[3]);
        return searchResultsPanel;
        //System.out.println(searchResults[0]);
    }

    @Override
    protected void done(){
        try {
            SearchResultsPanel searchResultsPanel=(SearchResultsPanel)get();
            storeContentPane.setViewportView(searchResultsPanel);//set viewport


            storeContentPane.getVerticalScrollBar().setValue(0);//reset scrollbar to top;

        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Couldn't load search results...\nCheck if the repository file isn't corrupt...\nAn exception occurred:\n"+e.getClass().getName());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"Couldn't load search results...\nCheck if the repository file isn't corrupt...\nAn exception occurred:\n"+e.getClass().getName(), "Error",JOptionPane.ERROR_MESSAGE);
            storeContentPane.setViewportView(null);//set viewport
        }
        finally{
            //enable storePanel components after loading
            Container StorePanelContainer=storeContentPane.getParent();
            JPanel StorePanel=(JPanel)StorePanelContainer;
            Component[] components=StorePanel.getComponents();
            for(Component component:components){
                component.setEnabled(true);
            }
            System.out.println("Search results loading routine is done..");
        }

    }
}

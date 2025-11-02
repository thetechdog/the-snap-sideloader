package org.sideloader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SearchResultsPanel extends JPanel {
    private JPanel panel1;
    private JLabel searchText;
    private JButton program1;
    private JButton program2;
    private JButton program3;
    private JButton program4;
    private JButton program5;
    private JPanel topPanel;
    private JPanel controlsPanel;
    private JButton firstButton;
    private JButton previousButton;
    private JSpinner pageSpinner;
    private JButton goButton;
    private JButton nextButton;
    private JButton lastButton;
    private JLabel pageLabel;
    private JButton[] programButtons={program1,program2,program3,program4,program5};
    JScrollPane storeContentPane;
    int pageNumber; //current page
    long totalApps;
    int totalPages;
    public SearchResultsPanel(JScrollPane storeContentPane, int pageNumber, long totalApps, String searchedText, String categoryFilter, DBOperator dbop){
        panel1.setPreferredSize(new Dimension(900,950));
        add(panel1);
        if(searchedText.equals("Type here to search"))
            searchText.setText(searchText.getText() +' '+ totalApps +" programs found");
        else
            searchText.setText(searchText.getText()+' '+totalApps+" programs found for query '"+searchedText+"'");
        this.storeContentPane=storeContentPane;
        this.pageNumber=pageNumber;
        this.totalApps=totalApps;
        totalPages=(int)(totalApps/5);
        if(totalApps%5!=0 || totalPages==0) totalPages++; //rounding up for the last partially full page
        pageSpinner.setModel(new SpinnerNumberModel(pageNumber,1,totalPages,1));//setting spinner limits: current page, min page, max page, step (1 pg)
        if(pageNumber>1){previousButton.setEnabled(true); firstButton.setEnabled(true);}
        if(pageNumber<totalPages){nextButton.setEnabled(true); lastButton.setEnabled(true);}
        pageLabel.setText(pageLabel.getText()+pageNumber+" of "+totalPages);

        System.out.println("Category filter: "+categoryFilter+" Search text: "+searchedText+" Page number: "+pageNumber);
        //action listeners
        firstButton.addActionListener(e -> changePage(1,searchedText,dbop,categoryFilter));
        previousButton.addActionListener(e -> changePage(pageNumber-1,searchedText,dbop,categoryFilter));
        nextButton.addActionListener(e -> changePage(pageNumber+1,searchedText,dbop,categoryFilter));
        lastButton.addActionListener(e -> changePage(totalPages,searchedText,dbop,categoryFilter));
        goButton.addActionListener(e -> changePage((int)pageSpinner.getValue(),searchedText,dbop,categoryFilter));

    }
    public void setUpButtons(ArrayList<String> programs, ArrayList<String> packageNames, ArrayList<String> summaries, ArrayList<String> iconURLs){
        for(int i=0;i<programs.size();i++){
            SnapSideloaderStore.styleButton(programButtons[i], programs.get(i), packageNames.get(i), (String) summaries.get(i), iconURLs.get(i));
            final int I = i;
            programButtons[i].addActionListener(e -> {
                storeContentPane.setViewportView(new LoadingPanel(storeContentPane)); //show loading panel while loading


                SnapSideloaderStore.loadSelectedProgram(programButtons[I], storeContentPane, this);
            });
        }
    }
    private void changePage(int pageNo, String searchText, DBOperator dbop, String categoryFilter){
        storeContentPane.setViewportView(new LoadingPanel(storeContentPane)); //show loading panel while loading
        //initiate search
        new SearchResultsFetcher(storeContentPane,searchText,categoryFilter,dbop,pageNo).execute();
    }

}

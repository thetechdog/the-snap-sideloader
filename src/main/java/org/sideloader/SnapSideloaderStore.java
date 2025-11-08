package org.sideloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;


public class SnapSideloaderStore extends JFrame {
    private JPanel storePanel;
    private JButton categoriesToggleButton;
    private JButton exitButton;
    private JButton settingsButton;
    private JButton updatesAndInstalledButton;
    private JList categoriesList;
    private JButton homeButton;
    private JButton installFromLocalFileButton;
    private JTextField searchField;
    private JButton searchButton;
    private JScrollPane storeContentPane;
    private JButton changeRepositoryButton;
    private JScrollPane categoriesScroller;
    private JButton helpAndAboutButton;
    private ProgramSettings stconfig;
    //private SearchResultsPanel searchResultsPanel=new SearchResultsPanel();
    private Map<String, String> repositories; //where repos are going to be loaded in
    private static final String configPath = System.getProperty("user.home") + "/.config/snap-sideloader/";
    private static final String dbDirectory = System.getProperty("user.home") + "/.local/share/snap-sideloader/"; //where the sqlite repo files and installed apps file are located
    private static final String tempDirectory = System.getProperty("user.home") + "/.local/share/snap-sideloader/cache/"; //OkHttp caching
    private static final OkHttpClient httpClient = new OkHttpClient.Builder().cache(new Cache(new File(tempDirectory), 32 * 1024 * 1024)).build(); //32MB local response cache
    private static DBOperator dbop;
    private FeaturedApps featuredAppsPanel;
    private String selectedRepo;
    private static HashMap<String, ArrayList<String>> installedApps = new HashMap<>(); //key: package name, value: [version date, version number, repo]
    private static boolean imgLoadingPref;

    public SnapSideloaderStore(ProgramSettings stConfig) {

        //loading up the settings and the repo list
        this.stconfig = stConfig;
        imgLoadingPref = stConfig.showImgs;
        setMinimumSize(new Dimension(680, 480));
        //loading up the repo list
        if (CommandOutputter.getExitCode("test -f " + configPath + "/repos.json") != 0) {//if repo list doesn't exist, create it
            createRepoList(new HashMap<>());
        } else System.out.println("Repo list file located.");
        repositories = readRepoList();//load in the repos
        //loading up the installed programs/apps list
        if (CommandOutputter.getExitCode("test -f " + dbDirectory + "/evidence.json") != 0) {//if installed programs list doesn't exist, create it
            createInstalledPackageList(new HashMap<>());
        } else System.out.println("Installed programs list file located.");
        installedApps = readInstalledPackageList();

        //guide the user to set up a repo if there are none
        boolean firstRun = false;
        while (repositories.isEmpty()) {
            RepoManager temprm = new RepoManager(repositories, true, null);
            temprm.setModal(true); //set modal so it pauses while loop execution
            temprm.setVisible(true);
            firstRun = true;
        }
        if (firstRun) {
            stConfig.lastUpdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Main.createConfigFile(stConfig);
        }//if first run, indicate last update was now
        //autoupdate repos according to preference
        if (stconfig.frequency != 4) {//if not manual
            autoRefreshChecker();
        }
        //then show list of repos as usual
        selectedRepo = selectRepo();
        System.out.println("Selected repository: " + selectedRepo); //maybe later add option for setting default repo?

        //creating the database operator based on the selected repository
        dbop = new DBOperator(dbDirectory + selectedRepo + ".sqlite");
        dbop.openConnection();

        //setting up the UI
        setTitle("The Snap Sideloader Store: " + selectedRepo);
        setIconImage(new ImageIcon(getClass().getResource("/tss.png")).getImage());
        setContentPane(storePanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        categoriesList.setSelectedIndex(0);
        categoriesList.setVisible(false);


        Color normalcolor = searchField.getForeground(); //default text color in the search box
        searchField.setText("Type here to search");
        searchField.setForeground(Color.gray);
        pack();

        //populate categories list
        populateCategoriesList();

        //setting up the content scroll pane + panel to show featured apps
        featuredAppsPanel = new FeaturedApps(storeContentPane);
        if (stConfig.showFeaturedPage) {
            loadFeaturedApps(); //prepare to load featured apps panel on another thread
        }

        //setting up actions
        installFromLocalFileButton.addActionListener(e -> {
            JPanel LPIGUI = new LocalPackageInstaller(this).getLocalPanel(); //creating panel while keeping this instance of SnapSideloaderStore
            setContentPane(LPIGUI);
            Dimension windowSize = getSize();
            pack();
            setSize(windowSize);

        });
        settingsButton.addActionListener(e -> openSettings());
        helpAndAboutButton.addActionListener(e -> {
            HelpAbout helpAbout = new HelpAbout();
            helpAbout.setLocationRelativeTo(this);
            helpAbout.setVisible(true);
        });
        exitButton.addActionListener(e -> {
            dbop.closeConnection();
            System.exit(0);
        });
        //listener for showing the placeholder text
        searchField.addFocusListener(new FocusAdapter() {
                                         @Override
                                         public void focusGained(FocusEvent e) {
                                             if (searchField.getText().equals("Type here to search")) {//check to prevent bug, only sets text to "" if nothing is written
                                                 searchField.setText("");
                                                 searchField.setForeground(normalcolor);
                                             }
                                             super.focusGained(e);
                                         }

                                         @Override
                                         public void focusLost(FocusEvent e) {
                                             if (searchField.getText().isEmpty()) {//check to prevent bug, only shows default text to "Type here to search" if nothing is written
                                                 searchField.setText("Type here to search");
                                                 searchField.setForeground(Color.gray);
                                             }
                                             super.focusLost(e);
                                         }
                                     }
        );
        //listener for pressing enter on the search bar
        searchField.addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            searchButton.doClick();
                        }
                    }
                }
        );
        categoriesToggleButton.addActionListener(e -> {
                    if (categoriesList.isVisible()) {
                        categoriesList.setVisible(false);
                        categoriesToggleButton.setText("Show Categories");
                    } else {
                        categoriesToggleButton.setText("Hide Categories");
                        categoriesList.setVisible(true);
                        categoriesList.setSelectedIndex(0);
                    }
                }
        );
        homeButton.addActionListener(e -> storeContentPane.setViewportView(featuredAppsPanel));
        changeRepositoryButton.addActionListener(e -> {
            //hide store window and clean up
            this.setVisible(false);
            storeContentPane.setViewportView(null);
            dbop.closeConnection();
            //hiding categories
            if (categoriesToggleButton.getText().equals("Hide Categories")) categoriesToggleButton.doClick();
            //reset search bar
            searchField.setText("Type here to search");
            searchField.setForeground(Color.gray);
            //showing repo selector
            selectedRepo = selectRepo();
            System.out.println("Selected repository: " + selectedRepo);
            setTitle("The Snap Sideloader Store: " + selectedRepo);
            //creating the database operator based on the selected repository
            dbop = new DBOperator(dbDirectory + selectedRepo + ".sqlite");
            dbop.openConnection();
            //showing store window again
            this.setVisible(true);
            populateCategoriesList();
            //reset the featured apps panel to its default state
            featuredAppsPanel = new FeaturedApps(storeContentPane);
            //reloading featured apps if needed
            if (stConfig.showFeaturedPage) {
                loadFeaturedApps(); //prepare to load featured apps panel on another thread
            }
        });
        searchButton.addActionListener(e -> {
            storeContentPane.setViewportView(new LoadingPanel(storeContentPane)); //show loading panel while loading
            //initiate search
            if (!categoriesList.isVisible())
                new SearchResultsFetcher(storeContentPane, searchField.getText(), "All Programs", dbop, 1).execute();
            else
                new SearchResultsFetcher(storeContentPane, searchField.getText(), categoriesList.getSelectedValue().toString(), dbop, 1).execute();

        });

        updatesAndInstalledButton.addActionListener(e -> {
            storeContentPane.setViewportView(new LoadingPanel(storeContentPane)); //show loading panel while loading
            new InstalledAndUpdatesPanelLoader(selectedRepo, installedApps, dbop, storeContentPane).execute();
        });

        setLocationRelativeTo(null);
    }

    //METHODS
    public void backToStore() {
        setContentPane(storePanel);
        setTitle("The Snap Sideloader Store: " + selectedRepo);
        pack();
    }

    private void openSettings() {
        dbop.closeConnection(); //to be able to refresh the DB
        JDialog settingsDialog = new ProgramSettingsGUI(stconfig, repositories, selectedRepo);
        settingsDialog.setModal(true);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setVisible(true);
        System.out.println("Reopening connection.");
        dbop.openConnection();
        loadFeaturedApps(); //after closing and reopening DB, reload featured apps
    }

    public static void createRepoList(Map<String, String> repositories) { //create or modify config file based on received settings
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(repositories);
        try {
            FileWriter writer = new FileWriter(configPath + "/repos.json");
            writer.write(json);
            writer.close();
            System.out.println("Repo list file created/modified.");
        } catch (IOException e) {
            System.err.println("Can't create repo list file.");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, String> readRepoList() {
        try {
            FileReader reader = new FileReader(configPath + "/repos.json");
            Gson gson = new Gson();
            return gson.fromJson(reader, HashMap.class);
        } catch (Exception e) {
            System.err.println("Can't read repo list file or file invalid.");
            e.printStackTrace();
            createRepoList(new HashMap<>());//restore defaults
            System.exit(1);
            return null;
        }

    }

    public static void createInstalledPackageList(HashMap<String, ArrayList<String>> repositories) { //create or modify installed programs file based on received settings
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(installedApps);
        try {
            FileWriter writer = new FileWriter(dbDirectory + "/evidence.json");
            writer.write(json);
            writer.close();
            System.out.println("Installed programs file created/modified.");
        } catch (IOException e) {
            System.err.println("Can't create installed programs file.");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, ArrayList<String>> readInstalledPackageList() {
        try {
            FileReader reader = new FileReader(dbDirectory + "/evidence.json");
            Gson gson = new Gson();
            return gson.fromJson(reader, HashMap.class);
        } catch (Exception e) {
            System.err.println("Can't read installed programs list file or file invalid.");
            e.printStackTrace();
            createInstalledPackageList(new HashMap<>());//restore defaults
            System.exit(1);
            return null;
        }

    }

    public static void downloadFile(String fileURL, String saveDir, String filename) throws IOException {
        URL url = new URL(fileURL);
        try (InputStream in = new BufferedInputStream(url.openStream())) {
            Path targetPath = Paths.get(saveDir, filename);
            //Path is an interface that represents the file system path, Paths is an utility class used to create a Path
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING); //copy the file in the location, overwriting if needed
        }
    }

    public static BufferedImage grabImageFromUrl(String imageUrl) {
        BufferedImage image = null;
        try {
            if (!imgLoadingPref) throw new IOException();
            //using OkHttp for faster image loading and caching
            Request urlrequest = new Request.Builder().url(imageUrl).build();
            Response urlresponse = httpClient.newCall(urlrequest).execute();
            InputStream urlin = new BufferedInputStream(urlresponse.body().byteStream());
            image = ImageIO.read(urlin);
        } catch (IOException e) {
            System.err.println("Can't retrieve image or unsupported format.");
            e.printStackTrace();
            return null;
        }
        return image;
    }

    private String selectRepo() {
        JComboBox<String> repoBox = new JComboBox<>(repositories.keySet().toArray(new String[0])); //populate the list of options
        int result;
        result = JOptionPane.showOptionDialog(null, repoBox, "Pick a Repository", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) System.exit(0);
        return repoBox.getSelectedItem().toString();
    }

    public static void styleButton(JButton button, String prettyName, String packageName, String summary, String imageURL) { //used to set up (fill in info) the package buttons
        String buttonText = "<html><b>" + prettyName + "</b><br><p style=\"font-size:12px\">" + summary + "</p></html>";
        BufferedImage image = grabImageFromUrl(imageURL);
        if (image != null) { //if program was able to download image, show icon, otherwise don't
            //image=image.getScaledInstance(48,48,Image.SCALE_SMOOTH);
            image = Scalr.resize(image, 48);
            button.setIcon(new ImageIcon(image));
        }
        button.setToolTipText(packageName);
        button.setText(buttonText);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setEnabled(true);
    }

    private void loadFeaturedApps() {
        //do the loading
        storeContentPane.setViewportView(new LoadingPanel(storeContentPane)); //show loading panel while loading
        new FeaturedAppsLoader(featuredAppsPanel, dbop, storeContentPane).execute(); //start the preparation of featuredAppsPanel in another thread using SwingWorker
    }

    public static void loadSelectedProgram(JButton clickedProgramButton, JScrollPane storeContentPane, JPanel previousPanel) {
        new ProgramOverviewLoader(dbop, storeContentPane, previousPanel, clickedProgramButton.getToolTipText(), clickedProgramButton.getIcon(), installedApps).execute();
    }

    public static long getDownloadSize(String packageFileURL) {//in bytes
        long fileSize = -1;
        try {
            URL url = new URL(packageFileURL);
            URLConnection connection = url.openConnection();
            fileSize = connection.getContentLengthLong(); //in bytes
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Can't get file size.");
            return fileSize;
        }
        return fileSize;

    }

    public static String convertDownloadSize(long downloadSize) {//conversion
        String fileSizeText = "undefined";
        double fileSize = (double) downloadSize;
        if (fileSize == -1) fileSizeText = "undefined";
        else if (fileSize < 1024) {//keep file size in bytes if it is less than 1 KB
            fileSizeText = fileSize + " bytes";
        } else if (fileSize < 1024 * 1024) {//convert to KB if fileSize is less than 1 MB
            fileSize = Math.floor(fileSize / 1024 * 100) / 100; //remove all but the first two decimals
            fileSizeText = fileSize + " KB";
        } else if (fileSize < 1024 * 1024 * 1024) {//convert to MB if fileSize is less than 1 GB
            fileSize = Math.floor(fileSize / (1024 * 1024) * 100) / 100;
            fileSizeText = fileSize + " MB";
        } else {//convert to GB
            fileSize = Math.floor(fileSize / (1024 * 1024 * 1024) * 100) / 100;
            fileSizeText = fileSize + " GB";
        }
        return fileSizeText;

    }

    private void populateCategoriesList() {
        //resetting list
        categoriesList.removeAll();
        DefaultListModel<String> categoriesModel = new DefaultListModel<>();
        //repopulating list from database
        categoriesModel.addElement("All Programs");
        ArrayList<String> categories = dbop.categoryQueryExecutor("SELECT CategoryName FROM CATEGORIES ORDER BY CategoryName ASC"); //order ascending by category name
        for (String category : categories) categoriesModel.addElement(category);
        categoriesList.setModel(categoriesModel);
        categoriesList.setBackground(storePanel.getBackground().brighter());

    }

    public static String getDbDirectory() {
        return dbDirectory;
    }

    public String getCurrentRepo() {
        return selectedRepo;
    }

    public boolean getSuggestionsStatus() {
        return stconfig.programSuggestions;
    }

    private void autoRefreshChecker() {
        try {
            System.out.println("Checking for repo updates...");
            LocalDateTime lastUpdate = LocalDateTime.parse(stconfig.lastUpdate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); //get last update date
            //prepare dialog
            JDialog messageDialog = new JDialog();
            messageDialog.setModal(true);
            messageDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            messageDialog.setTitle("Scheduled Auto Refresh");
            messageDialog.add(new JLabel("Updating repositories..."));
            messageDialog.pack();
            messageDialog.setSize(200, 150);
            Thread actionThread = new Thread(() -> {//thread to manage dialog and launch process running the command
                System.out.println("Thread execution started.");
                autoRefreshRepos(messageDialog);
            });
            if (stconfig.frequency == 3) {
                if (LocalDateTime.now().isEqual(lastUpdate.plusMonths(1)) || LocalDateTime.now().isAfter(lastUpdate.plusMonths(1))) {//if it has been a month since last update
                    actionThread.start();
                    messageDialog.setVisible(true);
                    actionThread.join();
                }
            } else if (stconfig.frequency == 2) {
                if (LocalDateTime.now().isEqual(lastUpdate.plusWeeks(1)) || LocalDateTime.now().isAfter(lastUpdate.plusWeeks(1))) {//if it has been a week since last update
                    actionThread.start();
                    messageDialog.setVisible(true);
                    actionThread.join();
                }
            } else if (stconfig.frequency == 1) {
                if (LocalDateTime.now().isEqual(lastUpdate.plusDays(1)) || LocalDateTime.now().isAfter(lastUpdate.plusDays(1))) {//if it has been a day since last update
                    actionThread.start();
                    messageDialog.setVisible(true);
                    actionThread.join();
                }
            } else if (stconfig.frequency == 0) {
                if (LocalDateTime.now().isEqual(lastUpdate.plusDays(3)) || LocalDateTime.now().isAfter(lastUpdate.plusDays(3))) {//if it has been 3 days since last update
                    actionThread.start();
                    messageDialog.setVisible(true);
                    actionThread.join();
                }
            }
        } catch (Exception e) {
            System.err.println("Can't auto update: " + e.getMessage());
        }
    }

    private void autoRefreshRepos(JDialog displayedDialog) {
        for (Map.Entry<String, String> repo : repositories.entrySet()) { //iterating over each key-value pair
            String filename = repo.getKey() + ".sqlite";
            String url = repo.getValue();
            try {
                SnapSideloaderStore.downloadFile(url, dbDirectory, filename);
            } catch (IOException e) {
                System.err.println("Couldn't download file for repository named " + repo.getKey());
                e.printStackTrace();
            }
            System.out.println("Saved " + filename);
        }//saving the db files for each repo locally
        stconfig.lastUpdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Main.createConfigFile(stconfig);//also save refresh date automatically
        displayedDialog.dispose();
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
        storePanel = new JPanel();
        storePanel.setLayout(new FormLayout("fill:161px:noGrow,left:14dlu:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        storePanel.setPreferredSize(new Dimension(1280, 720));
        exitButton = new JButton();
        exitButton.setText("Exit");
        CellConstraints cc = new CellConstraints();
        storePanel.add(exitButton, cc.xyw(1, 21, 2));
        settingsButton = new JButton();
        settingsButton.setText("Settings");
        storePanel.add(settingsButton, cc.xyw(1, 17, 2));
        updatesAndInstalledButton = new JButton();
        updatesAndInstalledButton.setText("Library");
        storePanel.add(updatesAndInstalledButton, cc.xyw(1, 13, 2));
        categoriesToggleButton = new JButton();
        categoriesToggleButton.setText("Show Categories");
        storePanel.add(categoriesToggleButton, cc.xyw(1, 5, 2));
        homeButton = new JButton();
        homeButton.setText("Home");
        storePanel.add(homeButton, cc.xyw(1, 3, 2));
        installFromLocalFileButton = new JButton();
        installFromLocalFileButton.setText("Local Sideload");
        storePanel.add(installFromLocalFileButton, cc.xyw(1, 15, 2));
        storeContentPane = new JScrollPane();
        storeContentPane.setHorizontalScrollBarPolicy(30);
        storePanel.add(storeContentPane, cc.xywh(3, 3, 4, 19, CellConstraints.FILL, CellConstraints.FILL));
        changeRepositoryButton = new JButton();
        changeRepositoryButton.setText("Change Repository");
        storePanel.add(changeRepositoryButton, cc.xyw(1, 1, 2));
        searchField = new JTextField();
        searchField.setToolTipText("Search for a program. Leave the box blank to search for all available programs in the selected category.");
        storePanel.add(searchField, cc.xyw(3, 1, 2, CellConstraints.FILL, CellConstraints.DEFAULT));
        searchButton = new JButton();
        searchButton.setText("Search");
        storePanel.add(searchButton, cc.xy(6, 1));
        categoriesScroller = new JScrollPane();
        categoriesScroller.setHorizontalScrollBarPolicy(31);
        categoriesScroller.setOpaque(true);
        storePanel.add(categoriesScroller, cc.xywh(1, 7, 2, 5, CellConstraints.FILL, CellConstraints.FILL));
        categoriesList = new JList();
        categoriesList.setBackground(new Color(-1));
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        categoriesList.setModel(defaultListModel1);
        categoriesList.setToolTipText("Pick a category to use for filtering searches.");
        categoriesScroller.setViewportView(categoriesList);
        helpAndAboutButton = new JButton();
        helpAndAboutButton.setText("Help and About");
        storePanel.add(helpAndAboutButton, cc.xyw(1, 19, 2));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return storePanel;
    }

}

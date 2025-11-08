# The Snap Sideloader
## What is it?
The Snap Sideloader (TSS) is a graphical program built with Java and Swing that can be used to manage snap packages from third party sources.  
You have the possibility of either installing packages individually from file, or installing packages from third party repositories.  
You can add as many repositories as you want and switch between them. Anyone can create a repository compatible with TSS by using the SQLite database schema available at https://github.com/thetechdog/the-snap-sideloader-repo-template as a base, and filling it with information as neceessary. As an example, check https://github.com/thetechdog/test-repo to see how a repository should look.  
Here's a short overview of the store interface:  
  
![overview](https://github.com/thetechdog/the-snap-sideloader/blob/master/images/overview.gif?raw=true "Short overview of the store interface")  

## Functionality Overview
As previously mentioned, TSS allows installing snap packages both locally and from a remote source. The focus for TSS however, is on the latter. As such, it will start in what we could call "Store Mode". If you have repositories already added, you just need to pick which one you want to browse. However, if it's the first time you're using TSS, you'll be greeted by the repository manager.  
### Repository Manager
The repository manager allows the user to add/remove repositories as well as modify existing ones. It appears the first time you use the program, and it can be accessed at any time from the store settings dialog.  
<img src="https://github.com/thetechdog/the-snap-sideloader/blob/master/images/repomanagement.png?raw=true" alt="repoman" width="500" />  
### Store Homepage
Once you select the repository you want to browse, you'll be met by the home page. This page hosts up to 6 highlighted programs as well as a custom welcome message set by the repository administrators.  
<img src="https://github.com/thetechdog/the-snap-sideloader/blob/master/images/frontpage.png?raw=true" alt="home"/> 
### Store Sidebar
This sidebar will always be visible as long as you are in store mode. Using the sidebar you can change the repository, go back home, show/hide the available program categories in the current repository, check your installed packages from the current repo, switch to local mode, open the settings dialog and finally, quit TSS.  
### Repository Search
Searching can be done by typing the terms that you want and clicking the search button, or pressing enter. The search results will be displayed with up to 5 programs per page. You can use the tools from the bottom to easily navigate between pages.  
You can also sort by category, by selecting a category from the sidebar and then executing another search.  
If you want to see all of the available programs from a selected category, leave the search field blank.  
<img src="https://github.com/thetechdog/the-snap-sideloader/blob/master/images/search.png?raw=true" alt="search" width="600"/>  
### Program Details
Once you see a program that interests you, you can click on it, at which point you'll be sent to its overview page. 

# The Snap Sideloader
## What is it?
The Snap Sideloader (TSS) is a graphical program built with Java and Swing that can be used to manage snap packages from third party sources.  
You have the possibility of either installing packages individually from a local file, or installing packages from remote third party snap package repositories.  
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
Here you can see details such as its name, its package name, the developer, confinement status, download size, license, category, summary, description, developer contact and developer site.  
If you have program suggestions enabled in settings, these will also show up, showcasing similar programs (based on summary and description). Be aware that these are computed locally and can take longer on weaker machines, as such suggestions are off by default.  
<img src="https://github.com/thetechdog/the-snap-sideloader/blob/master/images/programdetails.png?raw=true" alt="programoverview"/>  
### Installing/Uninstalling a Program
If the program has multiple versions available, you can select whichever one you want from the combo box, and click the install button, at which point TSS will download the package from the link in the repo, and install it.  
You can cancel the download before it finishes. Also, if there is a BLAKE2 hash available in the repository, the program will first check the downloaded package's hash against it before attempting to install.  
Upgrading or downgrading a package works in the same vein as installing a package. To uninstall an already installed package, click on the uninstall button, marked by the cross.  
Do be aware that any first action that requires administrative privileges will ask for your password, which will be retained until the program is closed, so that future actions do not ask again.  
### Checking Installed Programs
By clicking on the "Library" button in the sidebar, you will see a simple list of all of the installed packages, and if there are any updates available for them. Clicking on one will send the user to the program's overview page.  
### Store Settings
By clicking on the "Settings" button in the sidebar, you can access the store settings. Here you can change the program's theming, change the frequency at which repository files are refreshed automatically at the program's start, open the repository manager, enable/disable the home page, enable/disable program suggestions, enable/disable skipping image loading and you can also manually refresh the repository files.  
<img src="https://github.com/thetechdog/the-snap-sideloader/blob/master/images/settings.png?raw=true" alt="localsideload"/>  
### Local Mode
Clicking on the "Local Sideload" button in the sidebar, will put you in "Local Mode". Here you can open a snap package stored locally on your device, see its details and install/upgrade/remove it. The local sideloader is what The Snap Sideloader started as, and you can find it here: https://github.com/thetechdog/local-snap-sideloader.  
You can go back to "Store Mode" by clicking on the appropriately labeled button.  
<img src="https://github.com/thetechdog/the-snap-sideloader/blob/master/images/classicsideloader.png?raw=true" alt="localsideload" width="600"/>  
### Configuration and Repository Files Locations
The program keeps track of installed programs in ~/.local/share/snap-sideloader/ with the evidence.json file; that's also the location where repository databases are stored.  
Configuration file is stored in ~/.config/snap-sideloader/ with the config.json file; repos.json is also stored here and it keeps track of the added repositories.
## List of Features:
- Manage programs from local package files
- Manage programs from multiple third party snap package repositories in TSS-compatible format
- Add/Remove available repositories
- Browse the catalog of programs available in a repository
- Search, with filtering by category
- Program overview page with details and screenshot
- Multiple versions per program available to install
- Front page with custom greeting set by the repository administrators
- Custom categories support set by the repository administrators
- GTK theme integration (must be enabled in settings)
- Repository auto-update
- Program recommendations computed locally (must be enabled in settings)
- Installing packages with classic confinement
- BLAKE2 hash checking

## Building
Building is done using Maven. This program targets Java 21. It could probably be compiled for earlier Java versions just fine, but that's up to you to check.  

## Program Insight and Future Plans
The main inspiration for this program was F-Droid. I thought that something like that but for snap packages would be neat, especially when some people are complaining that you can't easily distribute snap packages outside of the Snap Store. This program just lays the foundation for that and it proves that it is indeed possible to distribute snap packages in such a way that anyone can basically make their own repository. Hopefully the people complaining about snap can now cross this thing from their list of complaints. That is to say, snap is a pretty awesome way to package applications, and hopefully this program pushes hesitant developers to give it a try and gives them the liberty that they craved for.  
As for The Snap Sideloader, it could be improved drastically, from the user experience to the functionality, not to mention some bugfixing wouldn't hurt. But honestly, I am not sure TSS will see much improvement because I am pretty content with what I have achieved here. To put it another way, if you want to fork TSS and make it better than it ever was, or if you want to make your own client that is compatible with TSS-format repositories, then I salute you and you are more than welcome to do that.  
Overall I think The Snap Sideloader has a solid enough groundwork, at least in concept, and I think there are a lot of ways it could be expanded upon.  

## License
The Snap Sideloader is licensed under the Apache License 2.0 license;  
(C) Andrei Ionel 2024-2025

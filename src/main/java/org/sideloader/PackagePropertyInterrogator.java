package org.sideloader;
public class PackagePropertyInterrogator {
    private String packageDetails;
    private String packagePath;
    public PackagePropertyInterrogator(String packageDetails){
        this.packageDetails=packageDetails;
    }
    public String getName() {
        int location = packageDetails.indexOf("name:");
        String name = packageDetails.substring(location + 6);
        name = name.trim();
        name = name.substring(0, name.indexOf("\n"));
        return name;
    }

    public String getSummary() {
        int location = packageDetails.indexOf("summary:");
        String summary = packageDetails.substring(location + 8);
        summary = summary.trim();
        summary = summary.substring(0, summary.indexOf("\n"));
        return summary;
    }

    public String getVersion() {
        int location = packageDetails.indexOf("version:");
        String version = packageDetails.substring(location + 8);
        version = version.trim();
        version = version.substring(0, version.indexOf("\n"));
        try{
            version = version.substring(0, version.lastIndexOf('-'));
        }
        catch (Exception e){//build type indicator(?) not found, skipping
            }

        return version;
    }

    public String getLicense() {
        int location = packageDetails.indexOf("license:");
        String license = packageDetails.substring(location + 8);
        license = license.trim();
        license = license.substring(0, license.indexOf("\n"));
        return license;
    }

    public String getDescription() {
        int location = packageDetails.indexOf("description:");
        String description = packageDetails.substring(location + 12);
        if (description.contains(" |\n")) description=description.substring(2);
        description = description.trim();
        description = description.substring(0, description.indexOf("\ncommands:"));
        return description;
    }

    public void setPath(String path){
        this.packagePath=path;
    }
    public String getPath(){
        return packagePath;
    }


}

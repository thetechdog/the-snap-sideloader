package org.sideloader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProgramSettings {
    public byte theme; //0 - default, 1 - classic, 2 - GTK
    public byte frequency; //0 - every 3 days, 1 - daily, 2 - weekly, 3 - monthly, 4 - manual
    public boolean updateAlerts; //true - enabled, false - disabled
    public boolean showFeaturedPage; //true - enabled, false - disabled
    public String lastUpdate;
    public boolean programSuggestions; //true - enabled, false - disabled
    public boolean showImgs; //true - enabled, false - disabled
    public ProgramSettings(byte theme, byte frequency, boolean updateAlerts, boolean showFeaturedPage, String lastUpdate, boolean programSuggestions, boolean showImgs) {
        this.theme=theme;
        this.frequency=frequency;
        this.updateAlerts=updateAlerts;
        this.showFeaturedPage=showFeaturedPage;
        this.lastUpdate=lastUpdate;
        this.programSuggestions=programSuggestions;
        this.showImgs=showImgs;
    }
}

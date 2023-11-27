package org.welcomedhere.welcomed;

import java.io.Serializable;

public class Report implements Serializable {
    int businessID;
    String userID;
    boolean followUpRequested;
    String reportText;
    Boolean hasImg;
    ReportType type;
    enum ReportType{
        ADA,
        DISCRIMINATION,
        OTHER
    }

    public Report(int businessID, String userID, ReportType type, boolean followUpRequested, String reportText, Boolean hasImg)
    {
        this.businessID = businessID;
        this.userID = userID;
        this.type = type;
        this.followUpRequested = followUpRequested;
        this.reportText = reportText;
        this.hasImg = hasImg;
    }

    public void printInfo()
    {
        System.out.println("businessID: " + businessID + " userID: " + userID + " type: " + type + " followUpRequested: " + followUpRequested + " reportText: " + reportText);
    }

}

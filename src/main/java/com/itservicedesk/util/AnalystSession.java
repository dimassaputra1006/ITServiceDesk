package com.itservicedesk.util;

import com.itservicedesk.model.User;

public class AnalystSession {

    private static User currentAnalyst;

    private AnalystSession() {
        // static holder, not meant to be instantiated
    }

    public static User getCurrentAnalyst() {
        return currentAnalyst;
    }

    public static void setCurrentAnalyst(User analyst) {
        currentAnalyst = analyst;
    }

    public static boolean isAnalystSelected() {
        return currentAnalyst != null;
    }

    public static String getCurrentAnalystId() {
        return currentAnalyst != null ? currentAnalyst.getEmployeeId() : null;
    }

    public static String getCurrentAnalystName() {
        return currentAnalyst != null ? currentAnalyst.getFullName() : null;
    }

    public static void clear() {
        currentAnalyst = null;
    }
}

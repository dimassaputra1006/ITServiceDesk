package com.itservicedesk.util;

import com.itservicedesk.dao.UserDao;
import com.itservicedesk.model.User;

import java.util.List;

public class AnalystSession {
    private static User currentAnalyst;

    private AnalystSession() {}

    public static User getCurrentAnalyst() { return currentAnalyst; }
    public static void setCurrentAnalyst(User analyst) { currentAnalyst = analyst; }
    public static boolean isAnalystSelected() { return currentAnalyst != null; }
    public static String getCurrentAnalystId() {
        return currentAnalyst != null ? currentAnalyst.getEmployeeId() : null;
    }
    public static String getCurrentAnalystName() {
        return currentAnalyst != null ? currentAnalyst.getFullName() : null;
    }
    public static void clear() { currentAnalyst = null; }

    /**
     * Auto-pick an analyst only if the session doesn't have one yet.
     * Shared by every controller instead of each one re-implementing
     * the same fallback logic.
     */
    public static void ensureSelected(UserDao userDao) {
        if (isAnalystSelected()) return;
        List<User> analysts = userDao.getActiveItStaff();
        User analyst = !analysts.isEmpty() ? analysts.get(0) : userDao.findByUsername("ahmad.fauzi");
        setCurrentAnalyst(analyst);
    }
}
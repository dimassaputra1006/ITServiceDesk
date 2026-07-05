package com.itservicedesk.service;

import com.itservicedesk.dao.ActivityLogDao;
import com.itservicedesk.dao.TicketDao;
import com.itservicedesk.model.ActivityLog;
import com.itservicedesk.model.Ticket;

public class TicketService {

    private final TicketDao ticketDao = new TicketDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public boolean createTicket(Ticket ticket, String analystId) {
        boolean ok = ticketDao.insertTicket(ticket);
        if (ok) {
            log(analystId, ticket.getTicketId(), "CREATED",
                    "New ticket: " + ticket.getTitle());
        }
        return ok;
    }

    public boolean claimTicket(String ticketId, String analystId) {
        boolean ok = ticketDao.claimTicket(ticketId, analystId);
        if (ok) {
            log(analystId, ticketId, "CLAIMED", "Ticket claimed to Active Room");
        }
        return ok;
    }

    public boolean releaseTicket(String ticketId, String analystId) {
        boolean ok = ticketDao.releaseTicket(ticketId, analystId);
        if (ok) {
            log(analystId, ticketId, "RELEASED", "Ticket returned to queue");
        }
        return ok;
    }

    public boolean resolveTicket(String ticketId, String analystId) {
        boolean ok = ticketDao.resolveTicket(ticketId);
        if (ok) {
            log(analystId, ticketId, "RESOLVED", "Ticket marked as resolved");
        }
        return ok;
    }

    public boolean closeTicket(String ticketId, String analystId) {
        boolean ok = ticketDao.closeTicket(ticketId);
        if (ok) {
            log(analystId, ticketId, "CLOSED", "Ticket closed");
        }
        return ok;
    }

    private void log(String analystId, String ticketId, String action, String message) {
        activityLogDao.insert(new ActivityLog(ticketId, analystId, action, message));
    }

    public TicketDao getTicketDao() {
        return ticketDao;
    }

    public ActivityLogDao getActivityLogDao() {
        return activityLogDao;
    }
}

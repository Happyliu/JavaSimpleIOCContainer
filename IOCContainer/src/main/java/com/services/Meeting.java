package com.services;

public class Meeting implements IMeeting {

    public Meeting(IMeetingService meetingService) {
        this.meetingService = meetingService;
    }

    String[] attendees;

    @Override
    public String[] getAttendees() {
        return attendees;
    }

    @Override
    public void setAttendees(String[] attendees) {
        this.attendees = attendees;
    }

    private IMeetingService meetingService;

    @Override
    public void getMeeting() {
        attendees = meetingService.getAttendees();
    }
}
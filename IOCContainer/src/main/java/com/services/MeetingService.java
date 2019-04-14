package com.services;

public class MeetingService implements  IMeetingService {

    @Override
    public String[] getAttendees() {
        return new String[]{"Alice", "Bob", "Charlie"};
    }
}
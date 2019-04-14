package com.services;

import com.zhao.container.Container;
import com.zhao.container.IOCException;

import java.lang.reflect.InvocationTargetException;

public class Main {

    public static void main(String[] args){
        try{
            Container container=new Container("config.json");
            IMeeting meeting=container.resolve(IMeeting.class);
            meeting.getMeeting();
            for(String attendee : meeting.getAttendees()){
                System.out.println(attendee);
            }
            System.out.println(meeting.getClass());
        } catch (IOCException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

/*
 * Reminder
 * --------
 * This class represents a reminder configuration for an event.
 * It specifies how many minutes before an event the user should
 * be notified.
 *
 * This is a model class and is used only to store reminder data.
 * It does not contain any logic for triggering notifications.
 *
 */

package app.model;

public class Reminder {
    /* Variables storing information about reminder of an event mirroring the header row of "data/reminder.csv" file */ 

    private int eventId; // Acts as a link to the Event class
    private int minutesBefore;

    /* Constructor is the only way to initialize and set an Reminder object */
    public Reminder(int eventId, int minutesBefore) {
        this.eventId = eventId;
        this.minutesBefore = minutesBefore;
    }

    /* Getter Methods for retrieving the value of each instance variable as they are private following the rules of encapsulation */
    public int getEventId() {
        return eventId;
    }

    public int getMinutesBefore() {
        return minutesBefore;
    }
}
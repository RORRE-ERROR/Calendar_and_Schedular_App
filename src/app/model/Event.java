/**
 * Event
 * -----
 * This class represents a single calendar event in the Scheduler application.
 * It is a model class used to store core event information such as:
 * - unique event ID
 * - title
 * - description
 * - start date and time
 * - end date and time
 *
 *
 * This class does NOT contain any logic, file handling, or user
 * interaction code. It is purely a data holder.
 */

package app.model;

import java.time.LocalDateTime; // Date (yyyy-MM-dd) and Time (24 hour format, HH:mm)

public class Event {
    /* Variables storing core information of an event mirroring the header row of "data/event.csv" file */ 

    private int eventId; // Unique identifier for an event which connects the event with reminder, recurrence rules and additional fields
    private String title;
    private String description; // Optional
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    /* Constructor is the only way to initialize and set an Event object */
    public Event(int eventId, String title, String description, LocalDateTime starDateTime, LocalDateTime endDateTime){
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.startDateTime = starDateTime;
        this.endDateTime = endDateTime;
    }

    /* Getter Methods for retrieving the value of each instance variable as they are private following the rules of encapsulation */
    public int getEventId(){
        return eventId;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }

    public LocalDateTime getStartDateTime(){
        return startDateTime;
    }

    public LocalDateTime getEndDateTime(){
        return endDateTime;
    }
}
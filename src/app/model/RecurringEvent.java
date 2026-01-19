/*
 * RecurringEvent
 * --------------
 * This class represents the recurrence rule for an event in the Scheduler App.
 * It defines how an event repeats over time (daily, weekly, etc.).
 *
 * This class does NOT represent an actual event occurrence.
 * Instead, it stores the recurrence configuration that is linked
 * to a base Event using the eventId.
 *
 * This is a model class and contains no logic or file handling.
 */

package app.model;

import java.time.LocalDate; // Date(yyyy-MM-dd)

public class RecurringEvent {
    /* Variables storing recurrence configuration of an event linked with eventID mirroring the header row of "data/recurrent.csv" file */

    private int eventId; // Acts as a link to the Event class

    /* Defines the reccurence interval
     * Example:
     * "1d" -> repeats every 1 day
     * "1w" -> repeats every 1 week
     * "1m" -> repeats every 1 month
     * "1y" -> repeats every 1 year
     */
    private String recurrentInterval;

    /* Either recurrentTimes or recurrentEndDate is used at a time*/

    private int recurrentTimes; // Number of times the event should repeat (Inclusive), 0 if unused
    private LocalDate recurrentEndDate; // The date on which the reccurence should end, null if unused which is written as 0 in the csv file

    /* Constructor is the only way to initialize and set an RecurringEvent object */
    public RecurringEvent(int eventId, String recurrentInterval, int recurrentTimes, LocalDate recurrentEndDate){
        this.eventId = eventId;
        this.recurrentInterval = recurrentInterval;
        this.recurrentTimes = recurrentTimes;
        this.recurrentEndDate = recurrentEndDate;
    }

    /* Getter Methods for retrieving the value of each instance variable as they are private following the rules of encapsulation */
    public int getEventId(){
        return eventId;
    }

    public String getRecurrentInterval(){
        return recurrentInterval;
    }

    public int getRecurrentTimes(){
        return recurrentTimes;
    }

    public LocalDate getRecurrentEndDate(){
        return recurrentEndDate;
    }
}

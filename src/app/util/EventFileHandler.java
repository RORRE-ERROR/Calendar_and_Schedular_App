/*
 * EventFileHandler
 * ----------------
 * This utility class is responsible for handling all file operations
 * related to Event objects.
 *
 * It provides methods to:
 * - write events to a CSV file
 * - generate unique event IDs
 * - read events from a CSV file
 * - update and delete events
 * - detect time conflicts between events
 * - search events by date or date range
 *
 * This class separates persistence logic from core logic,
 */

package app.util;

import app.model.Event;

import java.util.List; // List is a collection interface
import java.util.ArrayList; // ArrayList is mutable and expansible arrays implementing List interface

import java.time.LocalDateTime;
import java.time.LocalDate;

import java.io.PrintWriter; // PrintWriter class offers high-level methods for writing formatted data 
import java.io.FileWriter; // FileWriter is a basic class for writing raw character streams to a file
import java.io.BufferedReader; // BufferedReader class provides efficient, buffered reading by wrapping an existing Reader  
import java.io.FileReader; // FileReader is a basic class for reading characters directly from a file
import java.io.IOException;

public class EventFileHandler {
    // File path where event data is stored
    public static final String FILE_PATH = "data/event.csv";

    /*
     * Writes all events to the CSV file.
     */
    public static void writeEvents(List<Event> events){
        // Try-with-resources ensures the file is closed automatically
        try(PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))){
            // Write CSV header
            pw.println("eventId,title,description,startDateTime,endDateTime");
            // Write each event as a CSV row
            for(Event e : events){
                pw.println(e.getEventId() + "," + e.getTitle() + "," + e.getDescription() + "," + e.getStartDateTime() + "," + e.getEndDateTime());
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /*
     * Generates the next available event ID.
     */
    public static int nextEventId(List<Event> events){
        int max = 0;

        for(Event e : events){
            if(e.getEventId() > max){
                max = e.getEventId();
            }
        }

        return max + 1;
    }

    /*
     * Reads all events from the CSV file and returns them as a list.
     */
    public static List<Event> readEvents(){
        // List to store all events read from the file
        List<Event> events = new ArrayList<Event>();

        // Try-with-resources ensures the file is closed automatically
        try(BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))){
            // Read and ignore the header line
            String line = br.readLine();

            // Read the file line by line
            while((line = br.readLine()) != null){
                if(line.trim().isEmpty()){
                    continue;
                }

            // Parsing each line containing String
            String[] parts = line.split(",");

            int eventId = Integer.parseInt(parts[0].trim());
            String title = parts[1].trim();

            StringBuilder descBuilder = new StringBuilder();
            for(int i = 2; i < (parts.length - 2); i++){
                if(i > 2){
                    descBuilder.append(",");
                }
                descBuilder.append(parts[i]);
            }

            String description = descBuilder.toString().trim();

            LocalDateTime starDateTime = LocalDateTime.parse(parts[parts.length - 2].trim());
            LocalDateTime endDateTime = LocalDateTime.parse(parts[parts.length - 1].trim());

            // Create a new Event object and add it to the list
            events.add(new Event(eventId, title, description, starDateTime, endDateTime));
            }
        }
        catch(IOException e){
            System.out.println("File not found!");
        }

        return events;
    }

    /*
     * Updates an existing event in the CSV file.
     */
    public static boolean updateEvent(Event updatedEvent){
        List<Event> events = readEvents();
        boolean found = false;

        // Search for the event with matching Id
        for(int i = 0; i < events.size(); i++){
            if(events.get(i).getEventId() == updatedEvent.getEventId()){
                // Replace the event
                events.set(i,updatedEvent);
                found = true;
                break;
            }
        }

        // Save changes if updated
        if(found){
            writeEvents(events);
        }

        return found;
    }

    /*
     * Deletes an event from the CSV file using its event ID.
     */
    public static boolean deleteEvent(int eventId){
        List<Event> events = readEvents();

        // Remove the event with the matching ID
        boolean removed = events.removeIf(e -> e.getEventId() == eventId );

        // Save changes if removed
        if(removed){
            writeEvents(events);
        }

        return removed;
    }

    /*
     * Checks whether a new or updated event conflicts with existing events.
     */
    public static boolean hasConflict(Event newEvent){
        List<Event> events = readEvents();

        LocalDateTime newStart = newEvent.getStartDateTime();
        LocalDateTime newEnd = newEvent.getEndDateTime();

        // Skip comparing the same event (important during updates)
        for(Event e : events){
            if(e.getEventId() == newEvent.getEventId()){
                continue;
            }

            LocalDateTime existingStart = e.getStartDateTime();
            LocalDateTime existingEnd = e.getEndDateTime();

            // Check for overlapping time intervals
            boolean overlap = newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);

            if(overlap){
                return true;
            }
        }
        return false;
    }

    /*
     * Searches for events occurring on a specific date.
     */
    public static List<Event> searchByDate(LocalDate date){
        List<Event> results = new ArrayList<>();

        List<Event> events = readEvents();

        // Check each event's start date
        for(Event e : events){
            if(e.getStartDateTime().toLocalDate().equals(date)){
                results.add(e);
            }
        }
        return results; 
    }

    /*
     * Searches for events within a date range (inclusive).
     */
    public static List<Event> searchByDateRange(LocalDate start, LocalDate end){
        List<Event> results = new ArrayList<>();

        List<Event> events = readEvents();

        // Check if the event date is within the given range
        for(Event e : events){
            LocalDate eventDate = e.getStartDateTime().toLocalDate();
            if(!eventDate.isAfter(end) && !eventDate.isBefore(start)){
                results.add(e);
            }
        }
        return results; 
    }
}

/*
 * BackupManager
 * -------------
 * This utility class handles the backup and restore functionality
 * of the Scheduler application.
 *
 * It allows all event-related data (events, recurrence rules,
 * additional fields, and reminders) to be saved into a single
 * backup file and restored later.
 *
 * This class focuses on data migration and persistence only.
 * It does not contain any user interaction or menu logic.
 */

package app.util;

import app.model.AdditionalEventFields;
import app.model.Event;
import app.model.RecurringEvent;
import app.model.Reminder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;    // HashMap is a java class which implements map interface
import java.util.List;
import java.util.Map;



public class BackupManager {
    private static final String EVENT_FILE = "data/event.csv";
    private static final String RECURRENT_FILE = "data/recurrent.csv";
    private static final String ADDITIONAL_FILE = "data/additional.csv";
    private static final String REMINDER_FILE = "data/reminder.csv";

    private static void copyFile(String filePath, PrintWriter pw) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                pw.println(line);
            }
        } catch (IOException e) {
            System.out.println("File not found!");
        }
    }

    public static void backup(String backupPath){
        try(PrintWriter pw = new PrintWriter(new FileWriter(backupPath))){
            pw.println("#EVENT");
            copyFile(EVENT_FILE, pw);

            pw.println();
            pw.println("#RECURRENT");
            copyFile(RECURRENT_FILE, pw);

            pw.println();
            pw.println("#ADDITIONAL");
            copyFile(ADDITIONAL_FILE, pw);

            pw.println();
            pw.println("#REMINDER");
            copyFile(REMINDER_FILE, pw);
        }
        catch(IOException e){
            System.out.println("Backup Failed!");
            e.printStackTrace();
        }

        System.out.println("Backup completed !");
    }

    private static boolean hasConflict(Event newEvent, List<Event> events) {
        LocalDateTime newStart = newEvent.getStartDateTime();
        LocalDateTime newEnd = newEvent.getEndDateTime();

        for (Event e : events) {
            LocalDateTime start = e.getStartDateTime();
            LocalDateTime end = e.getEndDateTime();


            boolean overlap = newStart.isBefore(end) && newEnd.isAfter(start);

            if (overlap) {
                return true;
                }
            }
        return false;
    }

    public static void restore(String backupPath) {
        List<Event> currentEvents = EventFileHandler.readEvents();
        int nextId = EventFileHandler.nextEventId(currentEvents);

        List<Event> backupEvents = new ArrayList<>();
        List<RecurringEvent> backupRecurring = new ArrayList<>();
        List<AdditionalEventFields> backupAdditional = new ArrayList<>();
        List<Reminder> backupReminders = new ArrayList<>();

        enum Section { 
            NONE, 
            EVENTS, 
            RECURRENT, 
            ADDITIONAL, 
            REMINDER
        }

        Section section = Section.NONE;

        try (BufferedReader br = new BufferedReader(new FileReader(backupPath))) {
            String line;

            while ((line = br.readLine()) != null) {

                if (line.equals("#EVENT")) {
                    section = Section.EVENTS;
                    continue;
                }
                if (line.equals("#RECURRENT")) {
                    section = Section.RECURRENT;
                    continue;
                }
                if (line.equals("#ADDITIONAL")) {
                    section = Section.ADDITIONAL;
                    continue;
                }
                if (line.equals("#REMINDER")) {
                    section = Section.REMINDER;
                    continue;
                }

                if (section == Section.NONE) {
                    continue;
                }
            
                if (line.trim().isEmpty()) {
                    continue;
                }

    
                if (line.startsWith("eventId,")) {
                    continue;
                }

                String[] parts = line.split(",", -1);

                if (section == Section.EVENTS) {
                    int oldId = Integer.parseInt(parts[0]);
                    String title = parts[1];
                    String desc = parts[2];
                    LocalDateTime start = LocalDateTime.parse(parts[3]);
                    LocalDateTime end = LocalDateTime.parse(parts[4]);
                    
                    backupEvents.add(new Event(oldId, title, desc, start, end));
                }

                else if (section == Section.RECURRENT) {
                    int oldId = Integer.parseInt(parts[0]);
                    String interval = parts[1];
                    int times = Integer.parseInt(parts[2]);
                    LocalDate endDate = parts[3].equals("0") ? null : LocalDate.parse(parts[3]);
                    
                    backupRecurring.add(new RecurringEvent(oldId, interval, times, endDate));
                }

                else if (section == Section.ADDITIONAL) {
                    int oldId = Integer.parseInt(parts[0]);
                    String location = parts[1];
                    String category = parts[2];
                    
                    backupAdditional.add(new AdditionalEventFields(oldId, location, category));
                }

                else if (section == Section.REMINDER) {
                    int oldEventId = Integer.parseInt(parts[0].trim());
                    int minutesBefore = Integer.parseInt(parts[1].trim());
                    
                    backupReminders.add(new Reminder(oldEventId, minutesBefore));
                }
            }
        }
        catch(IOException | RuntimeException e){
            System.out.println("Restore failed!");
            e.printStackTrace();
            return;
        }

        Map<Integer, Integer> idMap = new HashMap<>();
        
        List<Event> mergedEvents = new ArrayList<>(currentEvents);

        for (Event be : backupEvents) {
            Event candidate = new Event(0, be.getTitle(), be.getDescription(), be.getStartDateTime(), be.getEndDateTime());
            if (hasConflict(candidate, mergedEvents)) {
                System.out.println("Conflict Detected current event! The event " + candidate.getTitle() + " was not added.");
                continue;
            }
            int newId = nextId;
            idMap.put(be.getEventId(), newId);

            mergedEvents.add(new Event(newId, be.getTitle(), be.getDescription(), be.getStartDateTime(), be.getEndDateTime()));
            // reserve id for next event
            nextId++;
        }

        List<RecurringEvent> mergedRecurring = new ArrayList<>(RecurringFileHandler.readRecurringEvents());

        for (RecurringEvent brc : backupRecurring) {
            Integer newEventId = idMap.get(brc.getEventId());
            if (newEventId == null) continue;

            mergedRecurring.add(new RecurringEvent(newEventId, brc.getRecurrentInterval(), brc.getRecurrentTimes(), brc.getRecurrentEndDate()));
        }

        List<AdditionalEventFields> mergedAdditional = new ArrayList<>(AdditionalFileHandler.readAdditional());

        for (AdditionalEventFields row : backupAdditional) {
            Integer newEventId = idMap.get(row.getEventId());
            if (newEventId == null) continue;

            mergedAdditional.add(new AdditionalEventFields(newEventId, row.getLocation(), row.getCategory()));
        }

        List<Reminder> mergedReminders = new ArrayList<>(ReminderFileHandler.readReminders());

        for (Reminder br : backupReminders) {
            Integer newEId = idMap.get(br.getEventId());
            if (newEId == null) continue;

            mergedReminders.add(new Reminder(newEId, br.getMinutesBefore()));
        }

        EventFileHandler.writeEvents(mergedEvents);
        RecurringFileHandler.writeRecurringEvents(mergedRecurring);
        AdditionalFileHandler.writeAdditional(mergedAdditional);
        ReminderFileHandler.writeReminders(mergedReminders);

        System.out.println("Restore completed !");

    }
}

/*
 * ReminderFileHandler
 * -------------------
 * This utility class is responsible for handling file input and output
 * operations related to Reminder objects.
 *
 * It reads reminder configurations from a CSV file and writes reminder
 * data back to the file for persistence.
 *
 * This class strictly handles file operations and does not perform
 * any reminder calculation or logic.
 */

package app.util;

import app.model.Reminder;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ReminderFileHandler {
    // File path where reminder data is stored
    private static final String FILE_PATH = "data/reminder.csv";

    /*
     * Writes all reminders to the CSV file.
     */
    public static void writeReminders(List<Reminder> reminders) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            pw.println("eventId,minutesBefore");

            for (Reminder r : reminders) {
                pw.println(r.getEventId() + "," + r.getMinutesBefore());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Reads all reminders from the CSV file.
     */
    public static List<Reminder> readReminders() {
        List<Reminder> reminders = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line = br.readLine(); // Skip Header

            // Read Line by Line
            while ((line = br.readLine()) != null){
                if (line.isEmpty()) {
                    continue;
                }

                // Parsing
                String[] parts = line.split(",");

                int eventId = Integer.parseInt(parts[0].trim());
                int minutesBefore = Integer.parseInt(parts[1].trim());

                reminders.add(new Reminder(eventId, minutesBefore));
            }
        }    
        catch (IOException e) {
            System.out.println("File not found!");
        }

        return reminders;
    }
}

/*
 * RecurringFileHandler
 * --------------------
 * This utility class is responsible for handling all file operations
 * related to recurring event rules.
 *
 * It reads and writes recurrence configurations to a CSV file and
 * provides helper methods to retrieve recurrence rules for specific events.
 *
 * This class focuses only on persistence logic and does not contain
 * any logic or user interaction code.
 */

package app.util;

import app.model.RecurringEvent;

import java.util.List;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.time.LocalDate;

public class RecurringFileHandler {
    public static final String FILE_PATH = "data/recurrent.csv";

        public static void writeRecurringEvents(List<RecurringEvent> list){
        try(PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))){
            pw.println("eventId,recurrentInterval,recurrentTimes,recurrentEndDate");
            for(RecurringEvent r : list){
                pw.println(r.getEventId() + "," + r.getRecurrentInterval() + "," + r.getRecurrentTimes() + "," + (r.getRecurrentEndDate() == null? "0" : r.getRecurrentEndDate()));
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static final List<RecurringEvent> readRecurringEvents(){
         List<RecurringEvent> list = new ArrayList<>();

         try(BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))){
            String line = br.readLine();

            while((line = br.readLine()) != null){
                if(line.trim().isEmpty()){
                    continue;
                }

            String[] parts = line.split(",");

            int eventId = Integer.parseInt(parts[0].trim());
            String recurrentInterval = parts[1].trim();
            int recurrentTimes = Integer.parseInt(parts[2].trim());

            LocalDate recurrentEndDate = (parts[3].equals("0") ? null : LocalDate.parse(parts[3]));

            list.add(new RecurringEvent(eventId, recurrentInterval, recurrentTimes, recurrentEndDate));
            }
        }
        catch(IOException e){
            System.out.println("File not found!");
        }

        return list;
    }

    public static RecurringEvent searchByEventId(int eventId){
        List<RecurringEvent> list = readRecurringEvents();

        for(RecurringEvent r : list){
            if(r.getEventId() == eventId){
                return r;
            }
        }

        return null;
    }
}

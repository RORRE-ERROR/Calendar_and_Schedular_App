/*
 * AdditionalFileHandler
 * ---------------------
 * This utility class handles file operations related to additional
 * (optional) event information such as location and category.
 *
 * It reads from and writes to a CSV file that stores metadata linked
 * to events using eventId.
 *
 * This class focuses only on persistence logic and does not contain
 * any business logic or user interaction code.
 */

package app.util;

import app.model.AdditionalEventFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; // Map is java interface for storing key-value pairs

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AdditionalFileHandler {
    private static final String FILE_PATH = "data/additional.csv";

    public static void writeAdditional(List<AdditionalEventFields> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            pw.println("eventId,Location,Catagory");
            for (AdditionalEventFields a : list){
                pw.println(a.getEventId() + "," + (a.getLocation() == null ? "" : a.getLocation()) + "," + (a.getCategory() == null ? "" : a.getCategory()));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

     public static List<AdditionalEventFields> readAdditional() {
        List<AdditionalEventFields> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                if(line.trim().isEmpty()){
                    continue;
                }

                String[] parts = line.split(",", -1);

                int eventId = Integer.parseInt(parts[0].trim());

                String location = parts[1].trim().isEmpty() ? null : parts[1].trim();
                String category = parts[2].trim().isEmpty() ? null : parts[2].trim();

                list.add(new AdditionalEventFields(eventId, location, category));
            }

        } catch (IOException e) {
            System.out.println("File not found!");
        }

        return list;
    }

    public static void deleteByEventId(int eventId) {
        List<AdditionalEventFields> list = readAdditional();

        boolean removed = list.removeIf(a -> a.getEventId() == eventId);

        if (removed) {
            writeAdditional(list);
        }
    }

    public static void upsert(AdditionalEventFields fields) {

        List<AdditionalEventFields> list = readAdditional();

        list.removeIf(a -> a.getEventId() == fields.getEventId());

        list.add(fields);

        writeAdditional(list);
    }

    public static Map<Integer, AdditionalEventFields> readAdditionalMap(){
        Map<Integer, AdditionalEventFields> map = new HashMap<>();

        List<AdditionalEventFields> list = readAdditional();
        
        for(AdditionalEventFields a : list){
            map.put(a.getEventId(), a);
        }

        return map;
    }
}
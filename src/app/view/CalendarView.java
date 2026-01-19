/*
 * CalendarView
 * ------------
 * This view class is responsible for displaying calendar-based
 * representations of events to the user.
 *
 * It supports:
 * - Month view
 * - Week view
 * - Day view
 *
 * This class does not modify data or perform file writing.
 * It only reads data and formats output for display.
 */

package app.view;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.model.AdditionalEventFields;
import app.model.Event;
import app.model.RecurringEvent;
import app.util.AdditionalFileHandler;
import app.util.RecurringFileHandler;

public class CalendarView {
    public static Period parseInterval(String interval){
        if(interval == null){
            return null;
        }
        
        interval = interval.trim().toLowerCase();

        if(interval.isEmpty()){
            return null;
        }

        int len = interval.length();
        char last = interval.charAt(len - 1);

        if(Character.isDigit(last)){
            int days = Integer.parseInt(interval);
            return days > 0 ? Period.ofDays(days) : null;
        }

        int num = Integer.parseInt(interval.substring(0, len - 1));
        if(num < 0){
            return null;
        }

        return switch (last) {
            case 'd' -> Period.ofDays(num);
            case 'w' -> Period.ofDays(num * 7);
            case 'm' -> Period.ofMonths(num);
            case 'y' -> Period.ofYears(num);
            default -> null;
        };

    }

    private static List<Event> withRecurringOccurrences(List<Event> events, LocalDate rangeStart, LocalDate rangeEnd) {
        List<Event> expanded = new ArrayList<>(events);

        List<RecurringEvent> recurringRules = RecurringFileHandler.readRecurringEvents();
        if (recurringRules.isEmpty()) {
            return expanded;
        }

        for (RecurringEvent rule : recurringRules) {

            Event base = null;
            for (Event e : events) {
                if (e.getEventId() == rule.getEventId()) {
                    base = e;
                    break;
                }
            }

            Period period = parseInterval(rule.getRecurrentInterval());

            if (period == null) {
                continue;
            }

            LocalDateTime start = base.getStartDateTime();
            LocalDateTime end = base.getEndDateTime();
            long durationMinutes = ChronoUnit.MINUTES.between(start, end);

            int maxOccurrences = rule.getRecurrentTimes();
            LocalDate endDateLimit = rule.getRecurrentEndDate();


            int generated = 0;
            int occurrenceIndex = 1;

            while (true) {

                LocalDateTime occStart = start.plusYears((long) period.getYears() * occurrenceIndex).plusMonths((long) period.getMonths() * occurrenceIndex).plusDays((long) period.getDays() * occurrenceIndex);

                if (maxOccurrences > 0 && generated >= maxOccurrences) {
                    break;
                }

                generated++;

                LocalDate occDate = occStart.toLocalDate();

                if (endDateLimit != null && occDate.isAfter(endDateLimit)) {
                    break;
                }

                if (occDate.isAfter(rangeEnd)) {
                    break;
                }

                if (!occDate.isBefore(rangeStart)) {
                    LocalDateTime occEnd = occStart.plusMinutes(durationMinutes);

                    expanded.add(new Event(base.getEventId(), base.getTitle(), base.getDescription(),occStart, occEnd));
                }

                occurrenceIndex++;
            }
        } 

        return expanded;  
    }

    public static void showMonthView(List<Event> events, int year, int month) {

        YearMonth ym = YearMonth.of(year, month);
        LocalDate rangeStart = ym.atDay(1);
        LocalDate rangeEnd = ym.atEndOfMonth();

        List<Event> allEvents = withRecurringOccurrences(events, rangeStart, rangeEnd);

        LocalDate firstDay = ym.atDay(1);

        
        System.out.println("\n" + ym.getMonth() + " " + year);
        System.out.println("Su  Mo  Tu  We  Th  Fr  Sa");


        int startOffset = firstDay.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < startOffset; i++) {
            System.out.print("    ");
        }

        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);

            boolean hasEvent = allEvents.stream().anyMatch(e -> e.getStartDateTime().toLocalDate().equals(date));

            System.out.printf("%2d%s ", day, hasEvent ? "*" : " ");

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                System.out.println();
            }
        }
        System.out.println("\n");

        Map<Integer, AdditionalEventFields> additionalMap = AdditionalFileHandler.readAdditionalMap();

        for (Event e : allEvents) {
            if (e.getStartDateTime().getMonthValue() == month && e.getStartDateTime().getYear() == year) {
                System.out.println("* " + e.getStartDateTime().toLocalDate() + ": " + e.getTitle() + " (" + e.getStartDateTime().toLocalTime() + " → " + e.getEndDateTime().toLocalTime() + ")");

                
                AdditionalEventFields a = additionalMap.get(e.getEventId());

                if (a != null) {
                    if (a.getLocation() != null && !a.getLocation().trim().isEmpty()) {
                        System.out.println("    Location: " + a.getLocation());
                    }
                    if (a.getCategory() != null && !a.getCategory().trim().isEmpty()) {
                        System.out.println("    Category: " + a.getCategory());
                    }
                }

                System.out.println();
            }
        }
    }
    
    public static void showWeekView(List<Event> events, LocalDate weekStart) {

        LocalDate rangeStart = weekStart;
        LocalDate rangeEnd = weekStart.plusDays(6);

        List<Event> allEvents = withRecurringOccurrences(events, rangeStart, rangeEnd);

        Map<Integer, AdditionalEventFields> additionalMap = AdditionalFileHandler.readAdditionalMap();

        System.out.println("\n=== Week of " + weekStart + " ===");

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);

            System.out.println(date.getDayOfWeek().toString().substring(0, 3) + " " + date.getDayOfMonth() + ":");

            boolean found = false;

            for (Event e : allEvents) {
                if (e.getStartDateTime().toLocalDate().equals(date)) {
                    System.out.println("  - " + e.getTitle() + " (" + e.getStartDateTime().toLocalTime() + " → " + e.getEndDateTime().toLocalTime() + ")");

                    AdditionalEventFields a = additionalMap.get(e.getEventId());
                    if (a != null) {
                        if (a.getLocation() != null && !a.getLocation().trim().isEmpty()) {
                            System.out.println("      Location: " + a.getLocation());
                        }
                        if (a.getCategory() != null && !a.getCategory().trim().isEmpty()) {
                            System.out.println("      Category: " + a.getCategory());
                        }
                    }

                    found = true;
                }
            }

            if (!found) {
                System.out.println("  No events");
            }

            System.out.println();
        }
    }

    public static void showDayView(List<Event> events, LocalDate date) {

        LocalDate rangeStart = date;
        LocalDate rangeEnd = date;

        List<Event> allEvents = withRecurringOccurrences(events, rangeStart, rangeEnd);

        Map<Integer, AdditionalEventFields> additionalMap = AdditionalFileHandler.readAdditionalMap();

        System.out.println("\n=== " + date + " (" + date.getDayOfWeek() + ") ===");

        boolean found = false;

        for (Event e : allEvents) {
            if (e.getStartDateTime().toLocalDate().equals(date)) {

                System.out.println("- " + e.getTitle() + " (" + e.getStartDateTime().toLocalTime() + " → " + e.getEndDateTime().toLocalTime() + ")");

                AdditionalEventFields a = additionalMap.get(e.getEventId());
                if (a != null) {
                    if (a.getLocation() != null && !a.getLocation().trim().isEmpty()) {
                        System.out.println("    Location: " + a.getLocation());
                    }
                    if (a.getCategory() != null && !a.getCategory().trim().isEmpty()) {
                        System.out.println("    Category: " + a.getCategory());
                    }
                }

                System.out.println();
                found = true;
            }
        }

        if (!found) {
            System.out.println("No events");
        }
    }
}

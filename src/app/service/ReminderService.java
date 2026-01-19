/*
 * ReminderService
 * ---------------
 * This service class contains the business logic related to reminders.
 * It is responsible for determining the next upcoming reminder based on:
 * - existing events
 * - recurrence rules
 * - reminder configurations
 *
 * This class does NOT handle file input/output or user interaction.
 * It operates purely on model objects and performs calculations.
 */

package app.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import app.model.Event;
import app.model.RecurringEvent;
import app.model.Reminder;
import app.view.CalendarView;


public class ReminderService {
    /*
     * NextReminderInfo
     * ----------------
     * A helper data structure used to group information about the next
     * upcoming reminder.
     *
     * This avoids returning multiple separate values from a method.
     */
    public static class NextReminderInfo {
        public Event event;
        public LocalDateTime occurrenceStart;
        public LocalDateTime notifyAt;
        public Duration timeUntilNotify;

        public NextReminderInfo(Event event, LocalDateTime occurrenceStart, LocalDateTime notifyAt, Duration timeUntilNotify) {
            this.event = event;
            this.occurrenceStart = occurrenceStart;
            this.notifyAt = notifyAt;
            this.timeUntilNotify = timeUntilNotify;
        }
    }

     private static LocalDateTime nextOccurrenceStart(LocalDateTime baseStart, RecurringEvent rule, LocalDateTime now) {
        Period period = CalendarView.parseInterval(rule.getRecurrentInterval());
        
        if (period == null) {
            return null;
        }

        int maxOccurrences = rule.getRecurrentTimes();
        LocalDate endDateLimit = rule.getRecurrentEndDate();

        int occurrenceIndex = 0;
        LocalDateTime candidate = baseStart;

        while (true) {
            if (!candidate.isBefore(now)) {

                if (endDateLimit != null && candidate.toLocalDate().isAfter(endDateLimit)) {
                    return null;
                }

                if (maxOccurrences > 0 && occurrenceIndex >= maxOccurrences) {
                    return null;
                }

                return candidate;
            }

            occurrenceIndex++;

            if (maxOccurrences > 0 && occurrenceIndex >= maxOccurrences) {
                return null;
            }

            candidate = candidate.plusYears((long) period.getYears()).plusMonths((long) period.getMonths()).plusDays((long) period.getDays());

            if (endDateLimit != null && candidate.toLocalDate().isAfter(endDateLimit)) {
                return null;
            }
        }
    }

    public static Optional<NextReminderInfo> getNextUpcomingReminder(List<Event> events, List<RecurringEvent> recurringRules, List<Reminder> reminders, LocalDateTime now) {
        if (events == null || reminders == null) {
            return Optional.empty();
        }

        Map<Integer, Reminder> reminderMap = new HashMap<>();
        for (Reminder r : reminders) {
            reminderMap.put(r.getEventId(), r);
        }

        Map<Integer, RecurringEvent> recurringMap = new HashMap<>();
        if (recurringRules != null) {
            for (RecurringEvent re : recurringRules) {
                recurringMap.put(re.getEventId(), re);
            }
        }

        NextReminderInfo best = null;

        for (Event baseEvent: events) {
            Reminder reminder = reminderMap.get(baseEvent.getEventId());
            if (reminder == null) {
                continue;
            }

            LocalDateTime occurrenceStart = baseEvent.getStartDateTime();

            RecurringEvent rule = recurringMap.get(baseEvent.getEventId());

            if (rule != null) {
                occurrenceStart = nextOccurrenceStart(baseEvent.getStartDateTime(), rule, now);

                if (occurrenceStart == null) {
                    continue;
                }
            }

            LocalDateTime notifyAt = occurrenceStart.minusMinutes(reminder.getMinutesBefore());

            if (notifyAt.isBefore(now)) {
                continue;
            }

            Duration until = Duration.between(now, notifyAt);

            if (best == null || notifyAt.isBefore(best.notifyAt)) {
                best = new NextReminderInfo(baseEvent, occurrenceStart, notifyAt, until);
            }
        }

        return Optional.ofNullable(best);
    }

    public static String formatDuration(Duration d) {

        if (d == null) {
            return "0 minutes";
        }

        long totalMinutes = Math.max(0, d.toMinutes());
        long days = totalMinutes / (60 * 24);
        long hours = (totalMinutes % (60 * 24)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append(days == 1 ? " day" : " days");
        }
        if (hours > 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(hours).append(hours == 1 ? " hour" : " hours");
        }
        if (minutes > 0 || sb.length() == 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(minutes).append(minutes == 1 ? " minute" : " minutes");
        }

        return sb.toString();
    }

}

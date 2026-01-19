/*
 * AdditionalEventFields
 * --------------------
 * This class represents optional or additional information related to an event.
 * It is separated from the main Event class to keep the core event data simple
 * and to follow the principle of separation of concerns.
 *
 * This class stores metadata such as:
 * - location of the event
 * - category of the event
 *
 * These fields are optional and may not exist for every event.
 * This is a model class and does not contain any logic or file handling.
 */

package app.model;

public class AdditionalEventFields {
        /* Variables storing additional information of an event linked with eventID mirroring the header row of "data/additional.csv" file */

        private int eventId; // Acts as a link to the Event class

        private String location; // Optional, null if unused, which is written as empty string in the csv file
        private String category; // Optional, null if unused, which is written as empty string in the csv file

        /* Constructor is the only way to initialize and set an AdditionalEventFields object */
        public AdditionalEventFields(int eventId, String location, String category){
            this.eventId = eventId; 
            this.location = location;
            this.category = category;
        }

        /* Getter Methods for retrieving the value of each instance variable as they are private following the rules of encapsulation */
        public int getEventId(){
            return eventId;
        }

        public String getLocation(){
            return location;
        }

        public String getCategory(){
            return category;
        }
}
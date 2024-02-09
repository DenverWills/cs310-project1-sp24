package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;

public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    
    public String convertCsvToJsonString(List<String[]> csv) {
        
        // Initialize LinkedHashMaps to store course names, schedule types, and subjects 
        LinkedHashMap subjectLinkMap = new LinkedHashMap<>(); 
        LinkedHashMap courseLinkMap = new LinkedHashMap<>();
        LinkedHashMap scheduleLinkMap = new LinkedHashMap<>(); 
            
        JsonArray sectionArray = new JsonArray();

            List csvData = csv;

            // Initialize iterator to traverse the CSV data
            Iterator<String[]> iterator;
            iterator = csvData.iterator();

            JsonObject csvLineData;
            String[] headerline = iterator.next();

            // Iterate over each line in the CSV
            while (iterator.hasNext()) {
                String [] dataLine = iterator.next();
                csvLineData = new JsonObject();

                for (int i = 0; i < headerline.length; i++) {
                csvLineData.put(headerline[i], dataLine[i]);
                }

                scheduleLinkMap.put(csvLineData.get(TYPE_COL_HEADER),csvLineData.get(SCHEDULE_COL_HEADER));

                // Split course name from course number and populate subject map
                String courseName[] = csvLineData.get(NUM_COL_HEADER).toString().split(" ");
                subjectLinkMap.put(courseName[0], csvLineData.get(SUBJECT_COL_HEADER));

                // Populate course name map
                LinkedHashMap courseLinkedMap = new LinkedHashMap<>();
                courseLinkedMap.put(SUBJECTID_COL_HEADER, courseName[0]);
                courseLinkedMap.put(NUM_COL_HEADER, courseName[1]);
                courseLinkedMap.put(DESCRIPTION_COL_HEADER, csvLineData.get(DESCRIPTION_COL_HEADER));
                int credits = Integer.parseInt(csvLineData.get(CREDITS_COL_HEADER).toString());
                courseLinkedMap.put(CREDITS_COL_HEADER, credits);
                courseLinkMap.put(csvLineData.get(NUM_COL_HEADER), courseLinkedMap);

                // Populate section map
                LinkedHashMap sectionLinkedMap = new LinkedHashMap<>();
                int crn = Integer.parseInt(csvLineData.get(CRN_COL_HEADER).toString());
                sectionLinkedMap.put(CRN_COL_HEADER, crn);
                sectionLinkedMap.put(WHERE_COL_HEADER, csvLineData.get(WHERE_COL_HEADER));
                sectionLinkedMap.put(TYPE_COL_HEADER, csvLineData.get(TYPE_COL_HEADER));
                sectionLinkedMap.put(SECTION_COL_HEADER, csvLineData.get(SECTION_COL_HEADER));
                sectionLinkedMap.put(DAYS_COL_HEADER, csvLineData.get(DAYS_COL_HEADER));
                sectionLinkedMap.put(END_COL_HEADER, csvLineData.get(END_COL_HEADER));
                sectionLinkedMap.put(START_COL_HEADER, csvLineData.get(START_COL_HEADER));
                sectionLinkedMap.put(NUM_COL_HEADER, courseName[1]);
                sectionLinkedMap.put(SUBJECTID_COL_HEADER, courseName[0]);

                // Split instructor names and populate section map
                String names = csvLineData.get(INSTRUCTOR_COL_HEADER).toString();
                String[] instructorArray = names.split(", ");
                sectionLinkedMap.put(INSTRUCTOR_COL_HEADER, instructorArray);

                sectionArray.add(sectionLinkedMap);
            }
            // Construct JsonObject to hold all data
            JsonObject jsonData = new JsonObject();
            jsonData.put("scheduletype", scheduleLinkMap);
            jsonData.put("subject", subjectLinkMap);
            jsonData.put("course", courseLinkMap);
            jsonData.put("section", sectionArray);

            return Jsoner.prettyPrint(jsonData.toJson());
        }
      
        public String convertJsonToCsvString(JsonObject json) {
            JsonObject jsonObject = new JsonObject(json);    
            StringWriter sWriter = new StringWriter();

            // Extract objects from JsonObject
            JsonObject scheduleObject = (JsonObject) jsonObject.get("scheduletype"); 
            JsonObject subjectObject = (JsonObject) jsonObject.get("subject"); 
            JsonObject courseObject = (JsonObject)jsonObject.get("course"); 
            JsonArray sectionArray = (JsonArray) jsonObject.get("section"); 
            
            List<String[]>sectionList = new ArrayList<>();
            String[] header = {CRN_COL_HEADER, SUBJECT_COL_HEADER, NUM_COL_HEADER, DESCRIPTION_COL_HEADER, SECTION_COL_HEADER, TYPE_COL_HEADER, CREDITS_COL_HEADER,
                START_COL_HEADER, END_COL_HEADER, DAYS_COL_HEADER, WHERE_COL_HEADER, SCHEDULE_COL_HEADER, INSTRUCTOR_COL_HEADER};
            sectionList.add(header);

            try {
                // Iterate over each section in the sectionArray
                for (Object sectionElement: sectionArray)    {
                    JsonObject sectionObject = (JsonObject)sectionElement; 

                    // Extract data for each section
                    String crn = sectionObject.get(CRN_COL_HEADER).toString(); 
                    String subject = subjectObject.get(sectionObject.get(SUBJECTID_COL_HEADER)).toString();
                    String courseNum = (sectionObject.get(SUBJECTID_COL_HEADER) + " " + sectionObject.get(NUM_COL_HEADER));

                    // Extract course related data
                    HashMap courseNames = (HashMap)courseObject.get(courseNum);
                    String description = courseNames.get(DESCRIPTION_COL_HEADER).toString();
                    String section = sectionObject.get(SECTION_COL_HEADER).toString();
                    String type = sectionObject.get(TYPE_COL_HEADER).toString();
                    String credits = courseNames.get(CREDITS_COL_HEADER).toString();
                    String start = sectionObject.get(START_COL_HEADER).toString();
                    String end = sectionObject.get(END_COL_HEADER).toString();
                    String days = sectionObject.get(DAYS_COL_HEADER).toString();
                    String where = sectionObject.get(WHERE_COL_HEADER).toString();
                    String schedule = scheduleObject.get(type).toString();

                    // Extract and format instructor data
                    JsonArray instructorArray =(JsonArray) sectionObject.get(INSTRUCTOR_COL_HEADER);
                    String[]instructorNames = instructorArray.toArray(new String[0]);
                    String instructor = String.join(", ", instructorNames);

                    // Create a CSV line and add it to the sectionList
                    String[] csvLine = {crn, subject, courseNum, description, section, type, credits, start, end, days, where, schedule, instructor};
                    sectionList.add(csvLine);
                    }
            try (CSVWriter csvWriter = new CSVWriter(sWriter, '\t', '"', '\\', "\n")){
                csvWriter.writeAll(sectionList);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        // Returning the final output  
        return sWriter.toString();      
    }   
    public JsonObject getJson() {
        
        JsonObject json = getJson(getInputFileData(JSON_FILENAME));
        return json;
        
    }
    
    public JsonObject getJson(String input) {
        
        JsonObject json = null;
        
        try {
            json = (JsonObject)Jsoner.deserialize(input);
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return json;
        
    }
    
    public List<String[]> getCsv() {
        
        List<String[]> csv = getCsv(getInputFileData(CSV_FILENAME));
        return csv;
        
    }
    
    public List<String[]> getCsv(String input) {
        
        List<String[]> csv = null;
        
        try {
            
            CSVReader reader = new CSVReaderBuilder(new StringReader(input)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
            csv = reader.readAll();
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return csv;
        
    }
    
    public String getCsvString(List<String[]> csv) {
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        csvWriter.writeAll(csv);
        
        return writer.toString();
        
    }
    
    private String getInputFileData(String filename) {
        
        StringBuilder buffer = new StringBuilder();
        String line;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));

            while((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return buffer.toString();
        
    }
    
}
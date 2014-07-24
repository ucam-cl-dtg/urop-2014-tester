package database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import reportelements.AbstractReport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUser {
    /*@JsonProperty("_id") */ private String crsId;
    private Map<String, DBTick> ticks = new HashMap<>();

    @JsonCreator
    public DBUser(@JsonProperty("_id") String crsId/*, @JsonProperty("Ticks") Map<String, DBTick> ticks*/) {
        this.crsId = crsId;
        /*this.ticks = ticks;*/
    }

    public void addReport(String tickId, AbstractReport newReport) {
        //if there is no tick object with the id of tickId, create one
        if (!(ticks.containsKey(tickId))) {
            ticks.put(tickId, new DBTick());
        }

        //add the new report
        ticks.get(tickId).addReport(newReport);
        
        System.out.println("Report added? " + ticks.containsKey(tickId));
    }

    @JsonIgnore
    public AbstractReport getLastReport(String tickId) {
        //TODO: ensure a tick with tickId is in the database
        System.out.println("Report found? " + ticks.containsKey(tickId));
        
        return ticks.get(tickId).getLast();
    }

    @JsonIgnore
    public List<AbstractReport> getAllReports(String tickId) {
        return ticks.get(tickId).getAll();
    }

    @JsonProperty("Ticks")
    public Map<String, DBTick> getTicks() {
        return ticks;
    }

    @JsonProperty("_id")
    public String getCrsId() {
        return crsId;
    }

    @JsonProperty("_id")
    public void setCrsId(String crsId) {
        this.crsId = crsId;
    }

    @JsonProperty("Ticks")
    public void setTicks(Map<String, DBTick> ticks) {
        this.ticks = ticks;
    }
}

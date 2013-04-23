package se.sdmapeg.common;

import java.util.Date;

/**
 * A class for handling timestamps. 
 * 
 * Whenever instantiated, this class saves the time of instantiation and 
 * calculates the prolapsed time since it's creation. 
 * @author Trivoc
 *
 */
public class TimeFormatter {

    long timeOfCreation;
    Date date;
    
    /**
     * Creates a timeformatter with the current time as a startingtime. 
     */
    public TimeFormatter(){
	date = new Date();
	timeOfCreation = date.getTime();
    }
    
    /**
     * Creates a timeformatter with the specified start time. 
     * @param startTime
     */
    public TimeFormatter(long startTime){
	date = new Date();
	timeOfCreation = startTime;
    }
    
    /**
     * Returns the elapsed hours since creation. 
     * @return elapsed minutes. 
     */
    public long getHours(){
	return (date.getTime() - timeOfCreation) / 3600000;
    }
    
    /**
     * Returns the elapsed minutes since creation. 
     * @return elapsed minutes. 
     */
    public long getMinutes(){
	return (date.getTime() - timeOfCreation) / 60000;
    }
    
    /**
     * Returns the elapsed seconds since creation. 
     * @return elapsed seconds.
     */
    public long getSeconds(){
	return (date.getTime() - timeOfCreation) / 1000;
    }

	/**
	 * Returns the elapsed hours since creation as a formatted string.
	 * @return elapsed hours.
	 */
	public String getFormattedHours() {
		return addLeadingZeros(Long.toString(getHours()), 2);
	}

	/**
	 * Returns the elapsed minutes since creation as a formatted string.
	 * @return elapsed minutes.
	 */
	public String getFormattedMinutes() {
		return addLeadingZeros(Long.toString(getMinutes()), 2);
	}

	/**
	 * Returns the elapsed seconds since creation as a formatted string.
	 * @return elapsed seconds.
	 */
	public String getFormattedSeconds() {
		return addLeadingZeros(Long.toString(getSeconds()), 2);
	}

	private String addLeadingZeros(String string, int length) {
		return string.length() == length ? string : addLeadingZeros(0 + string, length);
	}
}


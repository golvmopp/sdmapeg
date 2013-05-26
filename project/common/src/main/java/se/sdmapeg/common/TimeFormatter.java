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
public final class TimeFormatter {

    private final long timeOfCreation;
    
    /**
     * Creates a timeformatter with the current time as a startingtime. 
     */
    public TimeFormatter(){
	timeOfCreation = System.currentTimeMillis();
    }
    
    /**
     * Creates a timeformatter with the specified start time. 
     * @param startTime
     */
    public TimeFormatter(long startTime){
	timeOfCreation = startTime;
    }
    
    /**
     * Returns the elapsed hours since creation. 
     * @return elapsed minutes. 
     */
    public long getHours(){
	return (System.currentTimeMillis() - timeOfCreation) / 3600000;
    }
    
    /**
     * Returns the elapsed minutes since creation. 
     * @return elapsed minutes. 
     */
    public long getMinutes(){
	return (System.currentTimeMillis() - timeOfCreation) / 60000 % 60;
    }
    
    /**
     * Returns the elapsed seconds since creation. 
     * @return elapsed seconds.
     */
    public long getSeconds(){
	return (System.currentTimeMillis() - timeOfCreation) / 1000  % 60;
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

	public static String addLeadingZeros(String string, int length) {
		return string.length() < length ? addLeadingZeros(0 + string, length) : string;
	}
}


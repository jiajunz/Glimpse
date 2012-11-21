package edu.cmu.glimpse.modules;

import java.util.Calendar;
import java.util.List;

import edu.cmu.glimpse.entry.GlimpseEntryPreview;

public interface IEntryManageModule {

    /**
     * Get all entries in a specific date
     * 
     * @param date
     *            date in {@link java.util.Calendar} format
     * @return
     *         all entries in this date
     */
    List<GlimpseEntryPreview> getEntryPreviews(Calendar date);

    /**
     * Get content for a specific entry
     * 
     * @param id
     *            id of the entry
     * @return
     *         whole content as a string
     */
    String getContent(int id);

    /**
     * Create a new entry with given date and content
     * 
     * @param year
     *            year of the entry
     * @param month
     *            month of the entry
     * @param dayOfMonth
     *            dayOfMonth of the entry
     * @param content
     *            content of the entry
     * @return
     *         <tt>true</tt> if success, <tt>false</tt> otherwise
     */
    boolean createEntry(int year, int month, int dayOfMonth, String content);

    /**
     * Save an entry with given id and content
     * 
     * @param id
     *            id of the entry
     * @param content
     *            content of the entry
     * @return
     *         <tt>true</tt> if success, <tt>false</tt> otherwise
     */
    boolean saveEntry(int id, String content);

    /**
     * Remove a specific entry by its title and time
     * 
     * @param id
     *            id of the entry
     * @return
     *         <tt>true</tt> if success, <tt>false</tt> otherwise
     */
    boolean removeEntry(int id);
}

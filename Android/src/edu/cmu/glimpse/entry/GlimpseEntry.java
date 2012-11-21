package edu.cmu.glimpse.entry;

import java.util.Calendar;

/**
 * Class represents an entry
 * 
 * @author hanqingl
 * 
 */
public class GlimpseEntry {

    private final int mId;
    private final Calendar mCreate;
    private final Calendar mLastEdit;
    private final String mContent;
    private final GlimpseEntryPreview mGlimpseEntryPreview;

    /**
     * Initiate a new entry with create time, last edit time and content
     * 
     * @param id
     *            id of the entry
     * @param create
     *            create time of the entry
     * @param lastEdit
     *            last edit time of the entry
     * @param content
     *            content of the entry
     */
    public GlimpseEntry(int id, Calendar create, Calendar lastEdit, String content) {
        mId = id;
        mCreate = create;
        mLastEdit = lastEdit;
        mContent = content;
        mGlimpseEntryPreview = new GlimpseEntryPreview(id, content);
    }

    public Calendar getCreatedTime() {
        return mCreate;
    }

    public Calendar getLastEditTime() {
        return mLastEdit;
    }

    public String getContent() {
        return mContent;
    }
}

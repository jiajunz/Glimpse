package edu.cmu.glimpse.modules;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.cmu.glimpse.entry.GlimpseEntryPreview;

public class EntryManageModule implements IEntryManageModule {
    private static EntryManageModule instance;

    private EntryManageModule() {
        super();
    }

    public static EntryManageModule getInstance() {
        if (instance == null) {
            instance = new EntryManageModule();
        }
        return instance;
    }

    public List<GlimpseEntryPreview> getEntryPreviews(Calendar date) {
        List<GlimpseEntryPreview> entries = new ArrayList<GlimpseEntryPreview>();

        return entries;
    }

    public String getContent(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean createEntry(int year, int month, int dayOfMonth, String content) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        return false;
    }

    public boolean saveEntry(int id, String content) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean removeEntry(int id) {
        // TODO Auto-generated method stub
        return false;
    }

}

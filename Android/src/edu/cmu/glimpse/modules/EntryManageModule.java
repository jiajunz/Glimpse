package edu.cmu.glimpse.modules;

import java.util.List;

public interface EntryManageModule {

    List<String> getTitles(int year, int month, int dayOfMonth);

    String getContent(String title, int year, int month, int dayOfMonth);

    boolean createEntry(String title, int year, int month, int dayOfMonth);

    boolean removeEntry(String title, int year, int month, int dayOfMonth);
}

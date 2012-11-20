package edu.cmu.glimpse.guice;

import com.google.inject.AbstractModule;

import edu.cmu.glimpse.modules.EntryManageModule;

public class GlimpseModule extends AbstractModule {

    /**
     * Bind a type to its implementation
     */
    @Override
    protected void configure() {
        bind(EntryManageModule.class).to(EntryManageModule.class);
    }

}

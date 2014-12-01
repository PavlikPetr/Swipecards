package com.topface.topface.data.experiments;

public class MessagesWithTabs extends BaseExperiment {
    @Override
    protected String getOptionsKey() {
        return "messagesWithTabs";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

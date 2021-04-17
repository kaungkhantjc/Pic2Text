package com.jcoder.picsms.async;

import com.jcoder.picsms.models.Message;
import com.jcoder.picsms.utils.Text2CodeListUtil;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class AutoSelectSmsTask implements Callable<AutoSelectSmsTask.Result> {

    public static boolean shouldBreakLoop;
    private final int messageType;
    private final int rangeOfAutoSelect;
    private final ArrayList<Message> messages;

    public AutoSelectSmsTask(int messageType, int rangeOfAutoSelect, ArrayList<Message> messages) {
        this.messageType = messageType;
        this.rangeOfAutoSelect = rangeOfAutoSelect;
        this.messages = messages;
    }

    public static class Result {
        private final ArrayList<Message> autoSelectedMessages;
        private final ArrayList<Integer> selectedMessagePositions;

        public Result(ArrayList<Message> autoSelectedMessages, ArrayList<Integer> selectedMessagePositions) {
            this.autoSelectedMessages = autoSelectedMessages;
            this.selectedMessagePositions = selectedMessagePositions;
        }

        public ArrayList<Message> getAutoSelectedMessages() {
            return autoSelectedMessages;
        }

        public ArrayList<Integer> getSelectedMessagePositions() {
            return selectedMessagePositions;
        }
    }

    @Override
    public Result call() {
        final ArrayList<Message> autoSelectedMessages = new ArrayList<>();
        final ArrayList<Integer> selectedMessagePositions = new ArrayList<>();

        final long RANGE = rangeOfAutoSelect * 60 * 1000;

        long lastSelectedMessageDate = 0;

        for (int i = 0; i < messages.size(); i++) {
            if (shouldBreakLoop) {
                shouldBreakLoop = false;
                break;
            }

            Message message = messages.get(i);

            if (lastSelectedMessageDate == 0 || (lastSelectedMessageDate - message.getDate() <= RANGE)) {
                if (message.getType() == messageType) {
                    boolean hasCodeParts = Text2CodeListUtil.text2CodeParts(message.getBody(), true).size() > 0;
                    if (hasCodeParts) {
                        lastSelectedMessageDate = message.getDate();
                        autoSelectedMessages.add(message);
                        selectedMessagePositions.add(i);
                    }
                }
            } else break;
        }

        return new Result(autoSelectedMessages, selectedMessagePositions);
    }
}

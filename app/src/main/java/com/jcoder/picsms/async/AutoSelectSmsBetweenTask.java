package com.jcoder.picsms.async;

import com.jcoder.picsms.models.Message;
import com.jcoder.picsms.utils.Text2CodeListUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Callable;

public class AutoSelectSmsBetweenTask implements Callable<AutoSelectSmsBetweenTask.Result> {
    private final int messageType;
    private final long startDate, endDate;
    private final ArrayList<Message> messages;

    public AutoSelectSmsBetweenTask(int messageType, long startDate, long endDate, ArrayList<Message> messages) {
        this.messageType = messageType;
        this.startDate = startDate;
        this.endDate = endDate;
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

        final Calendar endDateCalendar = Calendar.getInstance();
        endDateCalendar.setTimeInMillis(endDate);
        endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endDateCalendar.set(Calendar.MINUTE, 59);
        endDateCalendar.set(Calendar.SECOND, 59);
        endDateCalendar.set(Calendar.MILLISECOND, 999);

        final long finalEndDate = endDateCalendar.getTimeInMillis();

        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);

            if (message.getType() == messageType) {
                long messageDate = message.getDate();
                if (messageDate > finalEndDate) continue; // skip
                if (messageDate < startDate) break; // stop loop

                boolean hasCodeParts = Text2CodeListUtil.text2CodeParts(message.getBody(), true).size() > 0;
                if (hasCodeParts) {
                    autoSelectedMessages.add(message);
                    selectedMessagePositions.add(i);
                }
            }
        }

        return new Result(autoSelectedMessages, selectedMessagePositions);
    }

}

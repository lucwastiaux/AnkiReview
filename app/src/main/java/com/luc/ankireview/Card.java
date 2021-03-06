package com.luc.ankireview;

import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Card {

    public static Pattern s_soundPattern = Pattern.compile("\\[sound\\:([^\\[\\]]*)\\]");
    private static final String TAG = "Card";


    public Card(long noteId, int cardOrd, long modelId, String cardTemplateName, HashMap<String,String> fieldMap, HashSet<String> tagSet, String questionContent, String answerContent) {
        m_noteId = noteId;
        m_cardOrd = cardOrd;
        m_modelId = modelId;
        m_cardTemplateName = cardTemplateName;
        m_fieldMap = fieldMap;
        m_tags = tagSet;

        m_buttonCount = 0;
        m_nextReviewTimes = null;

        m_questionContent = questionContent;
        m_answerContent = answerContent;
    }

    public void setReviewData(int buttonCount, Vector<String> nextReviewTimes) {
        m_buttonCount = buttonCount;
        m_nextReviewTimes = nextReviewTimes;
    }

    public String extractSoundFile( String content ) {
        String result = "";

        Matcher matcher = s_soundPattern.matcher(content);
        // While there is matches of the pattern for sound markers
        while (matcher.find()) {
            String sound = matcher.group(1);
            Log.v(TAG, "sound: " + sound);
            result = sound;
        }

        return result;
    }

    public static String filterSound( String content ) {
        Matcher matcher = s_soundPattern.matcher(content);
        // While there is matches of the pattern for sound markers
        while (matcher.find()) {
            String sound = matcher.group(1);
            Log.v(TAG, "sound: " + sound);
        }

        content = matcher.replaceAll("");

        return content;
    }

    public int getButtonCount()
    {
        return m_buttonCount;
    }

    public long getNoteId() {
        return m_noteId;
    }

    public long getModelId() { return m_modelId; }

    public int getCardOrd() {
        return m_cardOrd;
    }

    public String getCardTemplateName() { return m_cardTemplateName; }

    public String getQuestionContent() { return m_questionContent; }

    public String getAnswerContent() { return m_answerContent; }

    public String getFieldValue(String fieldName) { return m_fieldMap.get(fieldName); }

    public HashMap<String,String> getFieldMap() { return m_fieldMap; }

    public HashSet<String> getTagMap() { return m_tags; }

    public AnkiUtils.Ease getEaseBad() {
        return AnkiUtils.Ease.EASE_1;
    }

    public AnkiUtils.Ease getEaseGood() {
        switch (m_buttonCount) {
            case 2:
                return AnkiUtils.Ease.EASE_2;
            case 3:
                return AnkiUtils.Ease.EASE_2;
            case 4:
                return AnkiUtils.Ease.EASE_3;
            default:
                return AnkiUtils.Ease.EASE_1;
        }
    }

    public String getBadAnswerInterval() {
        return m_nextReviewTimes.get(0);
    }

    public String getGoodAnswerInterval() {
        switch( m_buttonCount ) {
            case 2:
                return m_nextReviewTimes.get(1);
            case 3:
                return m_nextReviewTimes.get(1);
            default:
                return m_nextReviewTimes.get(2);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return m_noteId == card.m_noteId &&
                m_cardOrd == card.m_cardOrd;
    }

    @Override
    public int hashCode() {

        return Objects.hash(m_noteId, m_cardOrd);
    }

    public Vector<String> getFullFieldList() {
        Vector<String> fieldList = new Vector<String>();
        for(String field: getFieldMap().keySet())
        {
            fieldList.add(field);
        }
        return fieldList;
    }

    public Vector<String> getNextReviewTimes() {
        return m_nextReviewTimes;
    }

    private long m_noteId;
    private int m_cardOrd;
    private long m_modelId;
    private String m_cardTemplateName;

    HashMap<String,String> m_fieldMap;
    HashSet<String> m_tags;

    private int m_buttonCount;
    private Vector<String> m_nextReviewTimes;

    private String m_questionContent;
    private String m_answerContent;
}

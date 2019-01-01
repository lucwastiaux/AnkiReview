package com.luc.ankireview.style;

public class FieldListItem {
    public static final String HEADER_ALLFIELDS = "All Fields";
    public static final String HEADER_QUESTION = "Question Fields";
    public static final String HEADER_ANSWER = "Answer Fields";
    public static final String HEADER_SOUND = "Sound Fields";

    public static final int VIEWTYPE_FIELD = 1;
    public static final int VIEWTYPE_HEADER = 2;

    public FieldListItem(CardField cardField, int viewType, String header) {
        m_cardField = cardField;
        m_viewType = viewType;
        m_header = header;
    }

    public int getViewType() {
        return m_viewType;
    }

    public CardField getCardField() {
        return m_cardField;
    }

    public String getHeader() {
        return m_header;
    }

    private CardField m_cardField;
    private int m_viewType;
    private String m_header;
}

package com.luc.ankireview.style;

import android.graphics.Typeface;

import com.luc.ankireview.style.CardField;

import java.io.Serializable;
import java.util.Vector;

public class CardTemplate implements Serializable {
    public static final long serialVersionUID = 2L; // increment this in case of schema changes

    public CardTemplate() {
        m_questionCardFields = new Vector<CardField>();
        m_answerCardFields = new Vector<CardField>();

        m_questionSoundField = null;
        m_answerSoundField = null;

        // setup default values
        setDefaultValues();

    }

    public void setDefaultValues() {
        setDefaultFontValues();
        setDefaultSpacingValues();
    }

    public void setDefaultFontValues() {
        m_baseTextSize = 40;
        m_font = "Roboto";
    }

    public void setDefaultSpacingValues() {
        m_width = 90;
        m_centerMargin_px = 20;

        m_paddingTop = 20;
        m_paddingBottom = 30;
        m_paddingLeftRight = 15;
    }

    public void setFont(String font) {
        m_font = font;
    }

    public String getFont() {
        return m_font;
    }

    public void addQuestionCardField(CardField cardField) {
        m_questionCardFields.add(cardField);
    }

    public void addAnswerCardField(CardField cardField) {
        m_answerCardFields.add(cardField);
    }


    public Vector<CardField> getQuestionCardFields() {
        return m_questionCardFields;
    }

    public Vector<CardField> getAnswerCardFields() {
        return m_answerCardFields;
    }


    public String getQuestionSoundField() { return m_questionSoundField; }
    public void setQuestionSoundField(String  soundField) { m_questionSoundField = soundField; }

    public String getAnswerSoundField() { return m_answerSoundField; }
    public void setAnswerSoundField(String  soundField) { m_answerSoundField = soundField; }

    public int getBaseTextSize() { return m_baseTextSize; }
    public void setBaseTextSize(int margin) { m_baseTextSize = margin; }

    public int getWidth() { return m_width; }
    public void setWidth(int width) { m_width = width; }

    public int getCenterMargin() { return m_centerMargin_px; }
    public void setCenterMargin( int margin ) { m_centerMargin_px = margin; }

    public int getPaddingTop() { return m_paddingTop; }
    public void setPaddingTop( int padding) { m_paddingTop = padding; }

    public int getPaddingBottom() { return m_paddingBottom; }
    public void setPaddingBottom( int padding )  { m_paddingBottom = padding; }

    public int getPaddingLeftRight() { return m_paddingLeftRight; }
    public void setPaddingLeftRight( int padding ) { m_paddingLeftRight = padding; }


    private String m_font;

    private Vector<CardField> m_questionCardFields;
    private Vector<CardField> m_answerCardFields;

    private String m_questionSoundField;
    private String m_answerSoundField;

    // text
    private int m_baseTextSize;

    // margins
    private int m_width;
    private int m_centerMargin_px;

    private int m_paddingTop;
    private int m_paddingBottom;
    private int m_paddingLeftRight;


}

package com.luc.ankireview;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.luc.ankireview.style.CardField;
import com.luc.ankireview.style.CardStyle;
import com.luc.ankireview.style.CardTemplate;
import com.luc.ankireview.style.CardTemplateKey;
import com.luc.ankireview.style.FieldListAdapter;
import com.luc.ankireview.style.ItemTouchCallback;
import com.luc.ankireview.style.ValueSlider;
import com.luc.ankireview.style.ValueSliderUpdate;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.SimpleTarget;
import com.thebluealliance.spectrum.SpectrumDialog;
import com.thebluealliance.spectrum.SpectrumPalette;

import java.util.Vector;

public class CardStyleActivity extends AppCompatActivity {
    private static final String TAG = "CardStyleActivity";
    public static final double TEXT_RELATIVE_SIZE_FACTOR = 10.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardstyle);


        m_cardStyle = new CardStyle(this);

        Intent intent = getIntent();
        long noteId = intent.getLongExtra("noteId", 0l);
        int cardOrd = intent.getIntExtra("cardOrd", 0);
        String deckName = intent.getStringExtra("deckName");
        String cardTemplateName = intent.getStringExtra("cardTemplateName");

        Toolbar toolbar = (Toolbar) findViewById(R.id.cardstyle_toolbar);
        toolbar.setTitle("Card Style: " + deckName);
        toolbar.setSubtitle("Card Template: " + cardTemplateName);
        setSupportActionBar(toolbar);



        Log.v(TAG, "starting CardStyleActivity with noteId: " + noteId + " cardOrd: " + cardOrd);

        m_firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        m_firebaseAnalytics.logEvent(Analytics.CARDSTYLE_START, null);

        // retrieve the appropriate card
        m_card = AnkiUtils.retrieveCard(getContentResolver(), noteId, cardOrd);

        // retrieve the card template
        boolean firstTimeSetup = false;
        m_cardTemplateKey = new CardTemplateKey(m_card.getModelId(), m_card.getCardOrd());
        m_cardTemplate = m_cardStyle.getCardTemplate(m_cardTemplateKey);
        if(m_cardTemplate == null) {
            // create a new one
            m_cardTemplate = m_cardStyle.createCardTemplate(m_cardTemplateKey);
            // showDefineStyleDialog();
            firstTimeSetup = true;
        }

        // Log.v(TAG, "num question card fields: " + m_cardTemplate.getQuestionCardFields().size());

        m_bottomNavigation = findViewById(R.id.cardstyle_bottom_navigation);
        m_bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.cardstyle_action_fields:
                        showFieldListView();
                        break;
                    case R.id.cardstyle_action_field_settings:
                        showFieldSettingsView();
                        break;
                    case R.id.cardstyle_action_font:
                        showFontView();
                        break;
                    case R.id.cardstyle_action_sound:
                        showSoundView();
                        break;
                    case R.id.cardstyle_action_spacing:
                        showSpacingView();
                        break;
                }
                return true;
            }
        });



        m_cardstyleEditorCards = findViewById(R.id.cardstyle_editor_cards);

        // card views
        m_questionCardView = findViewById(R.id.question_card);
        m_answerCardView = findViewById(R.id.answer_card);

        // card textviews
        m_questionTextView = m_questionCardView.findViewById(R.id.side_text);
        m_answerTextView = m_answerCardView.findViewById(R.id.side_text);

        m_cardStyle.renderBothCards(m_card, m_questionCardView, m_answerCardView, m_questionTextView, m_answerTextView);

        // get font view and margins view
        m_fieldSettingsView = findViewById(R.id.cardstyle_editor_fieldsettings);
        m_fontView = findViewById(R.id.cardstyle_editor_font);
        m_soundView = findViewById(R.id.cardstyle_editor_sound);
        m_marginsView = findViewById(R.id.cardstyle_editor_margins);

        // setup the full Field list ListView
        m_fullFieldListView = findViewById(R.id.cardstyle_editor_all_fields);
        m_fullFieldListView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        m_fullFieldListView.setLayoutManager(linearLayoutManager);

        // get field list
        Vector<String> fullFieldList = new Vector<String>();
        for (String field : m_card.getFieldMap().keySet())
        {
            fullFieldList.add(field);
        }

        m_fieldListAdapter = new FieldListAdapter(this, m_cardTemplate, fullFieldList);
        m_fullFieldListView.setAdapter(m_fieldListAdapter);

        ItemTouchCallback itemTouchCallback = new ItemTouchCallback(m_fieldListAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchCallback);
        m_fieldListAdapter.setTouchHelper(touchHelper);
        touchHelper.attachToRecyclerView(m_fullFieldListView);

        // set visibility of tabs
        m_fullFieldListView.setVisibility(View.VISIBLE);
        m_fieldSettingsView.setVisibility(View.INVISIBLE);
        m_fontView.setVisibility(View.INVISIBLE);
        m_soundView.setVisibility(View.INVISIBLE);
        m_marginsView.setVisibility(View.INVISIBLE);

        // field settings controls
        // =======================

        // field selector
        m_fieldSpinner = findViewById(R.id.cardstyle_field_settings_selector);
        m_fieldSpinnerAdapter = new ArrayAdapter<CardField>(this, R.layout.simple_spinner_item);
        m_fieldSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_fieldSpinner.setAdapter(m_fieldSpinnerAdapter);
        m_fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CardField cardField = (CardField) adapterView.getItemAtPosition(i);
                openFieldSettings(cardField);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        m_fieldSettingsPanel = findViewById(R.id.cardstyle_field_settings_panel);
        m_fieldSettingsPanel.setVisibility(View.INVISIBLE);

        // relative size
        m_field_relativesize = findViewById(R.id.cardstyle_text_relativesize_valueslider);
        m_field_relativesize.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_currentCardField.setRelativeSize((float) (currentValue / TEXT_RELATIVE_SIZE_FACTOR));
                updateCardPreview();
            }
        });

        // line return
        m_lineReturnCheckBox = findViewById(R.id.cardstyle_field_linereturn);
        m_lineReturnCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                m_currentCardField.setLineReturn(b);
                updateCardPreview();
            }
        });

        // html
        m_htmlCheckBox = findViewById(R.id.cardstyle_field_html);
        m_htmlCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                m_currentCardField.setIsHtml(b);
                updateCardPreview();
            }
        });

        // alignment
        m_fieldAlignmentSpinner = findViewById(R.id.cardstyle_field_alignment);
        m_fieldAlignmentAdapter = ArrayAdapter.createFromResource(this,
                R.array.alignment_array,  R.layout.simple_spinner_item);
        m_fieldAlignmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_fieldAlignmentSpinner.setAdapter(m_fieldAlignmentAdapter);
        m_fieldAlignmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(m_currentCardField == null)
                    return;

                switch(i) {
                    case 0:
                        m_currentCardField.setAlignment(Layout.Alignment.ALIGN_CENTER);
                        break;
                    case 1:
                        m_currentCardField.setAlignment(Layout.Alignment.ALIGN_NORMAL);
                        break;
                    case 2:
                        m_currentCardField.setAlignment(Layout.Alignment.ALIGN_OPPOSITE);
                        break;
                    default:
                        break;
                }
                updateCardPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // left margin
        m_field_leftmargin = findViewById(R.id.cardstyle_field_leftmargin);
        m_field_leftmargin.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_currentCardField.setLeftMargin(currentValue);
                updateCardPreview();
            }
        });

                // color
        m_field_colorSelector = findViewById(R.id.cardstyle_field_color_selector);
        m_fieldColorCircle = findViewById(R.id.cardstyle_field_color_circle);

        // reset button
        Button fieldSettingsReset = findViewById(R.id.cardstyle_editor_fieldsettings_reset);
        fieldSettingsReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "resetting field settings to default");
                m_currentCardField.setDefaultValues(defaultFieldColor());
                updateFieldSettingsControls();
                updateCardPreview();
            }
        });


        // text controls
        // -------------

        m_text_basesize = findViewById(R.id.cardstyle_text_basesize_valueslider);
        m_text_basesize.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setBaseTextSize(currentValue);
                updateCardPreview();
            }
        });

        m_text_font_family = findViewById(R.id.cardstyle_editor_font_family);
        m_text_font_lookup = findViewById(R.id.cardstyle_editor_font_lookup);
        m_text_font_lookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fontFamily = m_text_font_family.getText().toString();
                m_cardTemplate.setFont(fontFamily);
                updateCardPreview();

                hideSoftKeyboard();

            }
        });

        updateFontControls();

        Button resetFontSettings = findViewById(R.id.cardstyle_editor_font_reset);
        resetFontSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_cardTemplate.setDefaultFontValues();
                updateFontControls();
                updateCardPreview();
            }
        });


        // sound controls
        // --------------

        Spinner questionSoundSpinner = findViewById(R.id.cardstyle_soundfield_question);
        Spinner answerSoundSpinner = findViewById(R.id.cardstyle_soundfield_answer);

        ArrayAdapter<String> soundFieldsAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item);
        soundFieldsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questionSoundSpinner.setAdapter(soundFieldsAdapter);
        answerSoundSpinner.setAdapter(soundFieldsAdapter);

        questionSoundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0 ) {
                    m_cardTemplate.setQuestionSoundField(null);
                    return;
                }
                String field = (String) adapterView.getItemAtPosition(i);
                Log.v(TAG, "chosen question sound field: " + field);
                m_cardTemplate.setQuestionSoundField(field);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        answerSoundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0 ) {
                    m_cardTemplate.setAnswerSoundField(null);
                    return;
                }
                String field = (String) adapterView.getItemAtPosition(i);
                Log.v(TAG, "chosen answer sound field: " + field);
                m_cardTemplate.setAnswerSoundField(field);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // populate the adapter
        soundFieldsAdapter.add("<None>");
        int i = 1;
        for (String field : m_card.getFieldMap().keySet())
        {
            soundFieldsAdapter.add(field);
            if(field.equals(m_cardTemplate.getQuestionSoundField())) {
                questionSoundSpinner.setSelection(i);
            }
            if(field.equals(m_cardTemplate.getAnswerSoundField())) {
                answerSoundSpinner.setSelection(i);
            }
            i++;
        }

        // margin controls
        // ---------------

        m_valueSliderWidth = findViewById(R.id.cardstyle_width_valueslider);
        m_valueSliderWidth.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setWidth(currentValue);
                updateCardPreview();
            }
        });

        m_valueSliderBetween = findViewById(R.id.cardstyle_margin_between_valueslider);
        m_valueSliderBetween.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setCenterMargin(currentValue);
                updateCardPreview();
            }
        });


        m_valueSliderPaddingTop = findViewById(R.id.cardstyle_padding_top_valueslider);
        m_valueSliderPaddingTop.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setPaddingTop(currentValue);
                updateCardPreview();
            }
        });


        m_valueSliderPaddingBottom = findViewById(R.id.cardstyle_padding_bottom_valueslider);
        m_valueSliderPaddingBottom.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setPaddingBottom(currentValue);
                updateCardPreview();
            }
        });

        m_valueSliderPaddingLeftRight = findViewById(R.id.cardstyle_padding_leftright_valueslider);
        m_valueSliderPaddingLeftRight.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setPaddingLeftRight(currentValue);
                updateCardPreview();
            }
        });
        updateSpacingControls();

        Button resetSpacingButton = findViewById(R.id.cardstyle_editor_spacing_reset);
        resetSpacingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_cardTemplate.setDefaultSpacingValues();
                updateSpacingControls();
                updateCardPreview();
            }
        });

        // start spotlight
        // ===============

        if( firstTimeSetup ) {
            m_fullFieldListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    m_fullFieldListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    showSpotlightGuide();
                }
            });
        }

    }


    private void showSpotlightGuide() {

        SimpleTarget target1;
        SimpleTarget target2;
        SimpleTarget target3;
        SimpleTarget target4;

        // tell user where the card style preview will appear
        {
            int[] location = new int[2];
            View centeredView = m_cardstyleEditorCards;
            centeredView.getLocationOnScreen(location);

            float targetSize = 400f;
            float targetX = (float) (location[0] + m_cardstyleEditorCards.getWidth() / 2.0);
            float targetY = (float) (location[1] + m_cardstyleEditorCards.getHeight() / 2.0);

            float overlayX = 10f;
            float overlayY = targetY + targetSize + 100f;

            target1 = new SimpleTarget.Builder(this)
                    .setPoint(targetX, targetY)
                    .setShape(new Circle(targetSize)) // or RoundedRectangle()
                    .setTitle("Card Preview")
                    .setDescription("In this screen, you can design the appearance of your cards (Question and Answer). As you configure your card's style, a preview will appear at the top.")
                    .setOverlayPoint(overlayX, overlayY)
                    .build();

        }

        // instruct user to drag the field handle
        {
            float targetX;
            float targetY;

            float targetSize = 400f;

            float overlayX;
            float overlayY;

            int[] location = new int[2];
            LinearLayout fieldEntry = (LinearLayout) m_fullFieldListView.getChildAt(m_fieldListAdapter.getFirstUnassignedFieldId());

            if(fieldEntry != null) {
                View dragHandle = fieldEntry.getChildAt(1);
                dragHandle.getLocationOnScreen(location);

                targetX = (float) (location[0] + dragHandle.getWidth() / 2.0);
                targetY = (float) (location[1] - dragHandle.getHeight() / 2.0);

                overlayX = 10f;
                overlayY = targetY - 800f;
            } else {
                m_fieldSettingsView.getLocationOnScreen(location);

                targetX = (float) (location[0] + m_fieldSettingsView.getWidth() / 2.0);
                targetY = (float) (location[1] + m_fieldSettingsView.getHeight() / 2.0);

                overlayX = 10f;
                overlayY = targetY - 800f;
            }



            target2 = new SimpleTarget.Builder(this)
                    .setPoint(targetX, targetY)
                    .setShape(new Circle(targetSize)) // or RoundedRectangle()
                    .setTitle("Field List")
                    .setDescription("Here you can configure which fields appear in the Question card or Answer card. Grab this handle to drag the field into the Question or Answer section above. " +
                            "You can also scroll up or down to see the complete field list.")
                    .setOverlayPoint(overlayX, overlayY)
                    .build();
        }

        // color tab
        {
            int[] location = new int[2];

            View fieldLookButton = findViewById(R.id.cardstyle_action_field_settings);
            fieldLookButton.getLocationOnScreen(location);

            float targetSize = 100f;
            float targetX = (float) (location[0] + fieldLookButton.getWidth() / 2.0);
            float targetY = (float) (location[1] + fieldLookButton.getHeight() / 2.0);

            float overlayX = 10f;
            float overlayY = targetY - 600f;

            target3 = new SimpleTarget.Builder(this)
                    .setPoint(targetX, targetY)
                    .setShape(new Circle(targetSize)) // or RoundedRectangle()
                    .setTitle("Appearance Settings")
                    .setDescription("You can configure text color, size and other settings here. There are other settings in tabs to the right, such as Sound, Font, Spacing.")
                    .setOverlayPoint(overlayX, overlayY)
                    .build();
        }

        // instruct user to save
        {
            int[] location = new int[2];

            View saveButton = findViewById(R.id.cardstyle_toolbar);
            saveButton.getLocationOnScreen(location);

            float targetSize = 100f;
            float targetX = (float) (saveButton.getWidth() - 100f);
            float targetY = (float) (location[1] + saveButton.getHeight() / 2.0);

            float overlayX = 10f;
            float overlayY = targetY + targetSize + 100f;

            target4 = new SimpleTarget.Builder(this)
                    .setPoint(targetX, targetY)
                    .setShape(new Circle(targetSize)) // or RoundedRectangle()
                    .setTitle("Save and Exit")
                    .setDescription("When done configuring the card style, save and exit here. You can come back any time with the Card Style menu entry while reviewing.")
                    .setOverlayPoint(overlayX, overlayY)
                    .build();
        }

        Spotlight.with(this)
                .setOverlayColor(R.color.background)
                .setDuration(1000L)
                .setAnimation(new DecelerateInterpolator(2f))
                .setTargets(target1, target2, target3, target4)
                .setClosedOnTouchedOutside(true)
                .start();
    }

    public void hideSoftKeyboard() {
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) this.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                    this.getCurrentFocus().getWindowToken(), 0);
        } catch( Exception e ) {
            Log.e(TAG, "hideSoftKeyboard exception: ", e);
        }
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Save your Card Style")
                .setMessage("Do you want to save your changes to the card style ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveAndExit();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitWithoutSaving();
                    }

                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cardstyle_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cardstyle_save:
                Log.v(TAG, "save card style");
                saveAndExit();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void showFieldListView() {
        m_fullFieldListView.setVisibility(View.VISIBLE);
        m_fieldSettingsView.setVisibility(View.INVISIBLE);
        m_fontView.setVisibility(View.INVISIBLE);
        m_soundView.setVisibility(View.INVISIBLE);
        m_marginsView.setVisibility(View.INVISIBLE);
    }

    private void showFieldSettingsView() {
        m_fullFieldListView.setVisibility(View.INVISIBLE);
        m_fieldSettingsView.setVisibility(View.VISIBLE);
        m_fontView.setVisibility(View.INVISIBLE);
        m_soundView.setVisibility(View.INVISIBLE);
        m_marginsView.setVisibility(View.INVISIBLE);

        setupFieldSettings();
    }

    private void showFontView() {
        m_fullFieldListView.setVisibility(View.INVISIBLE);
        m_fieldSettingsView.setVisibility(View.INVISIBLE);
        m_fontView.setVisibility(View.VISIBLE);
        m_soundView.setVisibility(View.INVISIBLE);
        m_marginsView.setVisibility(View.INVISIBLE);
    }

    private void showSoundView() {
        m_fullFieldListView.setVisibility(View.INVISIBLE);
        m_fieldSettingsView.setVisibility(View.INVISIBLE);
        m_fontView.setVisibility(View.INVISIBLE);
        m_soundView.setVisibility(View.VISIBLE);
        m_marginsView.setVisibility(View.INVISIBLE);
    }

    private void showSpacingView() {
        m_fullFieldListView.setVisibility(View.INVISIBLE);
        m_fieldSettingsView.setVisibility(View.INVISIBLE);
        m_fontView.setVisibility(View.INVISIBLE);
        m_soundView.setVisibility(View.INVISIBLE);
        m_marginsView.setVisibility(View.VISIBLE);
    }

    public void fieldListUpateCardPreview() {
        m_firebaseAnalytics.logEvent(Analytics.CARDSTYLE_FIELD_CHOSEN, null);
        updateCardPreview();
    }

    public void updateCardPreview() {
        m_cardStyle.applyMotionLayoutStyle(m_card, m_cardstyleEditorCards);
        m_cardStyle.renderBothCards(m_card, m_questionCardView, m_answerCardView, m_questionTextView, m_answerTextView);
    }

    public void exitWithoutSaving() {

        m_firebaseAnalytics.logEvent(Analytics.CARDSTYLE_NOSAVE, null);

        setResult(Activity.RESULT_CANCELED, null);
        finish();
    }

    public void saveAndExit() {
        if (m_cardTemplate.getQuestionCardFields().size() == 0 || m_cardTemplate.getAnswerCardFields().size() == 0 ) {
            new AlertDialog.Builder(this)
                    .setTitle("Select Question and Answer fields before saving")
                    .setMessage("You haven't defined Question or Answer fields. Please drag fields from the All Fields section to the Question or Answer section")
                    .setPositiveButton("OK", null)
                    .show();
        } else {

            m_firebaseAnalytics.logEvent(Analytics.CARDSTYLE_SAVE, null);

            m_cardStyle.saveCardStyleData();
            setResult(Activity.RESULT_OK, null);
            finish();
        }
    }

    private void setupFieldSettings() {
        // setup the field dropdown
        m_fieldSpinnerAdapter.clear();

        for(CardField cardField : m_cardTemplate.getQuestionCardFields()) {
            m_fieldSpinnerAdapter.add(cardField);
        }

        for(CardField cardField : m_cardTemplate.getAnswerCardFields()) {
            m_fieldSpinnerAdapter.add(cardField);
        }

    }

    public void openFieldSettings(final CardField cardField) {
        Log.v(TAG, "openFieldSettings " + cardField.getFieldName());

        m_currentCardField = cardField;

        updateFieldSettingsControls();

        final CardStyleActivity context = this;


        m_field_colorSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SpectrumDialog.Builder(context)
                        .setColors(R.array.text_field_colors)
                        .setSelectedColor(cardField.getColor())
                        .setDismissOnColorSelected(true)
                        .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                            @Override public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                                if (positiveResult) {
                                    cardField.setColor(color);
                                    ImageViewCompat.setImageTintList(m_fieldColorCircle, ColorStateList.valueOf(color));
                                    updateCardPreview();
                                }
                            }
                        }).build().show(context.getSupportFragmentManager(), "field_color_picker");
            }
        });

        m_fieldSettingsPanel.setVisibility(View.VISIBLE);
    }

    private void updateFieldSettingsControls() {
        m_field_relativesize.setCurrentValue((int) (m_currentCardField.getRelativeSize() * TEXT_RELATIVE_SIZE_FACTOR));
        m_lineReturnCheckBox.setChecked(m_currentCardField.getLineReturn());
        m_htmlCheckBox.setChecked(m_currentCardField.getIsHtml());
        switch( m_currentCardField.getAlignment()) {
            case ALIGN_CENTER:
                m_fieldAlignmentSpinner.setSelection(0);
                break;
            case ALIGN_NORMAL:
                m_fieldAlignmentSpinner.setSelection(1);
                break;
            case ALIGN_OPPOSITE:
                m_fieldAlignmentSpinner.setSelection(2);
                break;
        }
        m_field_leftmargin.setCurrentValue(m_currentCardField.getLeftMargin());
        ImageViewCompat.setImageTintList(m_fieldColorCircle, ColorStateList.valueOf(m_currentCardField.getColor()));
    }

    private void updateFontControls() {
        m_text_font_family.setText(m_cardTemplate.getFont());
        m_text_basesize.setCurrentValue(m_cardTemplate.getBaseTextSize());
    }

    private void updateSpacingControls() {
        m_valueSliderWidth.setCurrentValue(m_cardTemplate.getWidth());
        m_valueSliderBetween.setCurrentValue(m_cardTemplate.getCenterMargin());
        m_valueSliderPaddingTop.setCurrentValue(m_cardTemplate.getPaddingTop());
        m_valueSliderPaddingBottom.setCurrentValue(m_cardTemplate.getPaddingBottom());
        m_valueSliderPaddingLeftRight.setCurrentValue(m_cardTemplate.getPaddingLeftRight());
    }

    public int defaultFieldColor() {
        return ContextCompat.getColor(this, R.color.md_black);
    }



    // card views
    private CardView m_questionCardView;
    private CardView m_answerCardView;

    // card textviews
    private TextView m_questionTextView;
    private TextView m_answerTextView;


    private CardStyle m_cardStyle;

    private FrameLayout m_fieldSettingsView;
    private FrameLayout m_soundView;
    private FrameLayout m_fontView;
    private FrameLayout m_marginsView;

    // views
    private RecyclerView m_fullFieldListView;
    private FieldListAdapter m_fieldListAdapter;
    private MotionLayout m_cardstyleEditorCards;

    // navigation
    BottomNavigationView m_bottomNavigation;


    // field setting controls
    private Spinner m_fieldSpinner;
    private ArrayAdapter<CardField> m_fieldSpinnerAdapter;
    private FrameLayout m_fieldSettingsPanel;
    private ValueSlider m_field_relativesize;
    private CheckBox m_lineReturnCheckBox;
    private CheckBox m_htmlCheckBox;
    private Spinner m_fieldAlignmentSpinner;
    private ArrayAdapter<CharSequence> m_fieldAlignmentAdapter;
    private ValueSlider m_field_leftmargin;
    private ImageView m_fieldColorCircle;
    private SpectrumPalette m_field_colorPalette;
    private LinearLayout m_field_colorSelector;

    // text controls
    private TextView m_text_font_family;
    private Button m_text_font_lookup;
    private ValueSlider m_text_basesize;

    // margin controls
    private ValueSlider m_valueSliderWidth;
    private ValueSlider m_valueSliderBetween;
    private ValueSlider m_valueSliderPaddingTop;
    private ValueSlider m_valueSliderPaddingBottom;
    private ValueSlider m_valueSliderPaddingLeftRight;

    private CardTemplateKey m_cardTemplateKey;
    private CardTemplate m_cardTemplate;
    private Card m_card;

    private CardField m_currentCardField = null;

    private FirebaseAnalytics m_firebaseAnalytics;
}

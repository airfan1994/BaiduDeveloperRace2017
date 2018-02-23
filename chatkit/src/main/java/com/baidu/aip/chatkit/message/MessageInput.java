/*******************************************************************************
 * Copyright 2016 stfalcon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.baidu.aip.chatkit.message;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.Space;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

import com.baidu.aip.chatkit.R;

/**
 * Component for input outcoming messages
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MessageInput extends RelativeLayout
        implements View.OnClickListener, TextWatcher {

    public static final int INPUT_MODE_TEXT = 0;
    public static final int INPUT_MODE_AUDIO = 1;
    protected EditText messageInput;
    protected ImageView voiceInput;
    protected ImageButton messageSendButton;
    protected ImageButton attachmentButton;
    protected ImageButton swithButton;
    protected Space swithButtonSpace;
    protected Space sendButtonSpace;
    protected Space attachmentButtonSpace;

    private int inputMode = INPUT_MODE_TEXT;
    private CharSequence input;
    private InputListener inputListener;
    private AttachmentsListener attachmentsListener;
    private SwithListener swithListener;
    private VoiceInputListener audioInputListener;

    public MessageInput(Context context) {
        super(context);
        init(context);
    }

    public MessageInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MessageInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Sets callback for 'submit' button.
     *
     * @param inputListener input callback
     */
    public void setInputListener(InputListener inputListener) {
        this.inputListener = inputListener;
    }

    /**
     * Sets callback for 'add' button.
     *
     * @param attachmentsListener input callback
     */
    public void setAttachmentsListener(AttachmentsListener attachmentsListener) {
        this.attachmentsListener = attachmentsListener;
    }

    public void setAudioInputListener(VoiceInputListener audioInputListener) {
        this.audioInputListener = audioInputListener;
    }

    public void setSwithListener(SwithListener swithListener) {
        this.swithListener = swithListener;
    }

    /**
     * Returns EditText for messages input
     *
     * @return EditText
     */
    public EditText getInputEditText() {
        return messageInput;
    }

    /**
     * Returns `submit` button
     *
     * @return ImageButton
     */
    public ImageButton getButton() {
        return messageSendButton;
    }

    /**
     * Returns `audio input` button
     *
     * @return AudioInputButton
     */
    public ImageView getVoiceInputButton() {
        return voiceInput;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.messageSendButton) {
            boolean isSubmitted = onSubmit();
            if (isSubmitted) {
                messageInput.setText("");
            }
        } else if (id == R.id.attachmentButton) {
            onAddAttachments();
        } else if (id == R.id.swithButton) {
            onSwith();
        } else if (id == R.id.audioInput) {
            onAudioInput();
        }
    }

    /**
     * This method is called to notify you that, within s,
     * the count characters beginning at start have just replaced old text that had length before
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        input = s;
        messageSendButton.setEnabled(input.length() > 0);
    }

    /**
     * This method is called to notify you that, within s,
     * the count characters beginning at start are about to be replaced by new text with length after.
     */
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // do nothing
    }

    /**
     * This method is called to notify you that, somewhere within s, the text has been changed.
     */
    @Override
    public void afterTextChanged(Editable editable) {
        // do nothing
    }

    private boolean onSubmit() {
        return inputListener != null && inputListener.onSubmit(input);
    }

    private void onAddAttachments() {
        if (attachmentsListener != null) {
            attachmentsListener.onAttachments();
        }
    }

    private void onAudioInput() {
        if (audioInputListener != null) {
            audioInputListener.onVoiceInputClick();
        }
    }

    private void onSwith() {
        if (inputMode == INPUT_MODE_TEXT) {
            inputMode = INPUT_MODE_AUDIO;
            swithButton.setImageDrawable(getResources().getDrawable(R.drawable.chat_btn_input_audio));
            messageInput.setVisibility(GONE);
            voiceInput.setVisibility(VISIBLE);
        } else if (inputMode == INPUT_MODE_AUDIO) {
            inputMode = INPUT_MODE_TEXT;
            swithButton.setImageDrawable(getResources().getDrawable(R.drawable.chat_btn_input_keyboard));
            messageInput.setVisibility(VISIBLE);
            voiceInput.setVisibility(GONE);
        }
        if (swithListener != null) {
            swithListener.onSwith(inputMode);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        init(context);
        MessageInputStyle style = MessageInputStyle.parse(context, attrs);

        this.voiceInput.setImageDrawable(style.getVoiceButtonBackground());
        // ViewCompat.setBackground(this.voiceInput, style.getVoiceButtonBackground());

        this.messageInput.setMaxLines(style.getInputMaxLines());
        this.messageInput.setHint(style.getInputHint());
        this.messageInput.setText(style.getInputText());
        this.messageInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getInputTextSize());
        this.messageInput.setTextColor(style.getInputTextColor());
        this.messageInput.setHintTextColor(style.getInputHintColor());
        ViewCompat.setBackground(this.messageInput, style.getInputBackground());
        setCursor(style.getInputCursorDrawable());

        this.swithButton.setImageDrawable(style.getSwithButtonIcon());
        this.swithButton.getLayoutParams().width = style.getSwithButtonWidth();
        this.swithButton.getLayoutParams().height = style.getSwithButtonHeight();
        ViewCompat.setBackground(this.swithButton, style.getSwithButtonBackground());

        this.swithButton.setVisibility(style.showSwithButton() ? VISIBLE : GONE);
        this.swithButtonSpace.setVisibility(style.showSwithButton() ? VISIBLE : GONE);
        this.swithButtonSpace.getLayoutParams().width = style.getSwithButtonMargin();

        this.attachmentButton.setVisibility(style.showAttachmentButton() ? VISIBLE : GONE);
        this.attachmentButton.setImageDrawable(style.getAttachmentButtonIcon());
        this.attachmentButton.getLayoutParams().width = style.getAttachmentButtonWidth();
        this.attachmentButton.getLayoutParams().height = style.getAttachmentButtonHeight();
        ViewCompat.setBackground(this.attachmentButton, style.getAttachmentButtonBackground());

        this.attachmentButtonSpace.setVisibility(style.showAttachmentButton() ? VISIBLE : GONE);
        this.attachmentButtonSpace.getLayoutParams().width = style.getAttachmentButtonMargin();

        this.messageSendButton.setImageDrawable(style.getInputButtonIcon());
        this.messageSendButton.getLayoutParams().width = style.getInputButtonWidth();
        this.messageSendButton.getLayoutParams().height = style.getInputButtonHeight();
        ViewCompat.setBackground(messageSendButton, style.getInputButtonBackground());
        this.sendButtonSpace.getLayoutParams().width = style.getInputButtonMargin();

        if (getPaddingLeft() == 0
                && getPaddingRight() == 0
                && getPaddingTop() == 0
                && getPaddingBottom() == 0) {
            setPadding(
                    style.getInputDefaultPaddingLeft(),
                    style.getInputDefaultPaddingTop(),
                    style.getInputDefaultPaddingRight(),
                    style.getInputDefaultPaddingBottom()
            );
        }
    }

    private void init(Context context) {
        inflate(context, R.layout.view_message_input, this);

        voiceInput = (ImageView) findViewById(R.id.audioInput);
        messageInput = (EditText) findViewById(R.id.messageInput);
        swithButton = (ImageButton) findViewById(R.id.swithButton);
        messageSendButton = (ImageButton) findViewById(R.id.messageSendButton);
        attachmentButton = (ImageButton) findViewById(R.id.attachmentButton);
        sendButtonSpace = (Space) findViewById(R.id.sendButtonSpace);
        attachmentButtonSpace = (Space) findViewById(R.id.attachmentButtonSpace);
        swithButtonSpace = (Space) findViewById(R.id.swithButtonSpace);

        voiceInput.setOnClickListener(this);
        swithButton.setOnClickListener(this);
        messageSendButton.setOnClickListener(this);
        attachmentButton.setOnClickListener(this);
        messageInput.addTextChangedListener(this);
        messageInput.setText("");
    }

    private void setCursor(Drawable drawable) {
        if (drawable == null) {
            return;
        }

        try {
            final Field drawableResField = TextView.class.getDeclaredField("mCursorDrawableRes");
            drawableResField.setAccessible(true);

            final Object drawableFieldOwner;
            final Class<?> drawableFieldClass;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                drawableFieldOwner = this.messageInput;
                drawableFieldClass = TextView.class;
            } else {
                final Field editorField = TextView.class.getDeclaredField("mEditor");
                editorField.setAccessible(true);
                drawableFieldOwner = editorField.get(this.messageInput);
                drawableFieldClass = drawableFieldOwner.getClass();
            }
            final Field drawableField = drawableFieldClass.getDeclaredField("mCursorDrawable");
            drawableField.setAccessible(true);
            drawableField.set(drawableFieldOwner, new Drawable[] {drawable, drawable});
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * Interface definition for a callback to be invoked when user pressed 'submit' button
     */
    public interface InputListener {

        /**
         * Fires when user presses 'send' button.
         *
         * @param input input entered by user
         *
         * @return if input text is valid, you must return {@code true} and input will be cleared, otherwise return
         * false.
         */
        boolean onSubmit(CharSequence input);
    }

    /**
     * Interface definition for a callback to be invoked when user presses 'add' button
     */
    public interface AttachmentsListener {

        /**
         * Fires when user presses  button.
         */
        void onAttachments();
    }

    /**
     * Interface definition for a callback to be invoked when user presses 'swith' button
     */
    public interface SwithListener {

        /**
         * Fires when user presses 'swith'  button.
         */
        void onSwith(int swith);
    }

    /**
     * Interface definition for a callback to be invoked when user presses 'swith' button
     */
    public interface VoiceInputListener {

        /**
         * Fires when user presses 'swith'  button.
         */
        void onVoiceInputClick();
    }
}

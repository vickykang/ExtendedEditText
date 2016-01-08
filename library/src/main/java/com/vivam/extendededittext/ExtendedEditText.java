package com.vivam.extendededittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * ExtendedEditText is an extended EditText.
 * To allow users to clear all input text
 * and view(or hide) password.
 *
 * <p>
 * <b>XML attributes</b>
 * <p>
 * @see {@link R.styleable#ExtendedEditText ExtendedEditText Attributes},
 * {@link android.R.styleable#EditText EditText Attributes},
 * {@link android.R.styleable#TextView TextView Attributes},
 * {@link android.R.styleable#View View Attributes}
 *
 * @attr ref R.styleable#ExtendedEditText_clearDrawable
 * @attr ref R.styleable#ExtendedEditText_eyeDrawable
 * @attr ref R.styleable#ExtendedEditText_eyeOffDrawable
 * @attr ref R.styleable#ExtendedEditText_enableClear
 * @attr ref R.styleable#ExtendedEditText_enableEye
 * @attr ref R.styleable#ExtendedEditText_buttonMargin
 * @attr ref R.styleable#ExtendedEditText_buttonAlwaysCenter
 */

public class ExtendedEditText extends EditText {

    private static final String LOG_TAG = "ExtendedEditText";

    private Drawable mClearDrawable;
    private Drawable mEyeDrawable;
    private Drawable mEyeOffDrawable;

    /** Whether this view allows users to clear text. */
    private boolean mEnableClear;

    /** Whether this view allows users to view password.  */
    private boolean mEnableEye;

    /** The right margin of clear(eye) button. */
    private int mButtonMargin;

    /** Whether the buttons are always in the center of the view */
    private boolean mButtonAlwaysCenter;

    /** The input type set by users */
    private int mInputType;

    private Rect mClearRect;
    private Rect mDrawClearRect;
    private Rect mEyeRect;
    private Rect mDrawEyeRect;

    /** The right padding set by users */
    private int mOriginalPaddingRight;

    /** Whether the password is visible */
    private boolean mIsEyeOff = false;

    public ExtendedEditText(Context context) {
        this(context, null);
    }

    public ExtendedEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExtendedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mInputType = getInputType();

        /* Get right padding set by users */
        mOriginalPaddingRight = getPaddingRight();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExtendedEditText,
                defStyleAttr, 0);

        mClearDrawable = a.getDrawable(R.styleable.ExtendedEditText_clearDrawable);
        if (mClearDrawable == null) {
            mClearDrawable = context.getResources()
                    .getDrawable(R.drawable.default_extended_edit_text_clear_drawable);
        }

        mEyeDrawable = a.getDrawable(R.styleable.ExtendedEditText_eyeDrawable);
        if (mEyeDrawable == null) {
            mEyeDrawable = context.getResources()
                    .getDrawable(R.drawable.default_extended_edit_text_eye_drawable);
        }

        mEyeOffDrawable = a.getDrawable(R.styleable.ExtendedEditText_eyeOffDrawable);
        if (mEyeOffDrawable == null) {
            mEyeOffDrawable = context.getResources()
                    .getDrawable(R.drawable.default_extended_edit_text_eye_off_drawable);
        }

        mEnableClear = a.getBoolean(R.styleable.ExtendedEditText_enableClear, true);
        mEnableEye = a.getBoolean(R.styleable.ExtendedEditText_enableEye, true);
        enableEye(mEnableEye);

        mButtonMargin = a.getDimensionPixelSize(R.styleable.ExtendedEditText_buttonMargin,
                context.getResources().getDimensionPixelSize(
                        R.dimen.default_extended_edit_text_button_margin));

        mButtonAlwaysCenter = a.getBoolean(R.styleable.ExtendedEditText_buttonAlwaysCenter, true);
        a.recycle();

        /* Set new padding with clear button and eye button */
        setPadding(getPaddingLeft(), getPaddingTop(), mOriginalPaddingRight, getPaddingBottom());

        mClearRect = new Rect();
        mDrawClearRect = new Rect();
        mEyeRect = new Rect();
        mDrawEyeRect = new Rect();
    }

    public void setClearDrawable(Drawable drawable) {
        mClearDrawable = drawable;
        invalidate();
    }

    public void setEyeDrawable(Drawable drawable) {
        mEyeDrawable = drawable;
        invalidate();
    }

    public void setEyeOffDrawable(Drawable drawable) {
        mEyeOffDrawable = drawable;
        invalidate();
    }

    public void setButtonMargin(int margin) {
        mButtonMargin = margin;
        invalidate();
    }

    public void setButtonAlwaysCenter(boolean alwaysCenter) {
        mButtonAlwaysCenter = alwaysCenter;
        requestLayout();
    }

    public void enableClear(boolean enable) {
        mEnableClear = enable;
        invalidate();
    }

    public void enableEye(boolean enable) {
        mEnableEye = enable && isPasswordInputType();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        final int scrollX = getScrollX();
        final int scrollY = getScrollY();

        if (isClearEnable()) {
            mDrawClearRect.set(
                    mClearRect.left + scrollX,
                    mClearRect.top+ scrollY,
                    mClearRect.right + scrollX,
                    mClearRect.bottom+ scrollY
            );
            mClearDrawable.setBounds(mDrawClearRect);
            mClearDrawable.draw(canvas);
        }

        if (isEyeEnable()) {
            mDrawEyeRect.set(
                    mEyeRect.left + scrollX,
                    mEyeRect.top+ scrollY,
                    mEyeRect.right + scrollX,
                    mEyeRect.bottom+ scrollY);
            mEyeDrawable.setBounds(mDrawEyeRect);

            if (mIsEyeOff && mEyeOffDrawable != null) {
                mEyeOffDrawable.setBounds(mDrawEyeRect);
                mEyeOffDrawable.draw(canvas);

            } else if (!mIsEyeOff) {
                mEyeDrawable.draw(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*
         * If users touch inside clear button and enable clear, clear text.
         * Else if users touch inside eye button and enable view password,
         * show the text if it's invisible, else, hide it.
         */
        float x;
        float y;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                x = event.getX();
                y = event.getY();
                if ((isInsideClear(x, y) && isClearEnable())
                        || (isInsideEye(x, y) && isEyeEnable())) {
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                x = event.getX();
                y = event.getY();

                if (isInsideClear(x, y) && isClearEnable()) {
                    clear();
                    return false;
                }

                if (isEyeEnable() && isInsideEye(x, y)) {
                    if (mIsEyeOff) {
                        mIsEyeOff = false;
                        setInputType(mInputType);
                        setSelection(getText().length());
                    } else {
                        mIsEyeOff = true;
                        setInputType(InputType.TYPE_CLASS_TEXT);
                        setSelection(getText().length());
                    }
                    return false;
                }
                break;
            }

        }
        return super.onTouchEvent(event);
    }

    private boolean isInsideClear(float x, float y) {
        return mClearRect.contains((int) x, (int) y);
    }

    private boolean isInsideEye(float x, float y) {
        return mEyeRect.contains((int) x, (int) y);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        final int gravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;

        int offset = 0;
        int left;
        int top;

        /* Calculates the bounding rectangles of mEyeDrawable, mEyeOffDrawable,
         * and mClearDrawable. The rectangle indicates the location of drawable
         * int this view.
         */
        if (mEnableEye && mEyeDrawable != null) {
            offset += mButtonMargin + mEyeDrawable.getIntrinsicWidth();
            left = width - offset;

            if (mButtonAlwaysCenter) {
                top = (height - mEyeDrawable.getIntrinsicHeight()) / 2;
            } else {
                if (gravity == Gravity.TOP) {
                    top = getPaddingTop();
                } else if (gravity == Gravity.BOTTOM) {
                    top = height - getPaddingBottom() - mEyeDrawable.getIntrinsicHeight();
                } else {
                    top = (height - mEyeDrawable.getIntrinsicHeight()) / 2;
                }
            }

            mEyeRect.set(left, top, left + mEyeDrawable.getIntrinsicWidth(),
                    top + mEyeDrawable.getIntrinsicHeight());
            mEyeDrawable.setBounds(mEyeRect);
            if (mEyeOffDrawable != null) {
                mEyeOffDrawable.setBounds(mEyeRect);
            }
        }

        if (mEnableClear && mClearDrawable != null) {
            offset += mButtonMargin + mClearDrawable.getIntrinsicWidth();
            left = width - offset;

            if (mButtonAlwaysCenter) {
                top = (height - mClearDrawable.getIntrinsicHeight()) / 2;
            } else {
                if (gravity == Gravity.TOP) {
                    top = getPaddingTop();
                } else if (gravity == Gravity.BOTTOM) {
                    top = height - getPaddingBottom() - mClearDrawable.getIntrinsicHeight();
                } else {
                    top = (height - mClearDrawable.getIntrinsicHeight()) / 2;
                }
            }

            // top = getPaddingTop() + (height - mClearDrawable.getIntrinsicHeight()) / 2;
            mClearRect.set(left, top,
                    left + mClearDrawable.getIntrinsicWidth(),
                    top + mClearDrawable.getIntrinsicHeight());
            mClearDrawable.setBounds(mClearRect);
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        mOriginalPaddingRight = right;
        super.setPadding(left, top, getRealPaddingRight(right), bottom);
    }

    /**
     * Returns new right padding. If it is enable to view password, plus
     * the width of {@link #mEyeDrawable} and {@link #mButtonMargin}.
     * If it is enable to clear text, plus the width of
     * {@link #mClearDrawable} and {@link #mButtonMargin}.
     *
     * @param right The original right padding set by users
     * @return The new right padding
     */
    private int getRealPaddingRight(int right) {
        if (mEnableEye && mEyeDrawable != null) {
            right += mButtonMargin + mEyeDrawable.getIntrinsicWidth();
        }

        if (mEnableClear && mClearDrawable != null) {
            right += mButtonMargin + mClearDrawable.getIntrinsicWidth();
        }

        return right;
    }

    /**
     * Sets text null.
     */
    private void clear() {
        setText(null);
    }

    private boolean isEmpty() {
        return TextUtils.isEmpty(getText().toString());
    }

    private boolean isClearEnable() {
        return mEnableClear && !isEmpty() && mClearDrawable != null;
    }

    private boolean isEyeEnable() {
        return mEnableEye && !isEmpty() && mEyeDrawable != null;
    }

    /**
     * Returns whether the input type is password type.
     */
    private boolean isPasswordInputType() {
        final int variation =
                mInputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
        final boolean passwordInputType = variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        final boolean webPasswordInputType = variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        final boolean numberPasswordInputType = variation
                == (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD);

        return passwordInputType || webPasswordInputType || numberPasswordInputType;
    }
}

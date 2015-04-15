package com.paypoint.sdk.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.paypoint.sdk.demo.R;

public class MaskedEditText extends EditText {
	
	private String mask;
	
	public MaskedEditText(Context context) {
		super(context );
		intialise();
	}

	public MaskedEditText(Context context, AttributeSet attrs) {
		super(context, attrs );
		intialise(attrs);
	}
	
	public MaskedEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		intialise(attrs);
	}
	
	private void intialise() {
		
		setInputType(InputType.TYPE_CLASS_PHONE);
		addTextChangedListener(  new MaskFormatter(this, mask) );
	}
	
	private void intialise(AttributeSet attrs) { 
		
		TypedArray a = getContext().getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.MaskedEditText,
		        0, 0);

		try {
			mask = a.getString(R.styleable.MaskedEditText_mask);
		} finally {
	       a.recycle();
		}
		
		intialise();
	}
	
	private static class MaskFormatter implements TextWatcher {
		
		private EditText editView;
		private String mask;
		private boolean deleting;
		
		public MaskFormatter( EditText editView, String mask ) {
			
			this.editView = editView;
			this.mask = mask;
		}

		@Override
		public void afterTextChanged(final Editable s) {
			
			String fieldText = editView.getText().toString();
			
//			String validText = fieldText.replaceAll("[^" + mask + "]", "" );
			String digits = fieldText.replaceAll("[^\\d]", "" );

			String tokens = mask.replaceAll("[^#]", "");
			String newText = mask;

			// If there are non-numerics at the end of the line and we are deleting,
			// we need to make sure we delete the last numeric.
			if( deleting &&
				digits.length() == tokens.length() ) {
				digits = digits.substring(0, digits.length() - 1 );
			}
			
			// Replace any number tokens in the copied mask with numbers from the current field...
			for( int i = 0; i < digits.length(); i++ ) {
			
				if( newText.contains( "#") == false) {
					break;
				}
				
				char fieldChar = digits.charAt(i);
				
				// Skip any non-digits in the current text...
				if( fieldChar >= '0' && fieldChar <= '9' ) {
					
					newText = newText.replaceFirst("#", String.valueOf(fieldChar));
				}
			}
			
			// Remove anything past any remaining tokens (line is not complete).
			int firstTokcen = newText.indexOf("#");
			if( firstTokcen != -1 ) {
					
				newText = newText.replaceAll("#.*", "" );
			}
			
			// if deleting, clear back to the last digit.
			if( deleting ) {
				newText = newText.replaceAll("[^\\d]*$", "" );
			}
			
			// Update the field but disable this listener first to prevent a recursive call to the formatter.
			if( fieldText.equals( newText ) == false ) {

				editView.removeTextChangedListener(this);
				editView.setText( newText );
				editView.addTextChangedListener(this);

				Selection.setSelection(editView.getEditableText(), newText.length());
			}
		}
			
//			// Update the field but disable this listener first to prevent a recursive call to the formatter.
//			if( fieldText.equals( validText ) == false ) {
//
//				int startOfSelection = Selection.getSelectionStart(editView.getEditableText());
//				if( startOfSelection > validText.length() ) {
//					startOfSelection = validText.length();
//				} else {
//					startOfSelection--;
//				}
//				
//				editView.removeTextChangedListener(this);
//				editView.setText( validText );
//				editView.addTextChangedListener(this);
//				
//				Selection.setSelection(editView.getEditableText(), startOfSelection);
//			}
//		}
//
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			deleting = (after < count);
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	}
}
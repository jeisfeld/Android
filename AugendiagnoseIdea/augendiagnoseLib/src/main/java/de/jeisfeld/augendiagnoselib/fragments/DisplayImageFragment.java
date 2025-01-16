package de.jeisfeld.augendiagnoselib.fragments;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import de.jeisfeld.augendiagnoselib.Application;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.activities.DisplayHtmlActivity;
import de.jeisfeld.augendiagnoselib.activities.DisplayImageActivity;
import de.jeisfeld.augendiagnoselib.activities.DisplayOneActivity;
import de.jeisfeld.augendiagnoselib.activities.DisplayTwoActivity;
import de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView;
import de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView.GuiElementUpdater;
import de.jeisfeld.augendiagnoselib.components.OverlayPinchImageView.PinchMode;
import de.jeisfeld.augendiagnoselib.components.colorpicker.ColorPickerConstants;
import de.jeisfeld.augendiagnoselib.components.colorpicker.ColorPickerDialog;
import de.jeisfeld.augendiagnoselib.components.colorpicker.ColorPickerSwatch.OnColorSelectedListener;
import de.jeisfeld.augendiagnoselib.util.DialogUtil;
import de.jeisfeld.augendiagnoselib.util.PreferenceUtil;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;
import de.jeisfeld.augendiagnoselib.util.imagefile.EyePhoto.RightLeft;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegMetadata;
import de.jeisfeld.augendiagnoselib.util.imagefile.JpegMetadataUtil;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * Variant of DisplayOneFragment that includes overlay handling.
 */
public class DisplayImageFragment extends Fragment implements GuiElementUpdater, OnColorSelectedListener {
	/**
	 * The resource key for the file path.
	 */
	private static final String STRING_FILE = "de.jeisfeld.augendiagnoselib.FILE";
	/**
	 * The resource kay for the image index (in case of multiple images).
	 */
	private static final String STRING_IMAGEINDEX = "de.jeisfeld.augendiagnoselib.IMAGEINDEX";
	/**
	 * The resource kay for the rightleft information (if not contained in the image).
	 */
	private static final String STRING_RIGHTLEFT = "de.jeisfeld.augendiagnoselib.RIGHTLEFT";

	/**
	 * The size of the circle overlay bitmap.
	 */
	private static final int CIRCLE_BITMAP_SIZE = OverlayPinchImageView.OVERLAY_SIZE;
	/**
	 * The radius of the iris circle displayed.
	 */
	private static final int CIRCLE_RADIUS_IRIS = 448;
	/**
	 * The radius of the pupil circle displayed.
	 */
	private static final int CIRCLE_RADIUS_PUPIL = 384;

	/**
	 * The file path.
	 */
	private String mFile;

	/**
	 * The image index.
	 */
	private int mImageIndex;

	/**
	 * Information if right or left image.
	 */
	private RightLeft mRightLeft;

	/**
	 * Flag indicating if overlays are allowed.
	 */
	private OverlayStatus mOverlayStatus;

	protected final OverlayStatus getOverlayStatus() {
		return mOverlayStatus;
	}

	/**
	 * Flag holding information if fragment is shown in landscape mode.
	 */
	private boolean mIsLandscape;

	private boolean isLandscape() {
		return mIsLandscape;
	}

	protected final void setLandscape(final boolean newIsLandscape) {
		this.mIsLandscape = newIsLandscape;
	}

	/**
	 * The view displaying the image.
	 */
	private OverlayPinchImageView mImageView;

	/**
	 * The number of overlay buttons, excluding the pupil overlay.
	 */
	public static final int OVERLAY_BUTTON_COUNT;

	/**
	 * The button for showing the image in full resolution.
	 */
	private Button mClarityButton;

	/**
	 * The button for showing the image comment.
	 */
	private Button mCommentButton;

	/**
	 * The button for showing the image info.
	 */
	private Button mInfoButton;

	/**
	 * The button for saving image metadata.
	 */
	private Button mSaveButton;

	/**
	 * The button for sharing the image.
	 */
	private Button mShareButton;

	/**
	 * The button for showing or hiding the tools.
	 */
	private Button mToolsButton;

	/**
	 * The button for showing or hiding the tools.
	 */
	private Button mHelpButton;

	/**
	 * The array of overlay buttons.
	 */
	private ToggleButton[] mToggleOverlayButtons;

	/**
	 * The lock button.
	 */
	private ToggleButton mLockButton;

	/**
	 * The button for setting pupil position.
	 */
	private ToggleButton mPupilButton;

	/**
	 * The color selector button.
	 */
	private Button mSelectColorButton;

	/**
	 * The button for the rotation of the eye photo.
	 */
	private Button mRotateImageButton;

	/**
	 * The button for the guided setup of iris and pupil position.
	 */
	private Button mGuidedTopoSetupButton;

	/**
	 * The brightness SeekBar.
	 */
	private SeekBar mSeekbarBrightness;

	/**
	 * The contrast SeekBar.
	 */
	private SeekBar mSeekbarContrast;

	/**
	 * The saturation SeekBar.
	 */
	private SeekBar mSeekbarSaturation;

	/**
	 * The color remperature SeekBar.
	 */
	private SeekBar mSeekbarColorTemperature;

	/**
	 * A flag indicating which utilities (seekbars, buttons) should be displayed.
	 */
	private UtilitiyStatus mShowUtilities = UtilitiyStatus.SHOW_NOTHING;

	/**
	 * The overlay color.
	 */
	private int mOverlayColor = Color.RED;

	/**
	 * The status of the pupil button.
	 */
	private PupilButtonStatus mPupilButtonStatus;

	static {
		TypedArray overlayButtonResources = Application.getAppContext().getResources().obtainTypedArray(R.array.overlay_buttons);
		OVERLAY_BUTTON_COUNT = Math.min(overlayButtonResources.length(), OverlayPinchImageView.OVERLAY_COUNT) - 1;
		overlayButtonResources.recycle();
	}

	/**
	 * Initialize the fragment with the file name.
	 *
	 * @param initialFile       the file path.
	 * @param initialImageIndex The index of the view (required if there are multiple such fragments)
	 * @param initialRightLeft  Information if it is the right or left eye (if not in image metadata)
	 */
	public final void setParameters(final String initialFile, final int initialImageIndex,
									@Nullable final RightLeft initialRightLeft) {
		Bundle args = new Bundle();
		args.putString(STRING_FILE, initialFile);
		args.putInt(STRING_IMAGEINDEX, initialImageIndex);
		if (initialRightLeft != null) {
			args.putSerializable(STRING_RIGHTLEFT, initialRightLeft);
		}

		setArguments(args);
	}

	/*
	 * Retrieve parameters.
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFile = getArguments().getString(STRING_FILE);
		mImageIndex = getArguments().getInt(STRING_IMAGEINDEX, 0);
		mRightLeft = (RightLeft) getArguments().getSerializable(STRING_RIGHTLEFT);
	}

	@Override
	public final void onResume() {
		super.onResume();
	}

	@Override
	public final void onPause() {
		super.onPause();
	}

	/*
	 * Inflate View.
	 */
	// OVERRIDABLE
	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
			setLandscape(true);
			return inflater.inflate(R.layout.fragment_display_image_landscape, container, false);
		}
		else {
			setLandscape(false);
			return inflater.inflate(R.layout.fragment_display_image_portrait, container, false);
		}
	}

	/*
	 * Update data from view.
	 */
	@Override
	public final void onActivityCreated(@Nullable final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getView() == null) {
			return;
		}

		mImageView = getView().findViewById(R.id.mainImage);
		mImageView.setGuiElementUpdater(this);
		mImageView.allowFullResolution(hasAutoFullResolution());

		mLockButton = getView().findViewById(R.id.toggleButtonLink);

		TypedArray overlayButtonResources = getResources().obtainTypedArray(R.array.overlay_buttons);
		mToggleOverlayButtons = new ToggleButton[OVERLAY_BUTTON_COUNT];
		for (int i = 0; i < OVERLAY_BUTTON_COUNT; i++) {
			mToggleOverlayButtons[i] = getView().findViewById(overlayButtonResources.getResourceId(i, -1));
			mToggleOverlayButtons[i].setVisibility(View.VISIBLE);
		}
		overlayButtonResources.recycle();

		mPupilButton = getView().findViewById(R.id.toggleButtonPupil);
		mSelectColorButton = getView().findViewById(R.id.buttonSelectColor);

		mClarityButton = getView().findViewById(R.id.buttonClarity);
		mInfoButton = getView().findViewById(R.id.buttonInfo);
		mCommentButton = getView().findViewById(R.id.buttonComment);
		mSaveButton = getView().findViewById(R.id.buttonSave);
		mShareButton = getView().findViewById(R.id.buttonShare);
		mToolsButton = getView().findViewById(R.id.buttonTools);
		mHelpButton = getView().findViewById(R.id.buttonHelp);
		mGuidedTopoSetupButton = getView().findViewById(R.id.buttonGuidedTopoSetup);
		mRotateImageButton = getView().findViewById(R.id.buttonRotateImage);

		// Layout for circle button
		ImageSpan imageSpan = new ImageSpan(getActivity(), R.drawable.ic_btn_wheel);
		SpannableString content = new SpannableString("X");
		content.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		mToggleOverlayButtons[0].setText(content);
		mToggleOverlayButtons[0].setTextOn(content);
		mToggleOverlayButtons[0].setTextOff(content);

		// Layout for guided topo setup button
		mGuidedTopoSetupButton.setText(content);

		// Layout for rotate image button
		ImageSpan imageSpan2 = new ImageSpan(getActivity(), R.drawable.ic_btn_rotate);
		SpannableString content2 = new SpannableString("X");
		content2.setSpan(imageSpan2, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		mRotateImageButton.setText(content2);

		if (savedInstanceState != null) {
			mShowUtilities = (UtilitiyStatus) savedInstanceState.getSerializable("showUtilities");
			mOverlayColor = savedInstanceState.getInt("overlayColor", Color.RED);
			mPupilButtonStatus = (PupilButtonStatus) savedInstanceState.getSerializable("pupilButtonStatus");
			mLockButton.setChecked(savedInstanceState.getBoolean("lockButtonIsChecked"));
			mOverlayStatus = (OverlayStatus) savedInstanceState.getSerializable("overlayStatus");

			if (mOverlayStatus == OverlayStatus.GUIDED || mOverlayStatus == OverlayStatus.GUIDE_IRIS || mOverlayStatus == OverlayStatus.GUIDE_PUPIL) {
				drawOverlayCircle();
			}
		}
		else {
			mShowUtilities = getDefaultShowUtilitiesValue();
			mOverlayColor = PreferenceUtil.getSharedPreferenceInt(R.string.key_overlay_color, Color.RED);
			mPupilButtonStatus = PupilButtonStatus.OFF;
			mOverlayStatus = PreferenceUtil.getSharedPreferenceBoolean(R.string.key_guided_topo_setup) ? OverlayStatus.GUIDED : OverlayStatus.ALLOWED;
		}

		showUtilities();

		// Initialize the onClick listeners for the buttons
		setButtonListeners();

		// Special handling for non-JPEG images
		checkJpeg();

		configureSeekbars();

		// The following also updates the selectColorButton
		mImageView.setOverlayColor(mOverlayColor);

		// Layout for pupil button
		mPupilButton.setEnabled(mLockButton.isChecked());
		setPupilButtonBitmap();
	}

	/**
	 * Configure the seekbars for brightness, contrast, saturation and color temperature.
	 */
	private void configureSeekbars() {
		mSeekbarBrightness = getView().findViewById(R.id.seekBarBrightness);
		mSeekbarBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(@NonNull final SeekBar seekBar, final int progress, final boolean fromUser) {
				if (fromUser) {
					mImageView.updateColorSettings(((float) seekBar.getProgress()) / seekBar.getMax() * 2 - 1, null, null, null, true);
				}
			}
		});

		mSeekbarContrast = getView().findViewById(R.id.seekBarContrast);
		mSeekbarContrast.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(@NonNull final SeekBar seekBar, final int progress, final boolean fromUser) {
				if (fromUser) {
					mImageView.updateColorSettings(null, ((float) seekBar.getProgress()) / seekBar.getMax() * 2 - 1, null, null, true);
				}
			}
		});

		mSeekbarSaturation = getView().findViewById(R.id.seekBarSaturation);
		mSeekbarSaturation.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(@NonNull final SeekBar seekBar, final int progress, final boolean fromUser) {
				if (fromUser) {
					mImageView.updateColorSettings(null, null, ((float) seekBar.getProgress()) / seekBar.getMax() * 2 - 1, null, true);
				}
			}
		});

		mSeekbarColorTemperature = getView().findViewById(R.id.seekBarColorTemperature);
		mSeekbarColorTemperature.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(@NonNull final SeekBar seekBar, final int progress, final boolean fromUser) {
				if (fromUser) {
					mImageView.updateColorSettings(null, null, null, ((float) seekBar.getProgress()) / seekBar.getMax() * 2 - 1, true);
				}
			}
		});

		OnTouchListener onIconTouchListener1 = new OnTouchListener() {
			@Override
			public boolean onTouch(final View v, final MotionEvent event) {
				v.performClick();
				if (mImageView == null) {
					return false;
				}
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mImageView.updateColorSettings(0f, 0f, 0f, 0f, false);
					return true;
				case MotionEvent.ACTION_UP:
					mImageView.updateColorSettings(((float) mSeekbarBrightness.getProgress()) / mSeekbarBrightness.getMax() * 2 - 1,
							((float) mSeekbarContrast.getProgress()) / mSeekbarContrast.getMax() * 2 - 1,
							((float) mSeekbarSaturation.getProgress()) / mSeekbarSaturation.getMax() * 2 - 1,
							((float) mSeekbarColorTemperature.getProgress()) / mSeekbarColorTemperature.getMax() * 2 - 1,
							false);
					return true;
				default:
					return false;
				}
			}
		};
		getView().findViewById(R.id.iconBrightness).setOnTouchListener(onIconTouchListener1);
		getView().findViewById(R.id.iconContrast).setOnTouchListener(onIconTouchListener1);

		OnTouchListener onIconTouchListener2 = new OnTouchListener() {
			@Override
			public boolean onTouch(final View v, final MotionEvent event) {
				v.performClick();
				if (mImageView == null) {
					return false;
				}
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mImageView.updateColorSettings(null, null, 0f, 0f, false);
					return true;
				case MotionEvent.ACTION_UP:
					mImageView.updateColorSettings(null, null,
							((float) mSeekbarSaturation.getProgress()) / mSeekbarSaturation.getMax() * 2 - 1,
							((float) mSeekbarColorTemperature.getProgress()) / mSeekbarColorTemperature.getMax() * 2 - 1,
							false);
					return true;
				default:
					return false;
				}
			}
		};
		getView().findViewById(R.id.iconSaturation).setOnTouchListener(onIconTouchListener2);
		getView().findViewById(R.id.iconColorTemperature).setOnTouchListener(onIconTouchListener2);
	}

	/**
	 * Initialize the on-click actions for the buttons.
	 */
	private void setButtonListeners() {
		setOverlayButtonListeners();
		setToolbarButtonListeners();
		setGuidedTopoButtonListeners();
	}

	/**
	 * Initialize the on-click actions for the overlay buttons.
	 */
	private void setOverlayButtonListeners() {
		for (int i = 0; i < OVERLAY_BUTTON_COUNT; i++) {
			final int index = i;
			mToggleOverlayButtons[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					onToggleOverlayClicked(index);
				}
			});
		}
		String[] overlayButtonStrings = getResources().getStringArray(R.array.overlay_button_strings);
		for (int i = 1; i < OVERLAY_BUTTON_COUNT; i++) {
			mToggleOverlayButtons[i].setText(overlayButtonStrings[i]);
			mToggleOverlayButtons[i].setTextOn(overlayButtonStrings[i]);
			mToggleOverlayButtons[i].setTextOff(overlayButtonStrings[i]);
			final int index = i;
			mToggleOverlayButtons[i].setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(final View v) {
					return displayOverlaySelectionPopup(index, false);
				}
			});
			if (PreferenceUtil.getIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, i, -1) < 0) {
				mToggleOverlayButtons[i].setVisibility(View.GONE);
			}
		}

		mLockButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				onToggleLockClicked();
			}
		});

		mSelectColorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				onButtonSelectColorClicked(v);
			}
		});


		mPupilButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				onTogglePupilClicked();
			}
		});
	}

	/**
	 * Initialize the on-click actions for the guided topo and rotate buttons.
	 */
	private void setGuidedTopoButtonListeners() {
		if (PreferenceUtil.getSharedPreferenceBoolean(R.string.key_guided_topo_setup)) {
			mGuidedTopoSetupButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					switch (mOverlayStatus) {
					case GUIDED:
						DialogUtil.displayTip(getActivity(), R.string.message_tip_overlay_guided, R.string.key_tip_overlay_guided);
						mOverlayStatus = OverlayStatus.GUIDE_IRIS;
						mImageView.setVisibility(View.INVISIBLE);
						mRotateImageButton.setVisibility(View.GONE);
						drawOverlayCircle();
						requestLayout();
						break;
					case GUIDE_IRIS:
						mImageView.setOverlayPosition((float) CIRCLE_RADIUS_IRIS / CIRCLE_BITMAP_SIZE);
						mImageView.lockOverlay(true, true);
						mOverlayStatus = OverlayStatus.GUIDE_PUPIL;
						drawOverlayCircle();
						break;
					case GUIDE_PUPIL:
						mImageView.setPupilPosition((float) CIRCLE_RADIUS_PUPIL / CIRCLE_BITMAP_SIZE);
						mImageView.storePupilPosition();
						mOverlayStatus = OverlayStatus.ALLOWED;
						setLockChecked(true);
						drawOverlayCircle();
						mImageView.post(new Runnable() {
							@Override
							public void run() {
								mImageView.redoInitialScaling();
							}
						});
						break;
					default:
						break;
					}
				}
			});

			mRotateImageButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					showRotateMenu(v);
				}
			});
		}
	}

	/**
	 * Initialize the on-click actions for the toolbar buttons.
	 */
	private void setToolbarButtonListeners() {
		if (!hasAutoFullResolution()) {
			mClarityButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					mImageView.showFullResolutionSnapshot(false);
					DialogUtil.displayTip(getActivity(), R.string.message_tip_clarity, R.string.key_tip_clarity);
				}
			});
		}
		else {
			mClarityButton.setVisibility(View.GONE);
		}

		mInfoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				DialogUtil.displayImageInfo(getActivity(), mImageView.getEyePhoto());
			}
		});

		mCommentButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (mImageView.getMetadata() != null) {
					((DisplayImageActivity) getActivity()).startEditComment(DisplayImageFragment.this, mImageView.getMetadata().getComment());
					DialogUtil.displayTip(getActivity(), R.string.message_tip_editcomment, R.string.key_tip_editcomment);
				}
			}
		});

		mShareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showShareMenu(v);
			}
		});

		mSaveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showSaveMenu(v);
				DialogUtil.displayTip(getActivity(), R.string.message_tip_saveview, R.string.key_tip_saveview);
			}
		});

		mToolsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				mShowUtilities = mShowUtilities.getNextStatus(alwaysShowOverlayBar(), allowAllBars());
				showUtilities();
				updateDefaultShowUtilities(mShowUtilities);
			}
		});

		mHelpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				DisplayHtmlActivity.startActivity(getActivity(), R.string.html_display_photos);
			}
		});
	}


	/**
	 * Method indicating if the overlay bar should always be shown.
	 *
	 * @return the indicator if the overlay bar should always be shown.
	 */
	// OVERRIDABLE
	protected boolean alwaysShowOverlayBar() {
		return true;
	}

	/**
	 * Method indicating if all bars may be shown at once.
	 *
	 * @return the indicator if all bars may be shown at once.
	 */
	// OVERRIDABLE
	protected boolean allowAllBars() {
		return true;
	}

	/**
	 * Check if the image is JPEG - otherwise disable some functionality.
	 */
	private void checkJpeg() {
		try {
			JpegMetadataUtil.checkJpeg(mFile);
		}
		catch (IOException e) {
			mOverlayStatus = OverlayStatus.NON_JPEG;
			showUtilities();
			DialogUtil.displayTip(getActivity(), R.string.message_tip_jpeg, R.string.key_tip_jpeg);
		}

	}

	/**
	 * Draw the overlay circle and print the corresponding guide text.
	 */
	private void drawOverlayCircle() {
		if (getView() == null) {
			return;
		}

		mGuidedTopoSetupButton.setEnabled(true);
		ImageView overlayView = getView().findViewById(R.id.circleOverlay);
		TextView textViewGuide = getView().findViewById(R.id.textViewGuide);

		mImageView.updatePosition(mOverlayStatus, mOverlayStatus == OverlayStatus.GUIDE_IRIS
				? (float) CIRCLE_RADIUS_IRIS / CIRCLE_BITMAP_SIZE
				: (float) CIRCLE_RADIUS_PUPIL / CIRCLE_BITMAP_SIZE);

		if (mOverlayStatus != OverlayStatus.GUIDE_IRIS && mOverlayStatus != OverlayStatus.GUIDE_PUPIL) {
			overlayView.setVisibility(View.GONE);
			textViewGuide.setVisibility(View.GONE);
			mGuidedTopoSetupButton.setText(mToggleOverlayButtons[0].getText());
			return;
		}

		mGuidedTopoSetupButton.setText(getString(R.string.button_ok));

		Bitmap overlayBitmap = Bitmap.createBitmap(CIRCLE_BITMAP_SIZE, CIRCLE_BITMAP_SIZE, Bitmap.Config.ARGB_8888);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		int overlayColor = PreferenceUtil.getSharedPreferenceInt(R.string.key_overlay_color, Color.RED);
		paint.setColor(overlayColor);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5); // MAGIC_NUMBER

		Canvas canvas = new Canvas(overlayBitmap);
		int circleRadius = mOverlayStatus == OverlayStatus.GUIDE_IRIS ? CIRCLE_RADIUS_IRIS : CIRCLE_RADIUS_PUPIL;
		canvas.drawCircle(CIRCLE_BITMAP_SIZE / 2.0f, CIRCLE_BITMAP_SIZE / 2.0f, circleRadius, paint);

		overlayView.setImageBitmap(overlayBitmap);
		overlayView.setVisibility(View.VISIBLE);

		textViewGuide.setText(mOverlayStatus == OverlayStatus.GUIDE_IRIS ? getString(R.string.message_guide_resize_iris)
				: getString(R.string.message_guide_resize_pupil));
		textViewGuide.setVisibility(View.VISIBLE);
	}


	/**
	 * Helper method for onClick actions for overlay buttons.
	 *
	 * @param position The number of the overlay button.
	 */
	private void onToggleOverlayClicked(final int position) {
		boolean isChecked = mToggleOverlayButtons[position].isChecked();
		boolean buttonGetsUnchecked = false;

		int overlayPosition = PreferenceUtil.getIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, position, -1);

		for (int i = 0; i < OVERLAY_BUTTON_COUNT; i++) {
			if (position != i) {
				if (mToggleOverlayButtons[i].isChecked()) {
					mToggleOverlayButtons[i].setChecked(false);
					buttonGetsUnchecked = true;
				}
			}
		}

		if (mPupilButtonStatus != PupilButtonStatus.OFF) {
			mPupilButtonStatus = PupilButtonStatus.OFF;
			setPupilButtonBitmap();
			mImageView.storePupilPosition();
		}

		if (overlayPosition >= 0 || !isChecked) {
			mImageView.triggerOverlay(overlayPosition, isChecked ? PinchMode.OVERLAY : PinchMode.ALL);
			if (isChecked) {
				String overlayName = getResources().getStringArray(R.array.overlay_names)[overlayPosition];
			}
		}

		if (isChecked && !buttonGetsUnchecked) {
			DialogUtil.displayTip(getActivity(), R.string.message_tip_overlay_buttons, R.string.key_tip_overlay_buttons);
		}
	}

	/**
	 * Display a popup for the selection of an overlay type.
	 *
	 * @param position     The number of the overlay button.
	 * @param forNewButton Flag indicating the popup is displayed for a new button. Then it is enforced to select a new overlay.
	 * @return true if long click was processed.
	 */
	private boolean displayOverlaySelectionPopup(final int position, final boolean forNewButton) {
		PopupMenu popupMenu;
		View anchorView = isLandscape() ? getActivity().findViewById(R.id.anchorForContextMenu) : mToggleOverlayButtons[position];

		if (SystemUtil.isAtLeastVersion(Build.VERSION_CODES.KITKAT)) {
			popupMenu = getPopupMenuKitkat(anchorView);
		}
		else {
			popupMenu = new PopupMenu(getActivity(), anchorView);
		}

		Menu menu = popupMenu.getMenu();

		if (position == getHighestOverlayButtonIndex()) {
			if (position > 1) {
				MenuItem menuItemRemove = menu.add(DialogUtil.fromHtml("<b>" + getString(R.string.menu_remove_overlay_button) + "</b>"));
				menuItemRemove.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(final MenuItem item) {
						mToggleOverlayButtons[position].setChecked(false);
						onToggleOverlayClicked(position);
						PreferenceUtil.setIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, position, -1);
						mToggleOverlayButtons[position].setVisibility(View.GONE);
						return true;
					}
				});
			}

			if (position < OVERLAY_BUTTON_COUNT - 1) {
				MenuItem menuItemAdd = menu.add(DialogUtil.fromHtml("<b>" + getString(R.string.menu_add_overlay_button) + "</b>"));
				menuItemAdd.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(final MenuItem item) {
						displayOverlaySelectionPopup(position + 1, true);
						return true;
					}
				});
			}
		}

		String[] overlayNames = getResources().getStringArray(R.array.overlay_names);
		for (int i = 1; i < overlayNames.length - 1; i++) {
			final int index = i;

			final Integer oldButtonPosition = buttonForOverlayWithIndex(i);
			MenuItem menuItem = null;
			if (oldButtonPosition == null) {
				menuItem = menu.add(overlayNames[i]);
			}
			else {
				if (!forNewButton) {
					SpannableString itemName = new SpannableString(
							getResources().getStringArray(R.array.overlay_button_strings)[oldButtonPosition] + " " + overlayNames[i]);
					if (oldButtonPosition == position) {
						itemName.setSpan(new StyleSpan(Typeface.BOLD), 0, itemName.length(), 0);
					}
					else {
						itemName.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, itemName.length(), 0);
					}
					menuItem = menu.add(Menu.NONE, Menu.NONE, OverlayPinchImageView.OVERLAY_COUNT + oldButtonPosition, itemName);
				}
			}

			if (menuItem != null) {
				menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(final MenuItem item) {
						if (oldButtonPosition != null && oldButtonPosition != position) {
							// If the same overlay is already used, switch overlays
							int currentOverlay = PreferenceUtil.getIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, position, -1);
							PreferenceUtil.setIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, oldButtonPosition, currentOverlay);
						}
						PreferenceUtil.setIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, position, index);
						mToggleOverlayButtons[position].setChecked(true);
						mToggleOverlayButtons[position].setVisibility(View.VISIBLE);
						onToggleOverlayClicked(position);
						return true;
					}
				});
			}
		}

		popupMenu.show();
		return true;
	}

	/**
	 * Get popup menu with gravity right - only supported for Kitkat or later.
	 *
	 * @param anchorView The anchor view.
	 * @return The popup menu.
	 */
	@NonNull
	@RequiresApi(Build.VERSION_CODES.KITKAT)
	private PopupMenu getPopupMenuKitkat(final View anchorView) {
		return new PopupMenu(getActivity(), anchorView, Gravity.END);
	}

	/**
	 * Get the index of the highest active overlay button.
	 *
	 * @return The index of the highest active overlay button.
	 */
	public static int getHighestOverlayButtonIndex() {
		int maxIndex = -1;
		for (int i = 0; i < OVERLAY_BUTTON_COUNT; i++) {
			if (PreferenceUtil.getIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, i, -1) >= 0) {
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	/**
	 * Check if there is already a button configured for a certain overlay type.
	 *
	 * @param index The index of the overlay type.
	 * @return The button index configured for this overlay type.
	 */
	public static Integer buttonForOverlayWithIndex(final int index) {
		if (index < 0) {
			return null;
		}
		for (int i = 0; i < OVERLAY_BUTTON_COUNT; i++) {
			if (PreferenceUtil.getIndexedSharedPreferenceIntString(R.string.key_indexed_overlaytype, i, -1) == index) {
				return i;
			}
		}
		return null;
	}

	/**
	 * onClick action for Button to switch link between overlay and image.
	 */
	private void onToggleLockClicked() {
		mImageView.lockOverlay(mLockButton.isChecked(), true);
		mPupilButton.setEnabled(mLockButton.isChecked());
	}

	/**
	 * onClick action for Button to change pupil size and position.
	 */
	private void onTogglePupilClicked() {
		switch (mPupilButtonStatus) {
		case OFF:
			mPupilButtonStatus = PupilButtonStatus.CENTER;
			break;
		case CENTER:
			mPupilButtonStatus = PupilButtonStatus.MOVE;
			break;
		case MOVE:
			mPupilButtonStatus = PupilButtonStatus.OFF;
			break;
		default:
			break;
		}

		setPupilButtonBitmap();

		if (mPupilButtonStatus == PupilButtonStatus.OFF) {
			mImageView.triggerOverlay(OverlayPinchImageView.OVERLAY_PUPIL_INDEX, PinchMode.ALL);
			mImageView.storePupilPosition();
		}
		else {
			for (int i = 0; i < OVERLAY_BUTTON_COUNT; i++) {
				if (mToggleOverlayButtons[i].isChecked()) {
					mToggleOverlayButtons[i].setChecked(false);
				}
			}

			PinchMode pinchMode = mPupilButtonStatus == PupilButtonStatus.CENTER ? PinchMode.PUPIL_CENTER : PinchMode.PUPIL;
			mImageView.triggerOverlay(OverlayPinchImageView.OVERLAY_PUPIL_INDEX, pinchMode);
		}
	}

	/**
	 * onClick action for Button to select color of overlays.
	 *
	 * @param view The view of the select color button.
	 */
	private void onButtonSelectColorClicked(final View view) {
		ColorPickerDialog dialog =
				ColorPickerDialog
						.newInstance(R.string.color_picker_title, ColorPickerConstants.COLOR_PICKER_COLORS,
								mImageView.getOverlayColor(),
								ColorPickerConstants.COLOR_PICKER_COLUMNS,
								ColorPickerConstants.COLOR_PICKER_SIZE);

		dialog.setTargetFragment(this, 0);
		dialog.show(getFragmentManager(), ColorPickerDialog.class.toString());
	}

	/**
	 * Create the popup menu for saving metadata.
	 *
	 * @param view The view opening the menu.
	 */
	private void showSaveMenu(final View view) {
		PopupMenu popup = new PopupMenu(getActivity(), view);
		popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(@NonNull final MenuItem item) {
				int itemId = item.getItemId();
				if (itemId == R.id.action_store_color_settings) {
					mImageView.storeColorSettings(false);
					return true;
				}
				else if (itemId == R.id.action_reset_color_settings) {
					mImageView.storeColorSettings(true);
					return true;
				}
				else if (itemId == R.id.action_store_position) {
					mImageView.storePositionZoom(false);
					return true;
				}
				else if (itemId == R.id.action_reset_position) {
					mImageView.storePositionZoom(true);
					return true;
				}
				else if (itemId == R.id.action_store_overlay_color) {
					mImageView.storeOverlayColor(false);
					return true;
				}
				else if (itemId == R.id.action_reset_overlay_color) {
					mImageView.storeOverlayColor(true);
					return true;
				}
				else if (itemId == R.id.action_delete_overlay_position) {
					mImageView.resetOverlayPosition(true);
					return true;
				}
				else {
					return true;
				}
			}
		});
		popup.inflate(R.menu.menu_image_save);

		if (mShowUtilities == UtilitiyStatus.SHOW_NOTHING) {
			popup.getMenu().removeGroup(R.id.group_overlay);
		}
		else {
			try {
				JpegMetadata metadata = mImageView.getEyePhoto().getImageMetadata();

				if (!metadata.hasOverlayPosition()) {
					popup.getMenu().removeGroup(R.id.group_overlay);
				}
				else if (metadata.hasFlag(JpegMetadata.FLAG_OVERLAY_SET_BY_CAMERA_ACTIVITY)
						|| metadata.hasFlag(JpegMetadata.FLAG_OVERLAY_POSITION_DETERMINED_AUTOMATICALLY)) {
					popup.getMenu().removeItem(R.id.action_store_overlay_color);
					popup.getMenu().removeItem(R.id.action_reset_overlay_color);
				}
			}
			catch (NullPointerException e) {
				// ignore
			}
		}

		if (mShowUtilities == UtilitiyStatus.SHOW_NOTHING || mShowUtilities == UtilitiyStatus.ONLY_OVERLAY) {
			popup.getMenu().removeGroup(R.id.group_color_settings);
		}

		popup.show();
	}

	/**
	 * Create the popup menu for rotating the image.
	 *
	 * @param view The view opening the menu.
	 */
	private void showRotateMenu(final View view) {
		PopupMenu popup = new PopupMenu(getActivity(), view);
		popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(@NonNull final MenuItem item) {
				int itemId = item.getItemId();
				if (itemId == R.id.action_rotate_right) {
					mImageView.rotateImage((short) ExifInterface.ORIENTATION_ROTATE_90);
					return true;
				}
				else if (itemId == R.id.action_rotate_left) {
					mImageView.rotateImage((short) ExifInterface.ORIENTATION_ROTATE_270);
					return true;
				}
				else if (itemId == R.id.action_rotate_180) {
					mImageView.rotateImage((short) ExifInterface.ORIENTATION_ROTATE_180);
					return true;
				}
				else {
					return true;
				}
			}
		});
		popup.inflate(R.menu.menu_image_rotate);

		popup.show();
	}

	/**
	 * Create the popup menu for sharing the image.
	 *
	 * @param view The view opening the menu.
	 */
	private void showShareMenu(final View view) {
		final DisplayImageFragment otherFragment;
		if (getActivity() instanceof DisplayTwoActivity) {
			otherFragment = ((DisplayTwoActivity) getActivity()).getOtherFragment(DisplayImageFragment.this);
		}
		else {
			otherFragment = null;
		}

		PopupMenu popup = new PopupMenu(getActivity(), view);

		popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(@NonNull final MenuItem item) {
				int itemId = item.getItemId();

				boolean currentView = itemId == R.id.action_share_this_image_view || itemId == R.id.action_share_both_image_views;
				boolean bothImages = itemId == R.id.action_share_both_images || itemId == R.id.action_share_both_image_views;

				if (bothImages && otherFragment != null) {
					Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
					ArrayList<Uri> uris = new ArrayList<>();
					uris.add(mImageView.getBitmapUri(currentView, "image"));
					uris.add(otherFragment.getImageView().getBitmapUri(currentView, "image2"));
					intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
					intent.setType("image/*");
					startActivity(intent);
					return true;
				}
				else {
					Intent intent = new Intent(Intent.ACTION_SEND);
					Uri uri = mImageView.getBitmapUri(currentView, "image");
					intent.putExtra(Intent.EXTRA_STREAM, uri);
					intent.setType("image/*");
					startActivity(intent);
					return true;
				}
			}
		});

		popup.inflate(R.menu.menu_image_share);

		if (otherFragment == null) {
			popup.getMenu().removeItem(R.id.action_share_both_images);
			popup.getMenu().removeItem(R.id.action_share_both_image_views);
		}

		popup.show();
	}

	/**
	 * Get the displayed EyePhoto.
	 *
	 * @return The displayed EyePhoto.
	 */
	protected OverlayPinchImageView getImageView() {
		return mImageView;
	}

	/**
	 * Store the comment in the image.
	 *
	 * @param comment The comment text to be stored.
	 */
	public final void storeComment(final String comment) {
		mImageView.storeComment(comment);
	}

	/**
	 * Show or hide the utilities (overlay bar, scrollbars).
	 */
	private void showUtilities() {
		View fragmentView = getView();
		if (fragmentView == null) {
			return;
		}

		// Icon
		if (isLandscape()) {
			mToolsButton.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					mShowUtilities.mArrowUp ? R.drawable.ic_tools_left : R.drawable.ic_tools_right, 0);
		}
		else {
			mToolsButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,
					mShowUtilities.mArrowUp ? R.drawable.ic_tools_up : R.drawable.ic_tools_down);
		}

		// Separator bar
		fragmentView.findViewById(R.id.separatorTools).setVisibility(mShowUtilities == UtilitiyStatus.SHOW_NOTHING ? View.GONE : View.VISIBLE);

		// Brightness/contrast seekbars
		int brightnessContrastVisibility = mShowUtilities == UtilitiyStatus.OVERLAY_BRIGHTNESS_CONTRAST
				|| mShowUtilities == UtilitiyStatus.SHOW_EVERYTHING ? View.VISIBLE : View.GONE;
		fragmentView.findViewById(R.id.seekBarBrightnessLayout).setVisibility(brightnessContrastVisibility);
		fragmentView.findViewById(R.id.seekBarContrastLayout).setVisibility(brightnessContrastVisibility);

		// Saturation/color temperature seekbars
		int saturationColorTemperatureVisibility = mShowUtilities == UtilitiyStatus.OVERLAY_SATURATION_COLOR_TEMPERATURE
				|| mShowUtilities == UtilitiyStatus.SHOW_EVERYTHING ? View.VISIBLE : View.GONE;
		fragmentView.findViewById(R.id.seekBarSaturationLayout).setVisibility(saturationColorTemperatureVisibility);
		fragmentView.findViewById(R.id.seekBarColorTemperatureLayout).setVisibility(saturationColorTemperatureVisibility);

		if (mShowUtilities == UtilitiyStatus.SHOW_NOTHING) {
			fragmentView.findViewById(R.id.buttonOverlayLayout).setVisibility(View.GONE);
			fragmentView.findViewById(R.id.buttonGuidedTopoLayout).setVisibility(View.GONE);
			if (mOverlayStatus == OverlayStatus.NON_JPEG) {
				mCommentButton.setVisibility(View.GONE);
				mSaveButton.setVisibility(View.GONE);
			}
		}
		else {
			if (mOverlayStatus == OverlayStatus.ALLOWED) {
				fragmentView.findViewById(R.id.buttonOverlayLayout).setVisibility(View.VISIBLE);
				fragmentView.findViewById(R.id.buttonGuidedTopoLayout).setVisibility(View.GONE);
			}
			else if (mOverlayStatus == OverlayStatus.FORBIDDEN) {
				fragmentView.findViewById(R.id.buttonOverlayLayout).setVisibility(View.GONE);
				fragmentView.findViewById(R.id.buttonGuidedTopoLayout).setVisibility(View.GONE);
			}
			else if (mOverlayStatus == OverlayStatus.NON_JPEG) {
				fragmentView.findViewById(R.id.buttonOverlayLayout).setVisibility(View.VISIBLE);
				fragmentView.findViewById(R.id.buttonGuidedTopoLayout).setVisibility(View.GONE);
				mCommentButton.setVisibility(View.GONE);
				mSaveButton.setVisibility(View.GONE);
				mLockButton.setVisibility(View.GONE);
				mPupilButton.setVisibility(View.GONE);
			}
			else {
				fragmentView.findViewById(R.id.buttonOverlayLayout).setVisibility(View.GONE);
				fragmentView.findViewById(R.id.buttonGuidedTopoLayout).setVisibility(View.VISIBLE);
				if (mOverlayStatus == OverlayStatus.GUIDED) {
					mRotateImageButton.setVisibility(View.VISIBLE);
				}
				else {
					mRotateImageButton.setVisibility(View.GONE);
				}
			}
		}
		requestLayout();
	}

	/**
	 * Get the value indicating what utilities should be shown.
	 *
	 * @return The value indicating if utilities should be shown.
	 */
	// OVERRIDABLE
	protected UtilitiyStatus getDefaultShowUtilitiesValue() {
		UtilitiyStatus level = UtilitiyStatus.fromResourceValue(
				PreferenceUtil.getSharedPreferenceInt(R.string.key_internal_show_utilities_fullscreen, -1));

		if (level == null) {
			// call this method only if no value is set
			level = UtilitiyStatus.SHOW_EVERYTHING;
		}

		return level;
	}

	/**
	 * Update default for showing utilities.
	 *
	 * @param utilityStatus the new default value for showing utilities.
	 */
	// OVERRIDABLE
	protected void updateDefaultShowUtilities(final UtilitiyStatus utilityStatus) {
		PreferenceUtil.setSharedPreferenceInt(R.string.key_internal_show_utilities_fullscreen, utilityStatus.getNumericValue());
	}

	@Override
	public final void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("showUtilities", mShowUtilities);
		outState.putInt("overlayColor", mOverlayColor);
		outState.putBoolean("lockButtonIsChecked", mLockButton.isChecked());
		outState.putSerializable("pupilButtonStatus", mPupilButtonStatus);
		outState.putSerializable("overlayStatus", mOverlayStatus);
	}

	/**
	 * Initialize images - to be called after the views have restored instance state.
	 */
	public final void initializeImages() {
		mImageView.setImage(mFile, getActivity(), mImageIndex);

		if (mImageView.getEyePhoto().getRightLeft() == null && mRightLeft != null) {
			mImageView.getEyePhoto().setRightLeft(mRightLeft);
		}
	}

	/**
	 * Trigger redrawing of the imageView from outside.
	 */
	public final void requestLayout() {
		mImageView.post(new Runnable() {
			@Override
			public void run() {
				mImageView.requestLayout();
				mImageView.invalidate();
				mImageView.setVisibility(View.VISIBLE);
			}
		});
	}

	/**
	 * Method indicating if images should be displayed in full resolution automatically.
	 *
	 * @return true if should be shown in full resolution.
	 */
	private boolean hasAutoFullResolution() {
		int fullResolutionFlag = PreferenceUtil.getSharedPreferenceIntString(R.string.key_full_resolution, null);

		if (getActivity().getClass().equals(DisplayOneActivity.class)) {
			return fullResolutionFlag > 0;
		}
		else {
			return fullResolutionFlag == 2;
		}
	}

	/**
	 * Implementation of OnColorSelectedListener, to react on selected overlay color.
	 *
	 * @param color the selected color.
	 */
	@Override
	public final void onColorSelected(final int color) {
		mImageView.setOverlayColor(color);
		mOverlayColor = color;
	}

	// Implementation of GuiElementUpdater

	@Override
	public final void setLockChecked(final boolean checked) {
		mLockButton.setChecked(checked);
		mPupilButton.setEnabled(checked);

		// Set overlay status according to the lock.
		if (mOverlayStatus != OverlayStatus.GUIDE_IRIS && mOverlayStatus != OverlayStatus.GUIDE_PUPIL && mOverlayStatus != OverlayStatus.NON_JPEG) {
			if (mImageView.canHandleOverlays() || mRightLeft != null) {
				if (PreferenceUtil.getSharedPreferenceBoolean(R.string.key_guided_topo_setup) && !checked) {
					mOverlayStatus = OverlayStatus.GUIDED;
					mGuidedTopoSetupButton.setEnabled(true);
					mRotateImageButton.setEnabled(true);
				}
				else {
					mOverlayStatus = OverlayStatus.ALLOWED;
				}
			}
			else {
				mOverlayStatus = OverlayStatus.FORBIDDEN;
			}
		}

		showUtilities();
	}

	@Override
	public final void updateSeekbarBrightness(final float brightness) {
		float progress = (brightness + 1) * mSeekbarBrightness.getMax() / 2;
		mSeekbarBrightness.setProgress(Float.valueOf(progress).intValue());
	}

	@Override
	public final void updateSeekbarContrast(final float contrast) {
		float progress = (contrast + 1) * mSeekbarContrast.getMax() / 2;
		mSeekbarContrast.setProgress(Float.valueOf(progress).intValue());
	}

	@Override
	public final void updateSeekbarSaturation(final float saturation) {
		float progress = (saturation + 1) * mSeekbarSaturation.getMax() / 2;
		mSeekbarSaturation.setProgress(Float.valueOf(progress).intValue());
	}

	@Override
	public final void updateSeekbarColorTemperature(final float colorTemperature) {
		float progress = (colorTemperature + 1) * mSeekbarColorTemperature.getMax() / 2;
		mSeekbarColorTemperature.setProgress(Float.valueOf(progress).intValue());
	}

	@Override
	public final void updateOverlayColorButton(final int color) {
		mSelectColorButton.setTextColor(color);
	}

	@Override
	public final int getOverlayDefaultColor() {
		return mOverlayColor;
	}

	@Override
	public final void resetOverlays() {
		for (int i = 0; i < OVERLAY_BUTTON_COUNT; i++) {
			mToggleOverlayButtons[i].setChecked(false);
		}
	}

	/**
	 * Set the bitmap of the pupil button.
	 */
	private void setPupilButtonBitmap() {
		int pupilButtonResource = 0;

		switch (mPupilButtonStatus) {
		case OFF:
			pupilButtonResource = R.drawable.ic_btn_pupil_0;
			break;
		case CENTER:
			pupilButtonResource = R.drawable.ic_btn_pupil_1;
			break;
		case MOVE:
			pupilButtonResource = R.drawable.ic_btn_pupil_2;
			break;
		default:
			break;
		}

		ImageSpan imageSpan = new ImageSpan(getActivity(), pupilButtonResource);
		SpannableString content = new SpannableString("X");
		content.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		mPupilButton.setText(content);
		mPupilButton.setTextOn(content);
		mPupilButton.setTextOff(content);

		mPupilButton.setChecked(mPupilButtonStatus != PupilButtonStatus.OFF);
	}

	/**
	 * Base implementation of OnSeekBarChangeListener, used for brightness and contrast seekbars.
	 */
	private abstract class OnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
		@Override
		public void onStopTrackingTouch(final SeekBar seekBar) {
			mImageView.refresh();
		}

		@Override
		public void onStartTrackingTouch(final SeekBar seekBar) {
			// do nothing
		}
	}

	/**
	 * Status of the button to move the pupil.
	 */
	private enum PupilButtonStatus {
		/**
		 * Button is off.
		 */
		OFF,
		/**
		 * Button is configured for keeping pupil in the center.
		 */
		CENTER,
		/**
		 * Button allows to move the pupil.
		 */
		MOVE
	}

	/**
	 * Status of the overlay usage.
	 */
	public enum OverlayStatus {
		/**
		 * Overlays cannot be used.
		 */
		FORBIDDEN,
		/**
		 * Overlay guided setup can be used.
		 */
		GUIDED,
		/**
		 * The guide is active to set the iris size.
		 */
		GUIDE_IRIS,
		/**
		 * The guide is active to set the pupil size.
		 */
		GUIDE_PUPIL,
		/**
		 * Overlays can be used.
		 */
		ALLOWED,
		/**
		 * Special handling for non-JPEG images.
		 */
		NON_JPEG
	}

	/**
	 * The display status of utilities.
	 */
	public enum UtilitiyStatus {
		/**
		 * Show nothing.
		 */
		SHOW_NOTHING(1, true),
		/**
		 * Show only the overlay pane.
		 */
		ONLY_OVERLAY(2, true),
		/**
		 * Show the overlay pane and the brightness/contrast sliders.
		 */
		OVERLAY_BRIGHTNESS_CONTRAST(3, true),
		/**
		 * Show the overlay pane and the saturation/color temparature sliders.
		 */
		SHOW_EVERYTHING(4, false),
		/**
		 * Show the overlay pane and the saturation/color temparature sliders.
		 */
		OVERLAY_SATURATION_COLOR_TEMPERATURE(5, false);

		/**
		 * The numeric value.
		 */
		private final int mNumericValue;
		/**
		 * Flag indicating if the "up" or "down" arrow should be displayed on the "show utilities" button.
		 */
		private final boolean mArrowUp;

		protected int getNumericValue() {
			return mNumericValue;
		}

		/**
		 * A map from the resourceValue to the color.
		 */
		private static final SparseArray<UtilitiyStatus> UTILITY_STATUS_MAP = new SparseArray<>();

		static {
			for (UtilitiyStatus utilityStatus : UtilitiyStatus.values()) {
				UTILITY_STATUS_MAP.put(utilityStatus.mNumericValue, utilityStatus);
			}
		}

		/**
		 * Constructor giving the resourceValue and the color value.
		 *
		 * @param numericValue The numeric value.
		 * @param arrowUp      Flag indicating if the "up" or "down" arrow should be displayed on the "show utilities" button.
		 */
		UtilitiyStatus(final int numericValue, final boolean arrowUp) {
			mNumericValue = numericValue;
			mArrowUp = arrowUp;
		}

		/**
		 * Get the utility status from its numeric value.
		 *
		 * @param numericValue The resource value.
		 * @return The corresponding UtilityStatus.
		 */
		protected static UtilitiyStatus fromResourceValue(final int numericValue) {
			return UTILITY_STATUS_MAP.get(numericValue);
		}

		/**
		 * The next status after pressing the "show utilities" button.
		 *
		 * @param alwaysShowOverlayBar Flag indicating if the overlay bar should always be shown.
		 * @param allowAllBars         Flag indicating if all bars may be shown at once.
		 * @return the next status.
		 */
		private UtilitiyStatus getNextStatus(final boolean alwaysShowOverlayBar, final boolean allowAllBars) {
			switch (this) {
			case SHOW_NOTHING:
				return ONLY_OVERLAY;
			case ONLY_OVERLAY:
				return OVERLAY_BRIGHTNESS_CONTRAST;
			case OVERLAY_BRIGHTNESS_CONTRAST:
				return allowAllBars ? SHOW_EVERYTHING : OVERLAY_SATURATION_COLOR_TEMPERATURE;
			case SHOW_EVERYTHING:
				return OVERLAY_SATURATION_COLOR_TEMPERATURE;
			case OVERLAY_SATURATION_COLOR_TEMPERATURE:
			default:
				return alwaysShowOverlayBar ? ONLY_OVERLAY : SHOW_NOTHING;
			}
		}

	}
}

package de.eisfeldj.augendiagnose.components;

import android.app.Activity;
import android.content.Context;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.util.EyePhotoPair;
import de.eisfeldj.augendiagnose.util.ImageSelectionAndDisplayHandler;

/**
 * Array adapter class to display an eye photo pair in a list (display for selection of second picture).
 */
public class ListPicturesForSecondNameArrayAdapter extends ListPicturesForNameBaseArrayAdapter {

	/**
	 * Constructor for the adapter.
	 *
	 * @param activity
	 *            The activity using the adapter.
	 * @param eyePhotoPairs
	 *            The array of eye photo pairs to be displayed.
	 */
	public ListPicturesForSecondNameArrayAdapter(final Activity activity, final EyePhotoPair[] eyePhotoPairs) {
		super(activity, eyePhotoPairs);
	}

	/**
	 * Default adapter to be used by the framework.
	 *
	 * @param context
	 *            The Context the view is running in.
	 */
	public ListPicturesForSecondNameArrayAdapter(final Context context) {
		super(context);
	}

	@Override
	protected final int getLayout() {
		return R.layout.adapter_list_pictures_for_second_name;
	}

	@Override
	protected final void prepareViewForSelection(final EyeImageView view) {
		ImageSelectionAndDisplayHandler.getInstance().prepareViewForSecondSelection(view);
	}

}

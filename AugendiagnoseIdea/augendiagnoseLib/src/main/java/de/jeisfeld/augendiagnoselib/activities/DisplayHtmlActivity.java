package de.jeisfeld.augendiagnoselib.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.jeisfeld.augendiagnoselib.R;
import de.jeisfeld.augendiagnoselib.fragments.DisplayHelpNavigationFragment;
import de.jeisfeld.augendiagnoselib.fragments.DisplayHtmlFragment;
import de.jeisfeld.augendiagnoselib.util.SystemUtil;

/**
 * Activity to display an HTML page. Used for display of help pages.
 */
public class DisplayHtmlActivity extends BaseFragmentActivity {

	/**
	 * The resource key for the resource to be displayed.
	 */
	private static final String STRING_EXTRA_RESOURCE = "de.jeisfeld.augendiagnoselib.RESOURCE";

	/**
	 * Resource id indicating that there is no resource.
	 */
	private static final int NO_RESOURCE = -1;

	/**
	 * The main fragment tag.
	 */
	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
	/**
	 * The details fragment tag.
	 */
	private static final String FRAGMENT_DETAILS_TAG = "FRAGMENT_DETAILS_TAG";

	/**
	 * Static helper method to start the activity, passing the resource holding the HTML as string.
	 *
	 * @param context  The context in which the activity is started.
	 * @param resource The resource to be displayed.
	 */
	public static void startActivity(@NonNull final Context context, final int resource) {
		Intent intent = new Intent(context, DisplayHtmlActivity.class);
		intent.putExtra(STRING_EXTRA_RESOURCE, resource);
		context.startActivity(intent);
	}

	/**
	 * Static helper method to start the activity without resource - just start navigation.
	 *
	 * @param context The context in which the activity is started.
	 */
	public static void startActivity(@NonNull final Context context) {
		Intent intent = new Intent(context, DisplayHtmlActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String[] activitiesWithHomeEnablement = getResources().getStringArray(R.array.activities_with_home_enablement);
		if (getActionBar() != null && Arrays.asList(activitiesWithHomeEnablement).contains(getClass().getName())) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		int resource = getIntent().getIntExtra(STRING_EXTRA_RESOURCE, NO_RESOURCE);

		if (SystemUtil.isTablet()) {
			setContentView(R.layout.activity_fragments_list_detail);
		}
		else {
			setContentView(R.layout.activity_fragments_single);
		}

		if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG) == null) {
			if (SystemUtil.isTablet() || resource == NO_RESOURCE) {
				displayNavigation();
			}
			if (resource != NO_RESOURCE) {
				displayDetails(resource);
			}
		}
	}

	@Override
	protected final void onResume() {
		super.onResume();
	}

	/**
	 * Display the navigation page.
	 */
	private void displayNavigation() {
		int containerViewId = SystemUtil.isTablet() ? R.id.fragment_list : R.id.fragment_container;
		DisplayHelpNavigationFragment fragment = new DisplayHelpNavigationFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(containerViewId, fragment, FRAGMENT_TAG)
				.commit();
		getSupportFragmentManager().executePendingTransactions();
	}

	/**
	 * Display a details HTML page.
	 *
	 * @param resourceId The resource to be shown in the details.
	 */
	public final void displayDetails(final int resourceId) {
		int containerViewId = SystemUtil.isTablet() ? R.id.fragment_detail : R.id.fragment_container;
		String fragmentTag = SystemUtil.isTablet() ? FRAGMENT_DETAILS_TAG : FRAGMENT_TAG;

		DisplayHtmlFragment detailFragment = new DisplayHtmlFragment();
		detailFragment.setParameters(resourceId);
		getSupportFragmentManager().beginTransaction()
				.replace(containerViewId, detailFragment, fragmentTag)
				.commit();
		getSupportFragmentManager().executePendingTransactions();
	}

	/*
	 * Inflate options menu.
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		if (!SystemUtil.isTablet()) {
			// Allow navigation to help overview page on smartphones
			getMenuInflater().inflate(R.menu.menu_default, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * Handle menu actions.
	 */
	@Override
	public final boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_help) {
			displayNavigation();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

}

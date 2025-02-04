package de.eisfeldj.augendiagnosefx.fxelements;

import de.eisfeldj.augendiagnosefx.controller.Controller;
import de.eisfeldj.augendiagnosefx.controller.DisplayImageHolderController;
import de.eisfeldj.augendiagnosefx.controller.DisplayImagePairController;
import de.eisfeldj.augendiagnosefx.controller.DisplayPhotosController;
import de.eisfeldj.augendiagnosefx.controller.MainController;
import de.eisfeldj.augendiagnosefx.util.DialogUtil;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ConfirmDialogListener;
import de.eisfeldj.augendiagnosefx.util.FxmlConstants;
import de.eisfeldj.augendiagnosefx.util.FxmlUtil;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;
import de.eisfeldj.augendiagnosefx.util.ResourceUtil;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhotoPair;
import de.eisfeldj.augendiagnosefx.util.imagefile.ImageUtil.Resolution;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

/**
 * Special GridPane for displaying a pair of eye photos.
 */
public class EyePhotoPairNode extends GridPane implements Controller {
	/**
	 * The parent controller.
	 */
	private DisplayPhotosController mParentController;

	/**
	 * Height of the left image.
	 */
	private double mHeightLeft = 0;
	/**
	 * Height of the right image.
	 */
	private double mHeightRight = 0;

	/**
	 * The eye photo pair.
	 */
	private EyePhotoPair mPair;

	/**
	 * The label for the date.
	 */
	@FXML
	private Label mLabelDate;

	/**
	 * The image view of the right eye.
	 */
	@FXML
	private ImageViewPane mImageViewRight;

	/**
	 * The image view of the left eye.
	 */
	@FXML
	private ImageViewPane mImageViewLeft;

	@Override
	public final Parent getRoot() {
		return this;
	}

	/**
	 * A boolean property indicating if images are loaded.
	 */
	private BooleanProperty mImagesLoadedProperty = new SimpleBooleanProperty(false);

	/**
	 * Get the property if images are loaded.
	 *
	 * @return The property if images are loaded.
	 */
	public final BooleanProperty getImagesLoadedProperty() {
		return mImagesLoadedProperty;
	}

	/**
	 * Constructor given a pair of eye photos.
	 *
	 * @param pair
	 *            The eye photo pair.
	 * @param initialParentController
	 *            The parent controller.
	 */
	public EyePhotoPairNode(final EyePhotoPair pair, final DisplayPhotosController initialParentController) {
		mParentController = initialParentController;
		mPair = pair;

		FxmlUtil.loadFromFxml(this, FxmlConstants.FXML_EYE_PHOTO_PAIR_NODE);

		mLabelDate.setText(pair.getDateDisplayString());

		if (pair.getRightEye() != null) {
			mImageViewRight.setImageView(getImageView(pair.getRightEye()));
		}
		if (pair.getLeftEye() != null) {
			mImageViewLeft.setImageView(getImageView(pair.getLeftEye()));
		}

		mLabelDate.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				if (event.getButton() != MouseButton.PRIMARY) {
					return;
				}
				if (MainController.getInstance().isSplitPane()) {
					return;
				}

				if (pair.isComplete()) {
					DisplayImagePairController controller =
							(DisplayImagePairController) FxmlUtil.displaySubpage(FxmlConstants.FXML_DISPLAY_IMAGE_PAIR, -1, true);
					controller.setEyePhotos(pair);
				}
			}
		});

		mLabelDate.setContextMenu(createDateContextMenu());
	}

	/**
	 * Create the context menu for the date.
	 *
	 * @return the context menu.
	 */
	private ContextMenu createDateContextMenu() {
		ContextMenu menu = new ContextMenu();

		MenuItem menuItemRemove = new MenuItem();
		menuItemRemove.setText(ResourceUtil.getString(ResourceConstants.MENU_DELETE_IMAGES));

		menuItemRemove.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				DialogUtil.displayConfirmationMessage(new ConfirmDialogListener() {

					@Override
					public void onDialogPositiveClick() {
						mParentController.removeItem(EyePhotoPairNode.this);
						mPair.delete();
					}

					@Override
					public void onDialogNegativeClick() {
						// do nothing
					}
				}, ResourceConstants.BUTTON_DELETE,
						ResourceConstants.MESSAGE_DIALOG_CONFIRM_DELETE_DATE, mPair.getPersonName(), mLabelDate.getText());
			}
		});
		menu.getItems().add(menuItemRemove);

		return menu;
	}

	/**
	 * Get the image view for a thumbnail.
	 *
	 * @param eyePhoto
	 *            The eye photo to be displayed.
	 * @return The image view.
	 */
	private ImageView getImageView(final EyePhoto eyePhoto) {
		Image image = eyePhoto.getImage(Resolution.THUMB);
		image.progressProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				if (newValue.doubleValue() == 1) {
					checkIfImagesLoaded();
				}
			}
		});

		ImageView imageView = new ImageView(image);
		imageView.setPreserveRatio(true);
		imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				if (PreferenceUtil.getPreferenceBoolean(PreferenceUtil.KEY_SHOW_SPLIT_WINDOW)
						&& !MainController.getInstance().isSplitPane()) {
					MainController.getInstance().setSplitPane(FxmlConstants.FXML_DISPLAY_PHOTOS);
				}

				DisplayImageHolderController controller = (DisplayImageHolderController) FxmlUtil
						.displaySubpage(FxmlConstants.FXML_DISPLAY_IMAGE_HOLDER, mParentController.getPaneIndex(), true);
				controller.setEyePhoto(eyePhoto);
			}
		});

		// Ensure that height is adapted to image width.
		imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				switch (eyePhoto.getRightLeft()) {
				case RIGHT:
					mHeightRight = newValue.doubleValue();
					break;
				case LEFT:
					mHeightLeft = newValue.doubleValue();
					break;
				default:
				}
				setPrefHeight(Math.max(mHeightLeft, mHeightRight));
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						requestParentLayout();
					}
				});
			}
		});
		return imageView;
	}

	/**
	 * Check if the images are loaded.
	 *
	 * @return true if the images are loaded.
	 */
	private boolean checkIfImagesLoaded() {
		if (mImagesLoadedProperty.get()) {
			return true;
		}

		Image imageRight = mImageViewRight.getImageView().getImage();
		Image imageLeft = mImageViewRight.getImageView().getImage();

		boolean loaded = imageRight.getProgress() == 1 && imageLeft.getProgress() == 1;
		if (loaded) {
			mImagesLoadedProperty.set(true);
		}
		return loaded;
	}

}

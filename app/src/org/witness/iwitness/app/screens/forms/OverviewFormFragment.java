package org.witness.iwitness.app.screens.forms;

import info.guardianproject.odkparser.FormWrapper.ODKFormListener;

import java.io.FileNotFoundException;
import java.util.Arrays;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.TimeUtility;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.screens.popups.AudioNotePopup;
import org.witness.iwitness.app.screens.popups.RenamePopup;
import org.witness.iwitness.app.screens.popups.TextareaPopup;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;
import org.witness.iwitness.utils.Constants.EditorActivityListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverviewFormFragment extends Fragment implements OnClickListener, ODKFormListener {
	View rootView, detailsView, topLevelAnnotationsView, tagsAndMessagesView;
	LinearLayout overviewFormRoot;

	Activity a;
	IForm textForm = null;
	IForm audioForm = null;

	FormUtility formUtility;

	TextView alias, dateCaptured, timeCaptured, location, messagesHolder, tagsHolder;
	EditText quickNotePrompt;
	ImageButton audioNotePrompt;

	IRegion overviewRegion;

	private final static String LOG = App.LOG;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_forms_overview, null);
		overviewFormRoot = (LinearLayout) rootView.findViewById(R.id.overview_form_root);
		overviewFormRoot.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		overviewFormRoot.setFocusableInTouchMode(true);

		detailsView = li.inflate(R.layout.forms_media_details, null);
		topLevelAnnotationsView = li.inflate(R.layout.forms_media_top_level_annotations, null);
		tagsAndMessagesView = li.inflate(R.layout.forms_media_tags_and_messages, null);

		alias = (TextView) detailsView.findViewById(R.id.media_details_alias);
		alias.setOnClickListener(this);

		dateCaptured = (TextView) detailsView.findViewById(R.id.media_date_captured);
		timeCaptured = (TextView) detailsView.findViewById(R.id.media_time_captured);
		location = (TextView) detailsView.findViewById(R.id.media_details_location);

		quickNotePrompt = (EditText) topLevelAnnotationsView.findViewById(R.id.media_quick_note_prompt);
		quickNotePrompt.setOnClickListener(this);

		audioNotePrompt = (ImageButton) topLevelAnnotationsView.findViewById(R.id.media_audio_note_prompt);
		audioNotePrompt.setOnClickListener(this);

		messagesHolder = (TextView) tagsAndMessagesView.findViewById(R.id.media_details_messages_holder);
		tagsHolder = (TextView) tagsAndMessagesView.findViewById(R.id.media_details_tags_holder);

		return rootView;
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initLayout();
	}

	@Override
	public void onDetach() {
		Log.d(LOG, "SHOULD SAVE FORM STATE!");


		super.onDetach();
	}

	private void initLayout() {
		overviewFormRoot.addView(detailsView);
		overviewFormRoot.addView(topLevelAnnotationsView);
		overviewFormRoot.addView(tagsAndMessagesView);

		initData();
		initForms();
	}

	private void initData() {
		int displayNumMessages = 0;
		if(((EditorActivityListener) a).media().messages != null && ((EditorActivityListener) a).media().messages.size() > 0) {
			displayNumMessages = ((EditorActivityListener) a).media().messages.size();
		}
		messagesHolder.setText(a.getResources().getString(R.string.x_messages, displayNumMessages, (displayNumMessages == 1 ? "" : "s")));

		String[] dateAndTime = TimeUtility.millisecondsToDatestampAndTimestamp(((EditorActivityListener) a).media().dcimEntry.timeCaptured);
		dateCaptured.setText(dateAndTime[0]);
		timeCaptured.setText(dateAndTime[1]);

		if(((EditorActivityListener) a).media().alias != null) {
			alias.setText(((EditorActivityListener) a).media().alias);
		}

		if(((EditorActivityListener) a).media().dcimEntry.exif.location != null && ((EditorActivityListener) a).media().dcimEntry.exif.location != new float[] {0.0f, 0.0f}) {
			location.setText(a.getString(R.string.x_location, ((EditorActivityListener) a).media().dcimEntry.exif.location[0], ((EditorActivityListener) a).media().dcimEntry.exif.location[1]));
		} else {
			location.setText(a.getString(R.string.location_unknown));
		}

	}

	private void initForms() {
		Logger.d(LOG, ((EditorActivityListener) a).media().asJson().toString());

		overviewRegion = ((EditorActivityListener) a).media().getTopLevelRegion();
		if(overviewRegion == null) {
			overviewRegion = ((EditorActivityListener) a).media().addRegion(a, null);
			
			for(IForm form : ((EditorActivity) a).availableForms) {
				if(form.namespace.equals(Forms.FreeText.TAG)) {
					textForm = new IForm(form, a);
					textForm.answerPath = new info.guardianproject.iocipher.File(((EditorActivityListener) a).media().rootFolder, "form_t" + System.currentTimeMillis()).getAbsolutePath();
					
					
					overviewRegion.addForm(textForm);
				}

				if(form.namespace.equals(Forms.FreeAudio.TAG)) {
					audioForm = new IForm(form, a);
					audioForm.answerPath = new info.guardianproject.iocipher.File(((EditorActivityListener) a).media().rootFolder, "form_a" + System.currentTimeMillis()).getAbsolutePath();
					
					overviewRegion.addForm(audioForm);
				}
			}
		} else {
			for(IForm form : ((EditorActivityListener) a).media().getForms(a)) {
				if(form.namespace.equals(Forms.FreeText.TAG)) {
					textForm = form;
				}
				
				if(form.namespace.equals(Forms.FreeAudio.TAG)) {
					audioForm = form;
				}
			}
		}
		
		textForm.associate(quickNotePrompt, Forms.FreeText.PROMPT);

		int displayNumTags = 0;
		if(((EditorActivityListener) a).media().associatedRegions != null && ((EditorActivityListener) a).media().associatedRegions.size() > 0) {
			displayNumTags = ((EditorActivityListener) a).media().getRegionsWithForms(Arrays.asList(new String[] {textForm.namespace, audioForm.namespace})).size();
		}
		tagsHolder.setText(a.getResources().getString(R.string.x_tags, displayNumTags, (displayNumTags == 1 ? "" : "s")));
	}

	public void cleanup() {

	}

	private void addQuickNote() {		
		new TextareaPopup(a, ((EditorActivityListener) a).media()) {
			@Override
			public void Show() {
				if(textForm.getQuestionDefByTitleId(Forms.FreeText.PROMPT).initialValue != null) {
					prompt.setText(textForm.getQuestionDefByTitleId(Forms.FreeText.PROMPT).initialValue);
				}

				super.Show();
			}
			
			@Override
			public void cancel() {
				super.cancel();
				quickNotePrompt.setText(this.prompt.getText().toString());
				textForm.answer(Forms.FreeText.PROMPT);
			}
		};
	}

	private void renameMedia() {
		new RenamePopup(a, ((EditorActivityListener) a).media()) {
			@Override
			public void cancel() {
				super.cancel();
				alias.setText(((EditorActivityListener) a).media().alias);
			}
		};
	}

	private void recordAudio() {
		new AudioNotePopup(a, audioForm) {

			@Override
			public void cancel() {
				audioForm.answer(Forms.FreeAudio.PROMPT);
				progress.shutDown();
				super.cancel();
			}
		};
	}

	@Override
	public void onClick(View v) {
		if(v == alias) {
			renameMedia();
		} else if(v == quickNotePrompt) {
			addQuickNote();
		} else if(v == audioNotePrompt) {
			recordAudio();
		}
	}

	@Override
	public boolean saveForm() {
		Log.d(LOG, "OK I AM SAVING FORM");

		try {
			textForm.save(new info.guardianproject.iocipher.FileOutputStream(textForm.answerPath));
		} catch (FileNotFoundException e) {
			Logger.e(LOG, e);
		}

		try {
			audioForm.save(new info.guardianproject.iocipher.FileOutputStream(audioForm.answerPath));
		} catch (FileNotFoundException e) {
			Logger.e(LOG, e);
		}

		return InformaCam.getInstance().mediaManifest.save();
	}
}

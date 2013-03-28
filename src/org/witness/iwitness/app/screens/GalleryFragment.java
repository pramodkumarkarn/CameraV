package org.witness.iwitness.app.screens;

import java.util.List;
import java.util.Vector;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.IMedia;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.HomeActivityListener;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.adapters.GalleryGridAdapter;
import org.witness.iwitness.utils.adapters.GalleryListAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

public class GalleryFragment extends Fragment implements OnItemSelectedListener, OnClickListener, OnItemClickListener, OnItemLongClickListener {
	View rootView;
	Spinner displayToggle, displaySort;
	ImageButton multiSelect, displaySortTrigger;
	Button displayToggleTrigger;

	GridView mediaDisplayGrid;
	GalleryGridAdapter galleryGridAdapter;

	ListView mediaDisplayList;
	GalleryListAdapter galleryListAdapter;
	RelativeLayout noMedia;

	Activity a;
	
	boolean isInMultiSelectMode;
	List<IMedia> batch;

	private static final String LOG = Home.LOG;	
	private InformaCam informaCam = InformaCam.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_home_gallery, null);
		displayToggle = (Spinner) rootView.findViewById(R.id.display_toggle);
		displaySort = (Spinner) rootView.findViewById(R.id.display_sort);
		
		displayToggleTrigger = (Button) rootView.findViewById(R.id.display_toggle_trigger);
		displayToggleTrigger.setOnClickListener(this);
		
		displaySortTrigger = (ImageButton) rootView.findViewById(R.id.display_sort_trigger);
		displaySortTrigger.setOnClickListener(this);
		
		multiSelect = (ImageButton) rootView.findViewById(R.id.multi_select);
		multiSelect.setOnClickListener(this);

		mediaDisplayGrid = (GridView) rootView.findViewById(R.id.media_display_grid);
		mediaDisplayList = (ListView) rootView.findViewById(R.id.media_display_list);
		
		noMedia = (RelativeLayout) rootView.findViewById(R.id.media_display_no_media);

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
		initLayout(savedInstanceState);	
	}

	public void initData() {
		try {
			if(informaCam.mediaManifest.media != null && informaCam.mediaManifest.media.size() > 0) {
				Log.d(LOG, "the manifest has " + informaCam.mediaManifest.media.size() + " entries now.");
				Log.d(LOG, informaCam.mediaManifest.asJson().toString());

				galleryGridAdapter = new GalleryGridAdapter(a, informaCam.mediaManifest.media);
				galleryListAdapter = new GalleryListAdapter(a, informaCam.mediaManifest.media);

				mediaDisplayGrid.setAdapter(galleryGridAdapter);
				mediaDisplayGrid.setOnItemLongClickListener(this);
				mediaDisplayGrid.setOnItemClickListener(this);
				
				mediaDisplayList.setAdapter(galleryListAdapter);
				mediaDisplayList.setOnItemLongClickListener(this);
				mediaDisplayList.setOnItemClickListener(this);
				
				noMedia.setVisibility(View.GONE);
			} else {
				noMedia.setVisibility(View.VISIBLE);
			}
		} catch(NullPointerException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
			
			noMedia.setVisibility(View.VISIBLE);
		}
	}

	public void updateData() {
		mediaDisplayGrid.removeAllViewsInLayout();
		mediaDisplayList.removeAllViewsInLayout();

		initData();
	}
	
	public void toggleMultiSelectMode(boolean mode) {
		isInMultiSelectMode = mode;
		toggleMultiSelectMode();
	}
	
	public void toggleMultiSelectMode() {
		multiSelect.setImageDrawable(a.getResources().getDrawable(isInMultiSelectMode ? R.drawable.ic_action_selected : R.drawable.ic_action_select));
		if(isInMultiSelectMode) {
			batch = new Vector<IMedia>();
		} else {
			batch = null;
			
		}
		initData();
		
	}

	private void initLayout(Bundle savedInstanceState) {
		ArrayAdapter<CharSequence> toggleAdapter = ArrayAdapter.createFromResource(a, R.array.view_options, android.R.layout.simple_spinner_item);
		ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(a, R.array.sort_options, android.R.layout.simple_spinner_item);

		toggleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		displayToggle.setAdapter(toggleAdapter);
		displayToggle.setOnItemSelectedListener(this);

		displaySort.setAdapter(sortAdapter);
		displaySort.setOnItemSelectedListener(this);

		mediaDisplayGrid.removeAllViewsInLayout();
		mediaDisplayList.removeAllViewsInLayout();

		initData();
		toggleMultiSelectMode(false);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		if(parent == displayToggle) {
			switch(pos) {
			case 0:
				mediaDisplayGrid.setVisibility(View.VISIBLE);
				mediaDisplayList.setVisibility(View.GONE);				
				break;
			case 1:
				mediaDisplayGrid.setVisibility(View.GONE);
				mediaDisplayList.setVisibility(View.VISIBLE);
				break;
			}
		} else if(parent == displaySort) {
			Log.d(LOG, "selecting " + pos + " from displaySort");
			informaCam.mediaManifest.sortBy(pos);
			updateData();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	@Override
	public void onClick(View v) {
		if(v == multiSelect) {
			if(isInMultiSelectMode) {
				isInMultiSelectMode = false;
			} else {
				isInMultiSelectMode = true;
			}
			
			toggleMultiSelectMode();
		} else if(v == displaySortTrigger) {
			displaySort.performClick();
		} else if(v == displayToggleTrigger) {
			displayToggle.performClick();
		}

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int viewId, long l) {
		((HomeActivityListener) a).getContextualMenuFor(((IMedia) informaCam.mediaManifest.media.get((int) l))._id);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int viewId, long l) {
		if(!isInMultiSelectMode) {
			((HomeActivityListener) a).launchEditor(((IMedia) informaCam.mediaManifest.media.get((int) l))._id);
		} else {
			batch.add((IMedia) informaCam.mediaManifest.media.get((int) l));
		}
		
	}
}

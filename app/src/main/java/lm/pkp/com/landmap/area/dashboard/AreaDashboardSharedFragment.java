package lm.pkp.com.landmap.area.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import lm.pkp.com.landmap.AreaDetailsActivity;
import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.res.disp.AreaItemAdaptor;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.sync.LocalDataRefresher;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaDashboardSharedFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shared_areas, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            loadFragment();
        }
    }

    private void loadFragment() {
        View view = getView();
        view.findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);

        ImageView refreshAreaView = (ImageView) getActivity().findViewById(R.id.action_area_refresh);
        refreshAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getView().findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                new LocalDataRefresher(getContext(), new DataReloadCallback()).refreshLocalData();
            }
        });

        final EditText inputSearch = (EditText) getActivity().findViewById(R.id.dashboard_search_box);
        inputSearch.addTextChangedListener(new UserInputWatcher());

        final Button seachClearButton = (Button) getActivity().findViewById(R.id.dashboard_search_clear);
        seachClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputSearch = (EditText) getView().findViewById(R.id.dashboard_search_box);
                inputSearch.setText("");
            }
        });

        final AreaDBHelper adh = new AreaDBHelper(view.getContext());
        ListView areaListView = (ListView) view.findViewById(R.id.area_display_list);
        AreaItemAdaptor adaptor = new AreaItemAdaptor(getContext(), R.layout.area_element_row, adh.getAreas("shared"));

        areaListView.setAdapter(adaptor);
        areaListView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
        areaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                AreaElement ae = (AreaElement) adapter.getItemAtPosition(position);
                AreaContext.getInstance().setAreaElement(ae, getContext());
                Intent intent = new Intent(getContext(), AreaDetailsActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.splash_panel).setVisibility(View.GONE);
    }

    private class DataReloadCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            final ArrayList<AreaElement> areas = new AreaDBHelper(getView().getContext()).getAreas("shared");

            ListView areaListView = (ListView) getView().findViewById(R.id.area_display_list);
            AreaItemAdaptor adaptor = new AreaItemAdaptor(getContext(), R.layout.area_element_row, areas);
            areaListView.setAdapter(adaptor);

            final EditText inputSearch = (EditText) getActivity().findViewById(R.id.dashboard_search_box);
            String filterStr = inputSearch.getText().toString().trim();
            if(!filterStr.equalsIgnoreCase("")){
                adaptor.getFilter().filter(filterStr);
            }

            getView().findViewById(R.id.splash_panel).setVisibility(View.INVISIBLE);
        }
    }

    private class UserInputWatcher implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            getView().findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);

            ListView areaListView = (ListView) getView().findViewById(R.id.area_display_list);
            final ArrayAdapter<AreaElement> adapter = (ArrayAdapter<AreaElement>) areaListView.getAdapter();
            adapter.getFilter().filter(editable.toString());

            getView().findViewById(R.id.splash_panel).setVisibility(View.GONE);
        }

    }

}
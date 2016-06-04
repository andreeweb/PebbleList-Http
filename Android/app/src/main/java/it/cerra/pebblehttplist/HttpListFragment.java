package it.cerra.pebblehttplist;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class HttpListFragment extends ListFragment {

    static final String TAG = "HttpListFragment";
    private ArrayList<PLUrl> list;
    private PLUrlAdapter adapter;
    private String uuid = "b3578af5-8a89-4a1d-9437-060a0b481c9e";

    public HttpListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_http_list, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        list = this.loadDataFromLocalFile();

        adapter = new PLUrlAdapter(getContext(), list);
        setListAdapter(adapter);

        // Handle actions on row
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog dialog = deleteDialog(position);
                dialog.show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.addItem);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (list.size() == 5){
                    Toast.makeText(getContext(), "In this demo app you cannot add more than 5 elements to the list", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(getContext(), AddUrlActivity.class);
                    startActivity(intent);
                }
            }
        });

        boolean isConnected = PebbleKit.isWatchConnected(getContext());
        TextView conneciton = (TextView) getActivity().findViewById(R.id.label_con);

        if (isConnected)
            conneciton.setText(R.string.status_ready);
        else
            conneciton.setText(R.string.status_not);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_update) {
            Log.d(TAG, "action update");
            this.sendUpdate();
            return true;
        }else if (id == R.id.action_open_app){
            Log.d(TAG, "action open app");
            this.openAppOnPebble();
            return true;
        }else if (id == R.id.action_push){
            Log.d(TAG, "action send push");
            this.sendPushToPebble();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void sendUpdate(){

        // Prepare data, we send always 5 elements to pebble
        final ArrayList<PLUrl> newList = new ArrayList<PLUrl>(list);
        int ele_to_add = 5 - newList.size();

        // Add empty elements
        for (int i = 0; i < ele_to_add; i++){
            PLUrl url = new PLUrl();
            url.setName("Empty");
            url.setUrl("--");
            newList.add(url);
        }

        Handler mHandler = new Handler();

        // Launch app
        this.openAppOnPebble();

        Toast.makeText(getContext(), "Sending data...", Toast.LENGTH_SHORT).show();

        // Send data 1s after launch
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

                for (PLUrl url : newList){
                    PebbleDictionary outgoing = new PebbleDictionary();
                    outgoing.addString(1, url.getName());
                    outgoing.addString(2, url.getUrl());
                    PebbleKit.sendDataToPebble(getActivity().getApplicationContext(), UUID.fromString(uuid), outgoing);
                }
            }

        }, 1000L);
    }

    void sendPushToPebble(){

        // Push a notification
        final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

        final Map data = new HashMap();
        data.put("title", "Test Message");
        data.put("body", "Whoever said nothing was impossible never tried to slam a revolving door.");
        final JSONObject jsonData = new JSONObject(data);
        final String notificationData = new JSONArray().put(jsonData).toString();

        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", "PebbleKit Android");
        i.putExtra("notificationData", notificationData);
        getActivity().sendBroadcast(i);

        Toast.makeText(getContext(), "Push sent", Toast.LENGTH_SHORT).show();
    }

    void openAppOnPebble(){

        // Open app on pebble
        PebbleKit.startAppOnPebble(getContext(), UUID.fromString(uuid));

        Toast.makeText(getContext(), "Launching...", Toast.LENGTH_SHORT).show();
    }

    private AlertDialog deleteDialog(final int position) {

        return new AlertDialog.Builder(getContext())
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Do you want to Delete this item?")

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("urls", Context.MODE_PRIVATE);
                        String strJson = sharedpreferences.getString("json_array","[]");

                        Log.d(TAG, strJson);

                        try {

                            JSONArray jsonArr = new JSONArray(strJson);

                            for (int i = 0; i < jsonArr.length(); ++i) {

                                JSONObject obj = jsonArr.getJSONObject(i);
                                String url = obj.getString("url");

                                PLUrl url_to_delete = list.get(position);
                                if(url_to_delete.getUrl().equals(url)){

                                    list.remove(position);
                                    adapter.notifyDataSetChanged();

                                    // json array remove is not available in
                                    // Android API 16 so we create a new json array from adapter data
                                    JSONArray newJsonArray = new JSONArray();
                                    for (PLUrl url_obj : list){
                                        JSONObject pnObj = new JSONObject();
                                        pnObj.put("name", url_obj.getName());
                                        pnObj.put("url", url_obj.getUrl());
                                        newJsonArray.put(pnObj);
                                    }


                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.putString("json_array", newJsonArray.toString());
                                    editor.apply();

                                    break;
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dialog.dismiss();
                    }

                })

                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

    }

    ArrayList<PLUrl> loadDataFromLocalFile(){

        ArrayList<PLUrl> tmp_array = new ArrayList<PLUrl>();;

        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("urls", Context.MODE_PRIVATE);
        String strJson = sharedpreferences.getString("json_array","[]");

        Log.d(TAG, strJson);

        try {

            JSONArray jsonArr = new JSONArray(strJson);

            for (int i = 0; i < jsonArr.length(); ++i) {

                JSONObject obj = jsonArr.getJSONObject(i);
                String name = obj.getString("name");
                String url = obj.getString("url");

                PLUrl tmp = new PLUrl();
                tmp.setName(name);
                tmp.setUrl(url);
                tmp_array.add(tmp);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tmp_array;

    }
}

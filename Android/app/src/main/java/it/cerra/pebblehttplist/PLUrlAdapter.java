package it.cerra.pebblehttplist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrea on 04/06/16.
 * PebbleHttp List
 */
public class PLUrlAdapter extends BaseAdapter {

    private ArrayList<PLUrl> listData = null;
    private Context context = null;

    public PLUrlAdapter(Context context,ArrayList<PLUrl> data) {

        this.listData = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.plurl_row, null);
        }

        PLUrl object = (PLUrl) getItem(position);

        // NAME
        TextView name = (TextView) convertView.findViewById(R.id.row_name);
        name.setText(object.getName());

        // URL
        TextView url = (TextView) convertView.findViewById(R.id.row_url);
        url.setText(object.getUrl());

        return convertView;
    }
}

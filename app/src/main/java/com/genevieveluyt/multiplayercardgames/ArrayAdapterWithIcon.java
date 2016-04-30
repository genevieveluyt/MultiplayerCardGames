package com.genevieveluyt.multiplayercardgames;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Genevieve on 4/29/2016.
 *
 * Source: http://stackoverflow.com/questions/8533394/icons-in-a-list-dialog
 */
public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

    private List<Integer> images;
    private Context context;

    public ArrayAdapterWithIcon(Context context, List<String> items, List<Integer> images) {
        super(context, android.R.layout.select_dialog_item, items);
        this.context = context;
        this.images = images;
    }

    public ArrayAdapterWithIcon(Context context, String[] items, Integer[] images) {
        super(context, android.R.layout.select_dialog_item, items);
        this.context = context;
        this.images = Arrays.asList(images);
    }

    public ArrayAdapterWithIcon(Context context,int itemArrayResourceId, int imagesArrayResourceId) {
        super(context, android.R.layout.select_dialog_item, context.getResources().getStringArray(itemArrayResourceId));
        this.context = context;
        TypedArray imagesTemp = context.getResources().obtainTypedArray(imagesArrayResourceId);
        Integer[] images = new Integer[imagesTemp.length()];
        for (int i = 0; i < imagesTemp.length(); i++)
            images[i] = imagesTemp.getResourceId(i, 0);
        this.images = Arrays.asList(images);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setTextAppearance(context, R.style.SubHeading);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(images.get(position), 0, 0, 0);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), 0, 0, 0);
        }
        textView.setCompoundDrawablePadding(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));

        return view;
    }

}

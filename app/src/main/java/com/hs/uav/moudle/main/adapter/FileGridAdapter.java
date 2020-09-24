package com.hs.uav.moudle.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hs.uav.R;
import com.hs.uav.common.entity.FileBean;
import com.hs.uav.common.view.DisplayUtil;

import java.util.List;

import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.Utils;
import me.goldze.mvvmhabit.utils.constant.Contact;

public class FileGridAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<FileBean> fileBeans;
    private Context con;
    public SingleLiveEvent<FileBean> fileBeanSingleLiveEvent = new SingleLiveEvent<>();

    public FileGridAdapter(Context context, List<FileBean> fileBeans) {
        this.inflater = LayoutInflater.from(context);
        this.con = context;
        this.fileBeans = fileBeans;
    }

    @Override
    public int getCount() {
        return fileBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = this.inflater.inflate(R.layout.item_pic_layout, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.file_image_iv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final FileBean fileBean = fileBeans.get(position);
        int width = (DisplayUtil.getScreenWidth() - Contact.dip2px(con,100)) / 5;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
        holder.iv.setLayoutParams(params);
        holder.iv.setOnClickListener(view -> {
            fileBeanSingleLiveEvent.setValue(fileBean);
        });
        return convertView;
    }

    private class ViewHolder {
        ImageView iv;
    }
}
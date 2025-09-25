package com.eam.rwtranslator.ui.main;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.eam.rwtranslator.R;
public class MainActListAdapter extends RecyclerView.Adapter<MainActListAdapter.ViewHolder> {
    private List<MainActData> mData;
    private Context context;
    private onItemClickListener clickListener;
    private onItemLongClickListener longclickListener;
    public MainActListAdapter(Context context) {
        this.mData=new ArrayList<>();
        this.context=context;
    }
    public interface onItemClickListener {
        void onItemClick(int position);
    }

    public interface onItemLongClickListener {
        boolean onItemLongClick(int position, View view);
    }

    public void setOnItemLongClickListener(onItemLongClickListener listener) {
        this.longclickListener = listener;
    }

    public void setOnItemClickListener(onItemClickListener listener) {
        this.clickListener = listener;
    }
    public MainActData getItem(int position) {
        return mData.get(position);
    }
        public void submitList(List<MainActData> newData) {
        mData = new ArrayList<>(newData);
        notifyDataSetChanged();
    }

    public List<MainActData> getCurrentList() {
        return new ArrayList<>(mData);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
        View view =
                LayoutInflater.from(arg0.getContext())
                        .inflate(R.layout.mainact_recyclerview_item, arg0, false);
        ViewHolder vh = new ViewHolder(view);
        view.setOnClickListener(v1 -> {
            if (clickListener != null) clickListener.onItemClick(vh.getAdapterPosition());
        });
        view.setOnLongClickListener(
                v2 -> longclickListener != null && longclickListener.onItemLongClick(vh.getAdapterPosition(), view));
        return vh;
        
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MainActData data = mData.get(position);
        holder.tv_title.setText(data.getProjectName());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
    
    // ViewHolder用于缓存item控件，便于后续扩展
    public static class ViewHolder extends RecyclerView.ViewHolder {
       TextView tv_title;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.mainact_recyclerview_item_title);
        }
    }
}

package com.thedeliveryapp.thedeliveryapp.recyclerview;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchUIUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.order_form.OrderForm;
import com.thedeliveryapp.thedeliveryapp.user.UserViewActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecyclerViewOrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {

    List<OrderData> list;
    Context context;
    String status;
    List<OrderData> pendingRemovalList;

    private static final int PENDING_REMOVAL_TIMEOUT = 5000; // 3sec
    private Handler handler = new Handler(); // handler for running delayed runnables
    private HashMap<OrderData, Runnable> pendingRunables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be

    public RecyclerViewOrderAdapter(List<OrderData> list, Context context) {
        this.list = list;
        this.context = context;
        pendingRemovalList = new ArrayList<>();
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_content, parent, false);
        OrderViewHolder holder = new OrderViewHolder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {

        
        final OrderData data = list.get(position);
 
        if (pendingRemovalList.contains(data)) {
            /** {show swipe layout} and {hide regular layout} */
            holder.cv.setVisibility(View.GONE);
            holder.isClickable = false;
            holder.swipeLayout.setVisibility(View.VISIBLE);
            holder.undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    undoOpt(data);
                }
            });
        } else {
            /** {show regular layout} and {hide swipe layout} */
            holder.cv.setVisibility(View.VISIBLE);
            holder.isClickable = true;
            holder.swipeLayout.setVisibility(View.GONE);
            //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
            status = String.valueOf(list.get(position).status.charAt(0));
            holder.category.setText(list.get(position).category);
            holder.description.setText(list.get(position).description);
            TextDrawable drawable = TextDrawable.builder().buildRound(status,Color.parseColor(getColor(status)));
            holder.imageView.setImageDrawable(drawable);
        }
        


    }
    private void undoOpt(OrderData delOrder) {
        Runnable pendingRemovalRunnable = pendingRunables.get(delOrder);
        pendingRunables.remove(delOrder);
        if (pendingRemovalRunnable != null)
            handler.removeCallbacks(pendingRemovalRunnable);
        pendingRemovalList.remove(delOrder);
        // this will rebind the row in "normal" state
        notifyItemChanged(list.indexOf(delOrder));
    }

    public void pendingRemoval(int position) {
 
        final OrderData data = list.get(position);
        if (!pendingRemovalList.contains(data)) {
            pendingRemovalList.add(data);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the data
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(list.indexOf(data));
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunables.put(data, pendingRemovalRunnable);
        }
    }
    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Insert a new item to the RecyclerView on a predefined position
    public void insert(int position, OrderData OrderData) {
        list.add(position, OrderData);
        notifyItemInserted(position);
    }

    // Remove a RecyclerView item containing a specified OrderData object
    public void remove(OrderData OrderData) {
        int position = list.indexOf(OrderData);
        list.remove(position);
        notifyItemRemoved(position);
    }

    public void remove(int position) {
        OrderData data = list.get(position);
        if (pendingRemovalList.contains(data)) {
            pendingRemovalList.remove(data);
        }
        if (list.contains(data)) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

    public boolean isPendingRemoval(int position) {
        OrderData data = list.get(position);
        return pendingRemovalList.contains(data);
    }

    String getColor(String st) {

        if(st.equals("P"))
            return "#ffa000";
        else if(st.equals("A"))
            return "#8bc34a";
        else if(st.equals("E"))
            return "#f44336";
        else
            return "#9e9e9e";

    }
 


}

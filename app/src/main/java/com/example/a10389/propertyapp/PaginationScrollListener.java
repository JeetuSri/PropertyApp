package com.example.a10389.propertyapp;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;

/**
 * Created by 10389 on 11/27/2017.
 */

public abstract class PaginationScrollListener implements AbsListView.OnScrollListener {

    private int visibleThreshold;
    private int currentPage=0;
    private int previousTotalItemCount=0;
    private boolean loading=true;
    private int startingPageIndex=0;


    public PaginationScrollListener() {
    }

    public PaginationScrollListener(int visibleThreshold, int startPage) {
        this.visibleThreshold = visibleThreshold;
        this.startingPageIndex = startPage;
        this.currentPage=startPage;
    }

    public PaginationScrollListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(totalItemCount < previousTotalItemCount){
            this.currentPage=this.startingPageIndex;
            this.previousTotalItemCount=totalItemCount;
            if(totalItemCount == 0){
                this.loading=true;
            }
        }

        if(loading && (totalItemCount > previousTotalItemCount)){
            loading=false;
            previousTotalItemCount=totalItemCount;
            currentPage++;
        }

        if(!loading && (firstVisibleItem+visibleItemCount+visibleThreshold) >= totalItemCount){
            loading=onLoadMore(currentPage+1,totalItemCount);
        }
    }

    protected abstract boolean onLoadMore(int i, int totalItemCount);

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }
}

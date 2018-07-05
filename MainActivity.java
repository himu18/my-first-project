package com.talent4assure.retrofit_get;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.SortedList;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.talent4assure.retrofit_get.Adapter.DataAdapter;
import com.talent4assure.retrofit_get.Interface.RequestInterface;
import com.talent4assure.retrofit_get.Model.Android;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
public static final String BASE_URL = "https://api.learn2crack.com/";

private RecyclerView mRecyclerView;

private CompositeDisposable mCompositeDisposable;

private DataAdapter mAdapter;

private ArrayList<Android> mAndroidArrayList;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCompositeDisposable = new CompositeDisposable();
        initRecyclerView();
        loadJSON();
        }

private void initRecyclerView() {

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        }

private void loadJSON() {

        RequestInterface requestInterface = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(RequestInterface.class);

        mCompositeDisposable.add(requestInterface.register()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(this::handleResponse,this::handleError));
        }

private void handleResponse(List<Android> androidList) {

        mAndroidArrayList = new ArrayList<>(androidList);
        mAdapter = new DataAdapter(mAndroidArrayList);
        mRecyclerView.setAdapter(mAdapter);
        }

private void handleError(Throwable error) {

        Toast.makeText(this, "Error "+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

@Override
public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
        }
        }

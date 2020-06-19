package com.coolweather.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.coolweather.android.TabAdapter.MyTabAdapter;
import com.coolweather.android.gson.News;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

public class NewsFragment extends Fragment {
    private FloatingActionButton fab;
    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<News.ResultBean.DataBean> list;
    private static final int UPNEWS_INSERT = 0;
    private int page =0,row =10;
    private static final int SELECT_REFLSH = 1;

    @SuppressLint("HandlerLeak")
    private final Handler newsHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String uniquekey, title, date, category, author_name, url, thumbnail_pic_s, thumbnail_pic_s02, thumbnail_pic_s03;
            switch (msg.what) {
                case UPNEWS_INSERT:
                    list = ((News) msg.obj).getResult().getData();
                    MyTabAdapter adapter = new MyTabAdapter(list, getActivity());
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
                case SELECT_REFLSH:
                    list = ((News) msg.obj).getResult().getData();
                    MyTabAdapter myTabAdapter = new MyTabAdapter(list, getActivity());
                    listView.setAdapter(myTabAdapter);
                    myTabAdapter.notifyDataSetChanged();
                    break;
                default:
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_item,container,false);
        listView = (ListView) view.findViewById(R.id.listView);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        return view;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onAttach(getActivity());
        //获取传递的值
        Bundle bundle = getArguments();
        final String data = bundle.getString("name","top");
        //置顶功能
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.smoothScrollToPosition(0);
            }
        });
        //下拉刷新
        swipeRefreshLayout.setColorSchemeResources(R.color.colorRed);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        // 下一步实现从数据库中读取数据刷新到listview适配器中
                    }
                },1000);
            }
        });
        //异步加载数据
        getDataFromNet(data);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获取点击条目的路径，传值显示webview页面
                String url = list.get(position).getUrl();
                String uniquekey = list.get(position).getUniquekey();
                final News.ResultBean.DataBean dataBean = (News.ResultBean.DataBean) list.get(position);
                Intent intent = new Intent(getActivity(),WebActivity.class);
                intent.putExtra("url",url);
                startActivity(intent);

            }
        });
    }
    private void getDataFromNet(final String data){
        @SuppressLint("StaticFieldLeak") AsyncTask<Void,Void,String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String path = "http://v.juhe.cn/toutiao/index?type="+data+"&key=3d874c2cfcd290ad915c2170a84834e5";
                URL url = null;
                try {
                    url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(5000);
                    connection.setConnectTimeout(5000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200){
                        InputStream inputStream = connection.getInputStream();
                        String json = streamToString(inputStream,"utf-8");
                        return json;
                    } else {
                        System.out.println(responseCode);
                        return "已达到今日访问次数上限";
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
            }
            protected void onPostExecute(final String result){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        News newsBean = new Gson().fromJson(result,News.class);
                        System.out.println(newsBean.getReason());
                        System.out.println(newsBean.getError_code());
                        if ("10012".equals(""+newsBean.getError_code())){
                            //数据库加载数据.....
                        }
                        Message msg=newsHandler.obtainMessage();
                        msg.what=UPNEWS_INSERT;
                        msg.obj = newsBean;
                        newsHandler.sendMessage(msg);
                    }
                }).start();
            }
            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }
        };
        task.execute();
    }

    private String streamToString(InputStream inputStream, String charset){
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,charset);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s = null;
            StringBuilder builder = new StringBuilder();
            while ((s = bufferedReader.readLine()) != null){
                builder.append(s);
            }
            bufferedReader.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}

package com.oss.android.Fragment.User;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.oss.android.GroupActivity;
import com.oss.android.GroupMakeActivity;
import com.oss.android.Model.GroupModel;
import com.oss.android.Model.Setting;
import com.oss.android.R;
import com.oss.android.Service.ViewHolder.GroupListViewHolder;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Button btn_search, btn_make;
    private EditText editText_serach;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<GroupModel> groupList;
    private Context mContext;

    public SearchListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_searchlist, container, false);
        editText_serach = (EditText) view.findViewById(R.id.user_searchlist_edittext_search);
        btn_search = (Button) view.findViewById(R.id.user_serachlist_btn_search);
        btn_make = (Button) view.findViewById(R.id.user_serachlist_btn_make);
        recyclerView = (RecyclerView) view.findViewById(R.id.user_searchlist_recycleview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        groupList = new ArrayList<>();
        mContext = getActivity().getApplicationContext();
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);

        btn_make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GroupMakeActivity.class);
                startActivity(intent);
            }
        });
        btn_search.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String searchText = editText_serach.getText().toString();
                new SearchGroup().execute(searchText);
            }
        });


        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            String[] params = new String[1];
            params[0] = Setting.getUrl() + "group/";

            HttpGetRequest myHttp = new HttpGetRequest();
            myHttp.execute(params);

        }
    }

    @Override
    public void onRefresh() {

    }

    private class MyAdapter extends RecyclerView.Adapter<GroupListViewHolder> {
        private Context context;
        private ArrayList<GroupModel> groupList;
        private int lastPosition = -1;

        public MyAdapter(Context context, ArrayList<GroupModel> groupList) {
            this.context = context;
            this.groupList = groupList;
        }

        @NonNull
        @Override
        public GroupListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
            GroupListViewHolder viewHolder = new GroupListViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull GroupListViewHolder viewHolder, final int i) {
            viewHolder.getTitle().setText(groupList.get(i).getTitle());
            viewHolder.getDescription().setText(groupList.get(i).getDescription());
            viewHolder.getNumPeople().setText(Integer.toString(groupList.get(i).getNumPeople()));
            viewHolder.getMaxNumPeople().setText(Integer.toString(groupList.get(i).getMaxNumPeolpe()));
            double [] groupTendency = groupList.get(i).getTendency();
            double [] userTendency = Setting.getUserTendency();

            double sum = 0;
            try {
                for (int j = 0; j < 6; j++) {
                    sum += Math.pow(groupTendency[j] - userTendency[j], 2);
                }
            } catch (Exception e){
                sum  = -1;
            }
            double error = sum / 24 * 100;
            int correct_rate = (int)(100 - error);
            viewHolder.getCorrect().setText(Integer.toString(correct_rate));
            for (int j = 0; j < viewHolder.getTag().length; j++) {
                viewHolder.getTag()[j].setText(groupList.get(i).getTags()[j]);
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(mContext, GroupActivity.class);
                    intent.putExtra("id", groupList.get(i).getId());
                    Setting.setGroupId(groupList.get(i).getId());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return groupList.size();
        }
    }

    private class HttpGetRequest extends AsyncTask<String, Void, JSONArray> {

        HttpURLConnection conn;

        String REQUEST_METHOD = "GET";
        int READ_TIMEOUT = 15000;
        int CONNECTION_TIMEOUT = 15000;

        @Override
        protected JSONArray doInBackground(String... strings) {
            String stringUrl = strings[0];
            JSONArray result = null;
            String inputLine;
            String stringResult;

            try {
                URL myUrl = new URL(stringUrl);
                conn = (HttpURLConnection) myUrl.openConnection();

                conn.setRequestMethod(REQUEST_METHOD);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);

                conn.connect();

                InputStreamReader streamReader = new InputStreamReader(conn.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();

                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }

                reader.close();
                streamReader.close();

                stringResult = stringBuilder.toString();
                result = new JSONArray(stringResult);

                Log.d("Url", stringUrl);
                Log.d("Result", result.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return result;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            groupList = new ArrayList<>();

            int id;
            String title;
            String description;
            int numPeople;
            int maxNumPeople;
            String[] tag = new String[Setting.NUM_OF_TAG];
            double rule, learning, numberPeople, friendship, environment, style;

            try {
                if(result != null) {
                    for (int i = 0; i < result.length(); i++) {
                        id = result.getJSONObject(i).getInt("id");
                        title = result.getJSONObject(i).getString("name");
                        description = result.getJSONObject(i).getString("description");
                        numPeople = result.getJSONObject(i).getInt("num_people");
                        maxNumPeople = result.getJSONObject(i).getInt("max_num_people");
                        for (int j = 0; j < tag.length; j++) {
                            tag[j] = " ";
                            String temp = result.getJSONObject(i).getString("tag" + Integer.toString(j + 1));
                            if (temp.equals("null")) {
                                tag[j] = " ";
                            } else
                                tag[j] = "# " + temp;
                        }
                        rule = result.getJSONObject(i).getDouble("rule");
                        learning = result.getJSONObject(i).getDouble("learning");
                        numberPeople = result.getJSONObject(i).getDouble("numberPeople");
                        friendship = result.getJSONObject(i).getDouble("friendship");
                        environment = result.getJSONObject(i).getDouble("environment");
                        style = result.getJSONObject(i).getDouble("style");
                        groupList.add(new GroupModel(id, title, description, numPeople, maxNumPeople, tag, rule, learning, numberPeople, friendship, environment, style));
                    }

                    adapter = new MyAdapter(mContext, groupList);
                    recyclerView.setAdapter(adapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class SearchGroup extends AsyncTask<String, Void, JSONArray>{

        HttpURLConnection conn = null;

        @Override
        protected JSONArray doInBackground(String... strings) {
            String searchText = strings[0];

            try {
                URL url = new URL(Setting.getUrl()+"group/search/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                JSONObject params = new JSONObject();
                params.accumulate("searchText", searchText);

                OutputStream os = conn.getOutputStream();
                os.write(params.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                conn.connect();
                int responseCode = conn.getResponseCode();

                if(responseCode == 404){
                    if(conn!=null)
                        conn.disconnect();
                    return null;
                }

                InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
                BufferedReader reader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String inputLine;
                while ((inputLine = reader.readLine())!=null){
                    stringBuilder.append(inputLine);
                }
                reader.close();
                inputStreamReader.close();
                conn.disconnect();

                String resultStr = stringBuilder.toString();
                JSONArray resultArray = new JSONArray(resultStr);
                return resultArray;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);

            double rule, learning, numberPeople, friendship, environment, style;

            if(result != null){
                groupList = null;
                groupList = new ArrayList<>();

                int id;
                String title;
                String description;
                int numPeople;
                int maxNumPeople;
                String[] tag = new String[Setting.NUM_OF_TAG];

                try {
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject group = result.getJSONObject(i);

                        id = result.getJSONObject(i).getInt("id");
                        title = result.getJSONObject(i).getString("name");
                        description = result.getJSONObject(i).getString("description");
                        numPeople = result.getJSONObject(i).getInt("num_people");
                        maxNumPeople = result.getJSONObject(i).getInt("max_num_people");
                        for (int j = 0; j < tag.length; j++) {
                            tag[j] = " ";
                            String temp = result.getJSONObject(i).getString("tag" + Integer.toString(j + 1));
                            if (temp.equals("")) {
                                tag[j] = " ";
                            } else
                                tag[j] = "# " + temp;
                        }
                        rule = result.getJSONObject(i).getDouble("rule");
                        learning = result.getJSONObject(i).getDouble("learning");
                        numberPeople = result.getJSONObject(i).getDouble("numberPeople");
                        friendship = result.getJSONObject(i).getDouble("friendship");
                        environment = result.getJSONObject(i).getDouble("environment");
                        style = result.getJSONObject(i).getDouble("style");
                        groupList.add(new GroupModel(id, title, description, numPeople, maxNumPeople, tag, rule, learning, numberPeople, friendship, environment, style));
                    }

                    adapter = new MyAdapter(mContext, groupList);
                    recyclerView.setAdapter(adapter);
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
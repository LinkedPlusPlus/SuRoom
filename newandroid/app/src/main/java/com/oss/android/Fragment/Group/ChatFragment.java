package com.oss.android.Fragment.Group;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.oss.android.Model.ChatModel;
import com.oss.android.Model.Setting;
import com.oss.android.R;
import com.oss.android.Service.Adapter.ChatListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private Button btn_submmit;
    private EditText content;
    private ListView listview;

    private ArrayList<ChatModel> chatModels;
    private ChatListAdapter chatListAdapter;
    private String user_id;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a hh:mm");

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        btn_submmit = (Button) view.findViewById(R.id.group_chat_btn_submit);
        content = (EditText) view.findViewById(R.id.group_chat_editText_content);
        listview = (ListView) view.findViewById(R.id.group_chat_listView);

        user_id = Integer.toString(Setting.getUserId());

        chatModels = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(chatModels);

        btn_submmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!content.getText().toString().equals("")) {
                    ChatModel chatModel = new ChatModel(user_id, Setting.getName(), content.getText().toString(), simpleDateFormat.format(new Date()), R.mipmap.ic_launcher);
                    databaseReference.child(Integer.toString(Setting.getGroupId())).push().setValue(chatModel);
                    content.setText("");
                }
            }
        });


        databaseReference.child(Integer.toString(Setting.getGroupId())).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                chatModels.add(dataSnapshot.getValue(ChatModel.class));
                listview.setAdapter(chatListAdapter);
                if(chatListAdapter != null)
                    listview.setSelection(chatListAdapter.getCount() - 1);// 자동스크롤링
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }

}

package com.example.blabber;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationsFragment extends Fragment {

    private RecyclerView conversationLog;

    private DatabaseReference conversationDatabase;
    private DatabaseReference messageDatabase;
    private DatabaseReference userDatabase;

    private FirebaseAuth auth;

    private String curr_user_id;

    private View mainView;

    public ConversationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_conversations, container, false);

        return mainView;
    }

}

package com.example.raula.agape;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;


public class TodoFragment extends Fragment {

    private FirebaseListAdapter<TodoModel> adapter;

    public TodoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_todo, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        displayToDo();
        FloatingActionButton fab =
                (FloatingActionButton)getView().findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)getView().findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseHelper.getTodoReference()
                        .push()
                        .setValue(new TodoModel(input.getText().toString()));
                // Clear the input
                input.setText("");
            }
        });
    }

    private void displayToDo() {

        ListView listOfTodo = (ListView) getView().findViewById(R.id.list_of_todos);

        adapter = new FirebaseListAdapter<TodoModel>(this.getActivity(), TodoModel.class,
                R.layout.todo, FirebaseHelper.getTodoReference()) {
            @Override
            protected void populateView(View v, TodoModel model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.todo_title);
                Button doneButton = (Button) v.findViewById(R.id.todo_delete);

                doneButton.setTag(this.getRef(position).getKey());

                // Set their text
                messageText.setText(model.getMessage());
            }
        };

        listOfTodo.setAdapter(adapter);
    }

    private void deleteTodo(View view) {
        String id = (String) view.getTag();

        FirebaseHelper.getTodoReference().child(id).removeValue();
    }

}

package com.trongquoc.chatchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;


public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessage> adapter;
    RelativeLayout activity_main;

    //Add Emojicon
    EmojiconEditText edtemoji;
    ImageView imgemoji,imgsend;
    EmojIconActions emojIconActions;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out)
        {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(activity_main,"You have been signed out.", Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                Snackbar.make(activity_main,"Successfully signed in.Welcome!", Snackbar.LENGTH_SHORT).show();
                displayChatMessage();
            }
            else{
                Snackbar.make(activity_main,"We couldn't sign you in.Please try again later", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = (RelativeLayout)findViewById(R.id.activity_main);

        //khỏi tạo
        imgemoji = (ImageView)findViewById(R.id.imagebuttonemoji);
        imgsend = (ImageView)findViewById(R.id.imagebuttonsend);
        edtemoji = (EmojiconEditText)findViewById(R.id.edittextemoji);


        emojIconActions = new EmojIconActions(getApplicationContext(),activity_main,imgemoji,edtemoji);
        emojIconActions.ShowEmojicon();

        //
        imgsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //thêm dữ liệu từ nút emoji
                FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(edtemoji.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                edtemoji.setText("");
                edtemoji.requestFocus();
            }
        });
  
        //Check neu khong có tk thì chuyển sang trang đăng ký
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),SIGN_IN_REQUEST_CODE);
        }
        else
            //có tài khoản dăng nhập bằng google smartlock dựa vào firebase
        {
            Snackbar.make(activity_main,"Welcome "+FirebaseAuth.getInstance().getCurrentUser().getEmail(),Snackbar.LENGTH_SHORT).show();
            //Load content
            displayChatMessage();
        }


    }



    private void displayChatMessage() {//phuong thuc hien tin nhan

        ListView listOfMessage = (ListView)findViewById(R.id.listviewmess);//listview hien tin nhan
        adapter = new FirebaseListAdapter<ChatMessage>
                (this,ChatMessage.class,R.layout.item_chat,
                        FirebaseDatabase.getInstance().getReference())
        {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {

                //Lay du lieu cho  itemchat.xml
                TextView messageText, messageUser, messageTime;
                messageText = (EmojiconTextView) v.findViewById(R.id.textviewemoji);
                messageUser = (TextView) v.findViewById(R.id.textviewuser);
                messageTime = (TextView) v.findViewById(R.id.textviewtime);

                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

            }
        };
        listOfMessage.setAdapter(adapter);
    }
}

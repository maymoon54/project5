package com.example.photodiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {

    DatabaseReference database;
    ArrayList<Content> arrayList;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        //데이터베이스 객체 가져오기
        database = FirebaseDatabase.getInstance().getReference("1day1shot"); //데이터베이스 객체 가져오기

        mAuth = FirebaseAuth.getInstance(); //구글로그인 인증 정보 가져오기
        if (mAuth.getCurrentUser() != null) {
            Log.d("구글 인증 아이디",mAuth.getUid());
            Log.d("구글 이메일주소",mAuth.getCurrentUser().getEmail());
            Log.d("사용자프로필 이미지",mAuth.getCurrentUser().getPhotoUrl().toString());
            Log.d("닉네임",mAuth.getCurrentUser().getDisplayName());

            database.orderByChild("id").equalTo(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot datas : snapshot.getChildren()){
                        String key = datas.getKey();
                        Log.d("현재유저가 쓴글 번호",key);
                        Content cuser = datas.getValue(Content.class);
                        arrayList.add(0, cuser); //역순정렬
                        Log.d("파이어베이스>>", "유저아이디로검색 " + cuser);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }


        //key값..글번호로 검색
        String del = "1";
        database.child(del).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Content content = snapshot.getValue(Content.class);
                Log.d("글번호로검색",""+content);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }
}

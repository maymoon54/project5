package com.example.photodiary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    DatabaseReference database;
    ArrayList<Content> arrayList;
    ImageView img;
    TextView text, name, date, sub;
    MyListAdapter adapter;
    ListView listView;
    int no;
    FloatingActionButton floatingActionButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //객체 가져오기
        database = FirebaseDatabase.getInstance().getReference("1day1shot");//디비
        mAuth = FirebaseAuth.getInstance();//인증

        //inputActivitiy로 이동
        //이동시 로그인 되어있는지 체크!
        floatingActionButton = findViewById(R.id.floatingActionButton);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                if (mAuth.getCurrentUser() != null) {
                    //구글 인증정보가 있다면 글작성 페이지로 이동
                    Intent intent = new Intent(getApplication(), InputActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    //구글 인증 정보가 없다면 로그인 페이지로 이동
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }

            }
        });


        //뷰 객체 지정
        listView = findViewById(R.id.listView);

        arrayList = new ArrayList<>();
        database.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //디비 데이터 가지고 오는 메서드
                arrayList.clear();
                Log.d("파이어베이스>>", "아래 자식 개수 " + snapshot.getChildrenCount());

                //vo에 넣어서 그것을 arraylist에 넣기!
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    //snapshot.getChildren() => 1day1shot 아래에 있는 목록을 다 가지고 온다
                    //DataSnapshot => for문으로 목록에 들어있는 데이터를 하나씩 꺼내줌.
                    //값들을 가지고 와서, Content (vo) 에 넣는다
                    //getValue(Content.class) => 해당하는 멤버변수명과 동일한 set메서드를 자동으로 부른다!
                    Content content = snapshot1.getValue(Content.class);
                    arrayList.add(0, content); //역순정렬
                    Log.d("파이어베이스>>", "하나씩 찍자 " + content);
                }

                adapter = new MyListAdapter(getApplicationContext());
                listView.setAdapter(adapter);
                registerForContextMenu(listView);
                adapter.notifyDataSetChanged();

                //아이템 하나하나에 대한 이벤트 설정
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        System.out.println(arrayList.get(position).getNo());
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(arrayList.get(position).getDate()); //제목은 날짜

                        //레이아웃 인플레이션
                        View dialogView = View.inflate(getApplicationContext(), R.layout.detail, null);
                        builder.setView(dialogView);
                        ImageView imageView = dialogView.findViewById(R.id.detail_img);
                        TextView textView = dialogView.findViewById(R.id.detail_text);
                        textView.setText(arrayList.get(position).getText());

                        Glide.with(view)
                                .load(arrayList.get(position).img)
                                .into(imageView);
                        builder.setNegativeButton("닫기", null);
                        builder.show();

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public class MyListAdapter extends BaseAdapter {
        Context context;

        public MyListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
        //반복되는 하나하나를 정의
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View oneView = View.inflate(getApplicationContext(), R.layout.one2, null);
            text = oneView.findViewById(R.id.text);
            sub = oneView.findViewById(R.id.sub);
            date = oneView.findViewById(R.id.date);
            img = oneView.findViewById(R.id.img);
            text.setText(arrayList.get(position).text);
            date.setText(arrayList.get(position).name + " · " + arrayList.get(position).date);
            Glide.with(context)
                    .load(arrayList.get(position).img)
                    .into(img);
            return oneView;
        }
    }
    //context 메뉴 (리스트뷰 position 정보 가져와야 한다)
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        no = info.position;
        String title = arrayList.get(no).getName()+ " 님 작성";
        menu.setHeaderTitle(title);
        getMenuInflater().inflate(R.menu.longclick, menu);
    }

    //context 메뉴 클릭했을때 (글삭제 / 수정)
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete:
                delete();
                break;
            case R.id.modify:
                modify();
                break;

        }
        return super.onContextItemSelected(item);
    }

    //타이틀바 옵션메뉴
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem menuItem = menu.findItem(R.id.menuIcon);
        mAuth = FirebaseAuth.getInstance();
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                return false;
            }
        });

        if (mAuth.getCurrentUser() != null) {
            //구글 인증정보가 있다면 프로필 사진을 보여주자!
            Log.d("구글 로그인", mAuth.getUid());
            Log.d("구글 이메일주소", mAuth.getCurrentUser().getEmail());
            Log.d("사용자프로필 이미지", mAuth.getCurrentUser().getPhotoUrl().toString());
            Log.d("닉네임", mAuth.getCurrentUser().getDisplayName());

            //글라이드 사용해서 icon에 덮어씌우기
            Glide.with(this)
                    .asBitmap()
                    .circleCrop()
                    .load(mAuth.getCurrentUser().getPhotoUrl().toString())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            menuItem.setIcon(new BitmapDrawable(getResources(), resource));
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }

        return super.onPrepareOptionsMenu(menu);
    }


    //옵션메뉴 클릭했을때 처리할 내용
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //로그아웃
        if (item.getItemId() == R.id.logout) {
            if (mAuth.getCurrentUser() != null) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "로그인 정보가 없습니다", Toast.LENGTH_LONG).show();
            }
        }

        //계정정보
        if (item.getItemId() == R.id.profile) {
            if (mAuth.getCurrentUser() != null) {
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "로그인 정보가 없습니다", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    //글삭제
    public void delete(){
            //작성자가 동일할때 삭제권한 있음
            if (mAuth.getUid().equals(arrayList.get(no).getId())) {
                //Toast.makeText(this, "디비의" + key +"번 글 삭제 해야함", Toast.LENGTH_LONG).show();
                database.child(arrayList.get(no).getNo()+"").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"삭제성공",Toast.LENGTH_SHORT).show();
                        arrayList.remove(no);
                        adapter.notifyDataSetChanged();

                        // Delete the file
                        FirebaseStorage storage = FirebaseStorage.getInstance("gs://photodiary-2420a.appspot.com"); //스토리지
                        final StorageReference storageRef = storage.getReference();
                        StorageReference desertRef = storageRef.child("images/" + arrayList.get(no).getFileName());
                        desertRef.delete();
                    }
                });
            } else {
                Toast.makeText(this, "삭제권한이 없어요!", Toast.LENGTH_LONG).show();
            }
        }

    //글수정
    public void modify(){
        if (mAuth.getUid().equals(arrayList.get(no).getId())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("글수정");

            //레이아웃 인플레이션
            View dialogView = View.inflate(getApplicationContext(), R.layout.dialog, null);
            builder.setView(dialogView);
            ImageView imageView = dialogView.findViewById(R.id.click_img);
            final EditText editText = dialogView.findViewById(R.id.click_text);
            editText.setText(arrayList.get(no).getText());
            Glide.with(getApplicationContext())
                    .load(arrayList.get(no).img)
                    .into(imageView);
            builder.setNegativeButton("닫기", null);
            builder.setPositiveButton("수정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //text수정하기
                    final String text2 = editText.getText().toString();
                    Map<String, Object> update = new HashMap<String, Object>();
                    update.put("text", text2);
                    database.child(arrayList.get(no).getNo()+"").updateChildren(update)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    arrayList.get(no).setText(text2.toString());
                                    adapter.notifyDataSetChanged();
                                }
                            });


                }
            });
            builder.show();
            } else {
            Toast.makeText(this, "수정권한이 없어요!", Toast.LENGTH_LONG).show();
        }
    }
}
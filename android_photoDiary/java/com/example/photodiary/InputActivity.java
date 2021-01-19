package com.example.photodiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class InputActivity extends AppCompatActivity {
    DatabaseReference database;

    ImageView imageView;
    Button button, button2;
    TextView textView;
    EditText editText;
    private FirebaseAuth mAuth;
    String fileName, id, name, img, date, text, storageUri;
    int no = 0; //게시물 번호 count하기 위한 변수

    private Uri filePath;
    Handler handler = new Handler();
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        database = FirebaseDatabase.getInstance().getReference("1day1shot"); //데이터베이스 객체 가져오기
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //글번호 구하기
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    int num = Integer.parseInt(snapshot1.getKey());
                    if (num != 0) {
                        no = Math.max(no, num);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mAuth = FirebaseAuth.getInstance(); //구글로그인 인증 정보 가져오기
        Log.d("구글 로그인", mAuth.getUid());
        Log.d("구글 이메일주소", mAuth.getCurrentUser().getEmail());
        Log.d("사용자프로필 이미지", mAuth.getCurrentUser().getPhotoUrl().toString());
        Log.d("닉네임", mAuth.getCurrentUser().getDisplayName());

        id = mAuth.getUid();
        name = mAuth.getCurrentUser().getDisplayName();

        //디비에 저장
        //아이디, 닉네임, 날짜, 내용, 이미지
        imageView = findViewById(R.id.imageView);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);

        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        textView.setText(format.format(c.getTime()));


        //이미지 선택하기
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);
            }
        });

        //업로드 버튼 눌렀을때
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(); //사진 스토리지에 업로드
                no++;
                text = editText.getText().toString();
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            }
        });

        //취소버튼 눌렀을때
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
        if (requestCode == 0 && resultCode == RESULT_OK) {
            filePath = data.getData();

            //배치해놓은 imageView에 set
            imageView.setImageURI(filePath);

        }else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
        }
    }

    //스토리지에 이미지 업로드
    private void uploadFile() {
        //업로드할 파일이 있으면 수행
        if (filePath != null) {
            //업로드 진행 Dialog 보이기
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드중...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //파일명을 만들자

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
            Date now = new Date();
            fileName = id + formatter.format(now) + ".png";

            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://photodiary-2420a.appspot.com").child("images/" + fileName);
            storageRef.putFile(filePath)
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                            uploadContent(); //이미지업로드 성공시 내용 디비저장
                        }
                    })
                    //실패시
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //진행중
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            //dialog에 진행률을 퍼센트로 출력해 준다
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        }

    }

    //디비저장
    private void uploadContent() {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://photodiary-2420a.appspot.com"); //스토리지
        final StorageReference storageRef = storage.getReference();
        storageRef.child("images/" + fileName).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri uri = task.getResult();
                img = uri.toString();
                Content content = new Content(no, id, name, text, img, fileName, date);
                database.child(String.valueOf(no)).setValue(content)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "저장을 완료했습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent1);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "저장을 실패했습니다.", Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        });

    }

}
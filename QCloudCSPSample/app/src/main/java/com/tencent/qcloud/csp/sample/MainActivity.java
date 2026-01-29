package com.tencent.qcloud.csp.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
//    private String[] fileSizeArray = {"5MB", "10MB", "20MB", "50MB", "80MB", "150MB", "400MB", "800MB", "1GB", "10GB"};
    private String[] fileSizeArray = {"5M", "10M", "20M", "50M", "80M", "150M", "400M", "800M", "1G", "10G"};
    public static String TAG = "MainActivity";
    private static Context context;
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1000;
    private final int OPEN_FILE_CODE = 10000;

    private RemoteStorage remoteStorage;

    private String appid = "1255000008";
    private String region = "wh";
    private String hostFormat = "${bucket}.yun.ccb.com";

    Switch partSwitch;
    EditText bucketName;
    EditText filePathEdt;
    EditText sliceSizeEdt;

    TaskFactory taskFactory;
    private String filePath;

    private String fileSizeType;
    private String[] picFilePaths;
    private String[] videoFilePaths;

    class MySelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Toast.makeText(MainActivity.this, "您选择的是：" + fileSizeArray[i], Toast.LENGTH_SHORT).show();
            fileSizeType = fileSizeArray[i];

            String picFolderPath = "/mnt/sdcard/picture/" + fileSizeArray[i];
            Log.d(TAG, "onItemSelected: picFolderPath = " + picFolderPath);
            picFilePaths = Utils.traverseFolder(new File(picFolderPath));
            Log.d(TAG, "onItemSelected: picFilePaths = " + Arrays.toString(picFilePaths));

            String videoFolderPath = "/mnt/sdcard/video/" + fileSizeArray[i];
            Log.d(TAG, "onItemSelected: videoFolderPath = " + videoFolderPath);
            videoFilePaths = Utils.traverseFolder(new File(videoFolderPath));
            Log.d(TAG, "onItemSelected: videoFilePaths = " + Arrays.toString(videoFilePaths));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private void initSpinner() {
        ArrayAdapter<String> starAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown, fileSizeArray);
        Spinner sp = findViewById(R.id.file_size_spinner);
        sp.setAdapter(starAdapter);
        sp.setSelection(0);
        sp.setOnItemSelectedListener(new MySelectedListener());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        remoteStorage = new RemoteStorage(this, appid, region, hostFormat);
        bucketName = findViewById(R.id.bucket_name);
        filePathEdt = findViewById(R.id.file_path);
        partSwitch = findViewById(R.id.switch_part);
        sliceSizeEdt = findViewById(R.id.slice_size);
        taskFactory = TaskFactory.getInstance();
        requestPermissions();
        initSpinner();
    }

    public void onGetServiceClick(View view) {
        taskFactory.createGetServiceTask(this, remoteStorage).execute();
    }

    public void onPutBucketClick(View view) {
        String bucketNameText = bucketName.getText().toString();
        if (!TextUtils.isEmpty(bucketNameText)) {
            taskFactory.createPutBucketTask(this, remoteStorage, bucketNameText).execute();
        }
    }

    public void onPutObjectClick(View view) {
        openFileSelector();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, "您必须允许读取外部存储权限，否则无法上传文件", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_FILE_CODE && resultCode == Activity.RESULT_OK) {
            filePath = FilePathHelper.getPath(this, data.getData());
            String bucketNameText = bucketName.getText().toString();
            if (!TextUtils.isEmpty(bucketNameText)) {
                taskFactory.createPutObjectTask(this, remoteStorage, bucketNameText, filePath, filePath).execute();
            }
        }
    }

    private void openFileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_FILE_CODE);
    }

    public void picUpload(View view) {
        if (partSwitch.isChecked()) {
            multiUpload(false, true);
        } else {
            simpleUpload(false, true);
        }
    }

    public void videoUpload(View view) {
        if (partSwitch.isChecked()) {
            multiUpload(true, true);
        } else {
            simpleUpload(true, true);
        }
    }

    public void simpleUpload(boolean isVideo, boolean isMultiFile) {
        String bucketNameText = bucketName.getText().toString();
        filePath = bucketName.getText().toString();
        filePath = "/mnt/sdcard/test_gif.gif";
        Log.d(TAG, "MainActivity, simpleUpload: bucketNameText = " + bucketNameText + ", filePath = " + filePath);
        if (!TextUtils.isEmpty(bucketNameText) && !TextUtils.isEmpty(filePath)) {
            taskFactory.createSimplePutObjectTask(this, remoteStorage, bucketNameText, filePath, filePath).execute();
        }
    }

    public void multiUpload(boolean isVideo, boolean isMultiFile) {
        String sliceSizeTxt = sliceSizeEdt.getText().toString();
        if (!TextUtils.isEmpty(sliceSizeTxt)) {
            int sliceSize = Integer.parseInt(sliceSizeTxt);
            RemoteStorage.setSliceSize(sliceSize);
        }
        Log.d(TAG, "MainActivity, multiUpload: getSliceSize = " + RemoteStorage.getSliceSize());
        String bucketNameText = bucketName.getText().toString();
        filePath = bucketName.getText().toString();
        filePath = "/mnt/sdcard/test_gif.gif";
        Log.d(TAG, "MainActivity, multiUpload: bucketNameText = " + bucketNameText + ", filePath = " + filePath);
        if (!TextUtils.isEmpty(bucketNameText) && !TextUtils.isEmpty(filePath)) {
            taskFactory.createSimplePutObjectTask(this, remoteStorage, bucketNameText, filePath, filePath).execute();
        }
    }
}
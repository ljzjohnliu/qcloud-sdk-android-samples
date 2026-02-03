package com.tencent.qcloud.csp.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.cos.xml.model.object.PutObjectResult;
import com.tencent.cos.xml.transfer.UploadService;

import java.io.File;
import java.util.Arrays;

public class MainActivity2 extends AppCompatActivity {
//    private String[] fileSizeArray = {"5MB", "10MB", "20MB", "50MB", "80MB", "150MB", "400MB", "800MB", "1GB", "10GB"};
    private String[] fileSizeArray = {"5M", "10M", "20M", "50M", "80M", "150M", "400M", "800M", "1G", "10G"};
    public static String TAG = "MainActivity";
    private static Context context;
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1000;
    private final int OPEN_FILE_CODE = 10000;

    private static String bucketName = "vodimagex-uploadtest-1301729325";
    private RemoteStorage remoteStorage;

    private String appid = "1255000008";
    private String region = "ap-beijing";
//    private String hostFormat = "${bucket}.yun.ccb.com";
//    private String hostFormat = "https://vodimagex-uploadtest-1301729325.cos.ap-beijing.myqcloud.com";
//    private String hostFormat = "vodimagex-uploadtest-1301729325.yun.ccb.com";
    private String hostFormat = "vodimagex-uploadtest-1301729325.cos.ap-beijing.myqcloud.com";

    Switch partSwitch;
    EditText bucketNameEdt;
    EditText filePathEdt;
    EditText sliceSizeEdt;
    TextView resultTv;

    TaskFactory taskFactory;
    private String filePath;

    private String fileSizeType;
    private String[] picFilePaths;
    private String[] videoFilePaths;

    private static long totalCostTime;
    private static int totalCount;
    private static int sucCount;

    public void updateResult(String fileSizeType) {
        String result = String.format("测试资源是：%s，上传平均耗时：， 成功率：", fileSizeType);
        resultTv.setText(result);
    }

    public void updateResult(final String fileSizeType, final long costTime, final int position, final int total) {
        resultTv.post(new Runnable() {
            @Override
            public void run() {
                String result = String.format("测试资源是：%s，上传平均耗时:%d， 成功率：%d / %d", fileSizeType, costTime, position, total);
                Log.d(TAG, "updateResult: ******* result = " + result);
                resultTv.setText(result);
            }
        });
    }

    class MySelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//            Toast.makeText(MainActivity.this, "您选择的是：" + fileSizeArray[i], Toast.LENGTH_SHORT).show();
            fileSizeType = fileSizeArray[i];

            String picFolderPath = "/mnt/sdcard/picture/" + fileSizeArray[i];
//            Log.d(TAG, "onItemSelected: picFolderPath = " + picFolderPath);
            picFilePaths = Utils.traverseFolder(new File(picFolderPath));
            Log.d(TAG, "onItemSelected: picFilePaths = " + Arrays.toString(picFilePaths));

            String videoFolderPath = "/mnt/sdcard/video/" + fileSizeArray[i];
//            Log.d(TAG, "onItemSelected: videoFolderPath = " + videoFolderPath);
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
        setContentView(R.layout.activity_main2);
        context = this;
        remoteStorage = new RemoteStorage(this, appid, region, hostFormat);
        bucketNameEdt = findViewById(R.id.bucket_name);
        filePathEdt = findViewById(R.id.file_path);
        partSwitch = findViewById(R.id.switch_part);
        sliceSizeEdt = findViewById(R.id.slice_size);
        resultTv = findViewById(R.id.upload_result);
        taskFactory = TaskFactory.getInstance();
        requestPermissions();
        initSpinner();
    }

    public void onGetServiceClick(View view) {
        taskFactory.createGetServiceTask(this, remoteStorage).execute();
    }

    public void onPutBucketClick(View view) {
        String bucketNameText = bucketNameEdt.getText().toString();
        Log.d(TAG, "onPutBucketClick: bucketNameText = " + bucketNameText);
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
            String bucketNameText = bucketNameEdt.getText().toString();
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
        Log.d(TAG, "picUpload: -----partSwitch.isChecked() = " + partSwitch.isChecked());
        if (partSwitch.isChecked()) {
            multiUpload(false, true);
        } else {
            simpleUpload(false, true);
        }
    }

    public void videoUpload(View view) {
        Log.d(TAG, "videoUpload: -----partSwitch.isChecked() = " + partSwitch.isChecked());
        if (partSwitch.isChecked()) {
            multiUpload(true, true);
        } else {
            simpleUpload(true, true);
        }
    }

    public void simpleUpload(boolean isVideo, boolean isMultiFile) {
        sucCount = 0;
        totalCount = isVideo ? videoFilePaths.length : picFilePaths.length;
        totalCostTime = 0;
        updateResult((isVideo ? "视频":"图片") + fileSizeType, 0, 0, isMultiFile ? totalCount : 1);
        String bucketNameText = bucketNameEdt.getText().toString();
        Log.d(TAG, "simpleUpload: bucketNameText = " + bucketNameText);
        if (TextUtils.isEmpty(bucketNameText)) {
            return;
        }
        if (isMultiFile) {
            String[] filePaths = isVideo ? videoFilePaths : picFilePaths;
            if (filePaths == null || filePaths.length == 0) {
                Toast.makeText(MainActivity2.this, "filePaths is null!", Toast.LENGTH_SHORT).show();
                return;
            }
            for (int i = 0; i < filePaths.length; i++) {
                Log.d(TAG, "MainActivity, simpleUpload: bucketNameText = " + bucketNameText + ", filePaths[" + i + "] = " + filePaths[i]);
                final String path = filePaths[i];
                File file = new File(path);
                String fileName = file.getName();
                if (!TextUtils.isEmpty(filePaths[i])) {
                    taskFactory.createSimplePutObjectTask(this, remoteStorage, bucketNameText, path, (isVideo ? "video/":"picture/") + fileName, new TaskFactory.SimplePutFileCallBack() {
                        @Override
                        public void onResult(PutObjectResult putObjectResult, long costTime) {
                            Log.d(TAG, "simpleUpload, onResult: path = " + path + ", putObjectResult = " + putObjectResult + ", httpCode = " + (putObjectResult != null ? "" + putObjectResult.httpCode : "0")
                                    + ", costTime = " + costTime);
                            if (putObjectResult != null && putObjectResult.httpCode == 200) {
                                sucCount++;
                                totalCostTime += costTime;
                                long averageCost = totalCostTime / sucCount;
                                Log.d(TAG, "simpleUpload onResult: costTime = " + costTime + ", sucCount = " + sucCount + ", totalCostTime = " + totalCostTime + ", averageCost = " + averageCost + ", totalCount = " + totalCount);
                                updateResult((isVideo ? "视频":"图片") + fileSizeType, averageCost, sucCount, totalCount);
                            }
                        }
                    }).execute();
                }
            }
        } else {
            filePath = filePathEdt.getText().toString();
            filePath = "/mnt/sdcard/test_gif.gif";
            File file = new File(filePath);
            String fileName = file.getName();
            Log.d(TAG, "MainActivity, simpleUpload: bucketNameText = " + bucketNameText + ", filePath = " + filePath + "， fileName = " + fileName);
            if (!TextUtils.isEmpty(bucketNameText) && !TextUtils.isEmpty(filePath)) {
                taskFactory.createSimplePutObjectTask(this, remoteStorage, bucketNameText, filePath, (isVideo ? "video/":"picture/") + fileName, new TaskFactory.SimplePutFileCallBack() {
                    @Override
                    public void onResult(PutObjectResult putObjectResult, long costTime) {
                        Log.d(TAG, "simpleUpload, onResult: filePath = " + filePath + ", putObjectResult = " + putObjectResult + ", costTime = " + costTime);
                        if (putObjectResult != null && putObjectResult.httpCode == 200) {
                            updateResult(isVideo ? "单视频":"单图片", costTime, 1, 1);
                        } else {
                            updateResult(isVideo ? "单视频":"单图片", costTime, 0, 1);
                        }
                    }
                }).execute();
            }
        }
    }

    public void executeTaskByOrder(final boolean isVideo, final String[] filePaths, int position) {
        if (position >= filePaths.length)
            return;
        final String uploadFilePath = filePaths[position];
        File file = new File(uploadFilePath);
        String fileName = file.getName();
        final int nextPos = position + 1;
        Log.d(TAG, "executeTaskByOrder: position = " + position + ", uploadFilePath = " + uploadFilePath + "， fileName = " + fileName + ", nextPos = " + nextPos);

        if (!TextUtils.isEmpty(uploadFilePath)) {
            taskFactory.createPutObjectTask(this, remoteStorage, bucketName, uploadFilePath, (isVideo ? "video/":"picture/") + fileName, new TaskFactory.PutFileCallBack() {
                @Override
                public void onResult(UploadService.UploadServiceResult uploadServiceResult, long costTime) {
                    Log.d(TAG, "multiUpload, onResult: path = " + uploadFilePath + ", uploadServiceResult = " + uploadServiceResult  + ", httpCode = " + (uploadServiceResult != null ? "" + uploadServiceResult.httpCode : "0")
                            + ", costTime = " + costTime);
                    if (uploadServiceResult != null && uploadServiceResult.httpCode == 200) {
                        sucCount++;
                        totalCostTime += costTime;
                        long averageCost = totalCostTime / sucCount;
                        Log.d(TAG, "multiUpload onResult: costTime = " + costTime + ", sucCount = " + sucCount + ", totalCostTime = " + totalCostTime + ", averageCost = " + averageCost + ", totalCount = " + totalCount);
                        updateResult((isVideo ? "视频":"图片") + fileSizeType, averageCost, sucCount, totalCount);
                    }
                    executeTaskByOrder(isVideo, isVideo ? videoFilePaths : picFilePaths, nextPos);
                }
            }).execute();
        }
    }

    public void multiUpload(boolean isVideo, boolean isMultiFile) {
        sucCount = 0;
        totalCount = isVideo ? videoFilePaths.length : picFilePaths.length;
        totalCostTime = 0;
        updateResult((isVideo ? "视频":"图片") + fileSizeType, 0, 0, isMultiFile ? totalCount : 1);
        String bucketNameText = bucketNameEdt.getText().toString();
        Log.d(TAG, "multiUpload: ----1111----bucketNameText = " + bucketNameText);
        if (TextUtils.isEmpty(bucketNameText)) {
            return;
        }
//        isMultiFile = false;
        String sliceSizeTxt = sliceSizeEdt.getText().toString();
        Log.d(TAG, "multiUpload: -----sliceSizeTxt = " + sliceSizeTxt);
        if (!TextUtils.isEmpty(sliceSizeTxt)) {
            int sliceSize = Integer.parseInt(sliceSizeTxt);
            RemoteStorage.setSliceSize(sliceSize);
        }
        Log.d(TAG, "MainActivity, multiUpload: getSliceSize = " + RemoteStorage.getSliceSize());

        if (isMultiFile) {
            String[] filePaths = isVideo ? videoFilePaths : picFilePaths;
            if (filePaths == null) {
                return;
            }
            executeTaskByOrder(isVideo, filePaths, 0);
        } else {
            filePath = filePathEdt.getText().toString();
            filePath = "/mnt/sdcard/test_gif.gif";
            File file = new File(filePath);
            String fileName = file.getName();
            Log.d(TAG, "MainActivity, multiUpload: ----3333----bucketNameText = " + bucketNameText + ", filePath = " + filePath + "， fileName = " + fileName);
            if (!TextUtils.isEmpty(filePath)) {
                taskFactory.createPutObjectTask(this, remoteStorage, bucketNameText, filePath, (isVideo ? "video/":"picture/") + fileName, new TaskFactory.PutFileCallBack() {
                    @Override
                    public void onResult(UploadService.UploadServiceResult uploadServiceResult, long costTime) {
                        Log.d(TAG, "multiUpload, onResult: filePath = " + filePath + ", uploadServiceResult = " + uploadServiceResult + ", costTime = " + costTime);
                        if (uploadServiceResult != null && uploadServiceResult.httpCode == 200) {
                            updateResult(isVideo ? "单视频":"单图片", costTime, 1, 1);
                        }
                    }
                }).execute();
            }
        }
    }
}
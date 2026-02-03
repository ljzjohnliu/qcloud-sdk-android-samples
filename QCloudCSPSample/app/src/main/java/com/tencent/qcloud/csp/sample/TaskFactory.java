package com.tencent.qcloud.csp.sample;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.model.bucket.PutBucketResult;
import com.tencent.cos.xml.model.object.PutObjectResult;
import com.tencent.cos.xml.model.service.GetServiceResult;
import com.tencent.cos.xml.model.tag.ListAllMyBuckets;
import com.tencent.cos.xml.transfer.UploadService;
import com.tencent.qcloud.core.logger.QCloudLogger;

import java.util.List;

/**
 * Created by rickenwang on 2018/9/18.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class TaskFactory {

    public static final String TAG = "TaskFactory";
    private static TaskFactory instance;

    private TaskFactory() {}

    public static TaskFactory getInstance() {

        if (instance == null) {
            synchronized (TaskFactory.class) {
                if (instance == null) {
                    instance = new TaskFactory();
                }
            }
        }

        return instance;
    }

    public GetServiceTask createGetServiceTask(Context context, RemoteStorage remoteStorage) {

        return new GetServiceTask(context, remoteStorage);
    }

    public PutBucketTask createPutBucketTask(Context context, RemoteStorage remoteStorage, String bucketName) {

        return new PutBucketTask(context, remoteStorage, bucketName);
    }

    public PutObjectTask createPutObjectTask(Context context, RemoteStorage remoteStorage, String bucket,
                                             String srcPath, String dstPath) {

        return new PutObjectTask(context, remoteStorage, bucket, srcPath, dstPath);
    }

    public PutObjectTask createPutObjectTask(Context context, RemoteStorage remoteStorage, String bucket,
                                             String srcPath, String dstPath, PutFileCallBack putFileCallBack) {

        return new PutObjectTask(context, remoteStorage, bucket, srcPath, dstPath, putFileCallBack);
    }

    public SimplePutObjectTask createSimplePutObjectTask(Context context, RemoteStorage remoteStorage, String bucket,
                                             String srcPath, String dstPath) {

        return new SimplePutObjectTask(context, remoteStorage, bucket, srcPath, dstPath);
    }

    public SimplePutObjectTask createSimplePutObjectTask(Context context, RemoteStorage remoteStorage, String bucket,
                                                         String srcPath, String dstPath, SimplePutFileCallBack simplePutFileCallBack) {

        return new SimplePutObjectTask(context, remoteStorage, bucket, srcPath, dstPath, simplePutFileCallBack);
    }

    public class GetServiceTask extends AsyncTask<Void, Void, GetServiceResult> {

        Context context;
        RemoteStorage remoteStorage ;

        public GetServiceTask(Context context, RemoteStorage remoteStorage) {
            this.remoteStorage = remoteStorage;
            this.context = context;
            Log.d(TAG, "GetServiceTask: remoteStorage = " + remoteStorage);
        }

        @Override
        protected GetServiceResult doInBackground(Void ... voids) {
            try {
                Log.d(TAG, "doInBackground: ***********");
                return remoteStorage.getService();
            } catch (CosXmlServiceException e) {
                Log.d(TAG, "doInBackground: CosXmlServiceException = " + e);
                e.printStackTrace();
            } catch (CosXmlClientException e) {
                Log.d(TAG, "doInBackground: CosXmlClientException = " + e);
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(GetServiceResult getServiceResult) {

            if (getServiceResult != null && getServiceResult.listAllMyBuckets != null) {
                List<ListAllMyBuckets.Bucket> buckets = getServiceResult.listAllMyBuckets.buckets;
                Log.d(TAG, "onPostExecute: ---------------buckets.toString() = " + buckets.toString());
                Toast.makeText(context, buckets.toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "GetService failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class PutBucketTask extends AsyncTask<Void, Void, PutBucketResult> {

        RemoteStorage remoteStorage ;
        String bucketName;
        Context context;

        public PutBucketTask(Context context, RemoteStorage remoteStorage, String bucketName) {
            this.context = context;
            this.remoteStorage = remoteStorage;
            this.bucketName = bucketName;
        }

        @Override
        protected PutBucketResult doInBackground(Void ... voids) {
            try {
                return remoteStorage.putBucket(bucketName);
            } catch (CosXmlServiceException e) {
                e.printStackTrace();
            } catch (CosXmlClientException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(PutBucketResult putBucketResult) {

            if (putBucketResult != null) {
                Toast.makeText(context, putBucketResult.printResult(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    interface PutFileCallBack {
        void onResult(UploadService.UploadServiceResult uploadServiceResult, long costTime);
    }

    static class PutObjectTask extends AsyncTask<Void, Integer, UploadService.UploadServiceResult> {
        Context context;
        RemoteStorage remoteStorage;
        String bucket;
        String srcPath;
        String dstPath;
        long startTime;
        PutFileCallBack putFileCallBack;

        public PutObjectTask(Context context, RemoteStorage remoteStorage, String bucket, String srcPath, String dstPath) {
            this.context = context;
            this.remoteStorage = remoteStorage;
            this.bucket = bucket;
            this.srcPath = srcPath;
            this.dstPath = dstPath;
        }

        public PutObjectTask(Context context, RemoteStorage remoteStorage, String bucket, String srcPath, String dstPath, PutFileCallBack putFileCallBack) {
            this.context = context;
            this.remoteStorage = remoteStorage;
            this.bucket = bucket;
            this.srcPath = srcPath;
            this.dstPath = dstPath;
            this.putFileCallBack = putFileCallBack;
        }

        @Override
        protected UploadService.UploadServiceResult doInBackground(Void... voids) {
            try {
                startTime = System.currentTimeMillis();
                Log.d(TAG, "PutObjectTask, doInBackground: ------startTime = " + startTime);
                return remoteStorage.uploadFile(bucket, dstPath, srcPath, new CosXmlProgressListener() {
                    @Override
                    public void onProgress(long progress, long total) {
                        publishProgress((int) ((progress/ (float) total) * 100));
                    }
                });
            } catch (CosXmlServiceException e) {
                Log.d(TAG, "PutObjectTask, doInBackground: CosXmlServiceException = " + e);
                e.printStackTrace();
            } catch (CosXmlClientException e) {
                Log.d(TAG, "PutObjectTask, doInBackground: CosXmlClientException = " + e);
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            QCloudLogger.i("upload", "progress " + values[0]);
            Log.d(TAG, "PutObjectTask, onProgressUpdate: progress " + values[0]);
        }


        @Override
        protected void onPostExecute(UploadService.UploadServiceResult uploadServiceResult) {
            long costTime = System.currentTimeMillis() - startTime;
            Log.d(TAG, "PutObjectTask, upload cost time: " + costTime);
            if (uploadServiceResult != null) {
                Toast.makeText(context, uploadServiceResult.printResult(), Toast.LENGTH_SHORT).show();
            }
            if (putFileCallBack != null) {
                putFileCallBack.onResult(uploadServiceResult, costTime);
            }
        }
    }

    interface SimplePutFileCallBack {
        void onResult(PutObjectResult putObjectResult, long costTime);
    }

    static class SimplePutObjectTask extends AsyncTask<Void, Integer, PutObjectResult> {
        Context context;
        RemoteStorage remoteStorage;
        String bucket;
        String srcPath;
        String dstPath;
        long startTime;
        SimplePutFileCallBack simplePutFileCallBack;

        public SimplePutObjectTask(Context context, RemoteStorage remoteStorage, String bucket, String srcPath, String dstPath) {
            this.context = context;
            this.remoteStorage = remoteStorage;
            this.bucket = bucket;
            this.srcPath = srcPath;
            this.dstPath = dstPath;
        }

        public SimplePutObjectTask(Context context, RemoteStorage remoteStorage, String bucket, String srcPath, String dstPath, SimplePutFileCallBack simplePutFileCallBack) {
            this.context = context;
            this.remoteStorage = remoteStorage;
            this.bucket = bucket;
            this.srcPath = srcPath;
            this.dstPath = dstPath;
            this.simplePutFileCallBack = simplePutFileCallBack;
        }

        @Override
        protected PutObjectResult doInBackground(Void... voids) {
            try {
                startTime = System.currentTimeMillis();
                Log.d(TAG, "doInBackground: ------bucket = " + bucket + ", dstPath = " + dstPath + ", srcPath = " + srcPath);
                return remoteStorage.simpleUploadFile(bucket, dstPath, srcPath, new CosXmlProgressListener() {
                    @Override
                    public void onProgress(long progress, long total) {
                        publishProgress((int) ((progress/ (float) total) * 100));
//                        Log.d(TAG, "onProgress: -------- progress = " + (int) ((progress/ (float) total) * 100));
                    }
                });
            } catch (CosXmlServiceException e) {
                Log.d(TAG, "doInBackground: CosXmlServiceException = " + e);
                e.printStackTrace();
            } catch (CosXmlClientException e) {
                Log.d(TAG, "doInBackground: CosXmlClientException = " + e);
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            QCloudLogger.i("upload", "progress " + values[0]);
//            Log.d(TAG, "SimplePutObjectTask, onProgressUpdate: progress " + values[0]);
        }


        @Override
        protected void onPostExecute(PutObjectResult putObjectResult) {
            long costTime = System.currentTimeMillis() - startTime;
            Log.d(TAG, "SimplePutObjectTask, upload cost time: " + costTime + ", putObjectResult = " + putObjectResult);
            if (putObjectResult != null) {
                Toast.makeText(context, putObjectResult.printResult(), Toast.LENGTH_SHORT).show();
            }
            if (simplePutFileCallBack != null) {
                simplePutFileCallBack.onResult(putObjectResult, costTime);
            }
        }
    }
}

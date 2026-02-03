package com.tencent.qcloud.csp.sample;
import android.content.Context;
import android.util.Log;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.model.bucket.PutBucketRequest;
import com.tencent.cos.xml.model.bucket.PutBucketResult;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.cos.xml.model.object.PutObjectResult;
import com.tencent.cos.xml.model.service.GetServiceRequest;
import com.tencent.cos.xml.model.service.GetServiceResult;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.UploadService;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;

/**
 *
 * Created by ljz on 2026/2/4.
 * <p>
 * Copyright (c) 2010-2017 Tencent Cloud. All rights reserved.
 */
public class MyTransferManager {

    private static String secretId = "xxxx";
    private static String secretKey ="xxxx";
    private static String appid = "1255000008";
    private static String region = "ap-beijing";
    private static String hostFormat = "vodimagex-uploadtest-1301729325.cos.ap-beijing.myqcloud.com";

    private int MULTIPART_UPLOAD_SIZE = 1024 * 2;
    private CosXmlService cosXmlService;
    private boolean isHttps = true;

    private TransferManager simpleTransferManager;
    private TransferManager multiTransferManager;

    private CosXmlServiceConfig cosXmlServiceConfig;
    private Context context;

    public MyTransferManager(Context context) {
        this.context = context;
        if (cosXmlServiceConfig == null) {
            /**
             * 初始化配置
             */
            cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                    .isHttps(isHttps)
                    .setAppidAndRegion(appid, region) // appid 和 region 均可以为空
                    .setDebuggable(true)
                    .setBucketInPath(false) // 将 Bucket 放在 URL 的 Path 中
                    .setHostFormat(hostFormat)  // 私有云需要设置主域名
                    .builder();
        }
    }

    public TransferManager getSimpleTransferManager() {
        if (simpleTransferManager == null) {
            // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
            TransferConfig transferConfig = new TransferConfig.Builder()
                    // 设置是否强制使用简单上传, 禁止分块上传
                    .setForceSimpleUpload(true)
                    .build();
            /**
             * 初始化 {@link QCloudCredentialProvider} 对象，来给 SDK 提供临时密钥。
             */
            QCloudCredentialProvider credentialProvider = new ShortTimeCredentialProvider(secretId, secretKey, 300);
            cosXmlService = new CosXmlService(context, cosXmlServiceConfig, credentialProvider);

            // 初始化 TransferManager
            simpleTransferManager = new TransferManager(cosXmlService, transferConfig);
        }
        return simpleTransferManager;
    }

    public TransferManager getMultiTransferManager(long sliceSize) {
        // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
        TransferConfig transferConfig = new TransferConfig.Builder()
                // 设置启用分块上传的最小对象大小 默认为2M
//                    .setDivisionForUpload(2097152)
                // 设置分块上传时的分块大小 默认为1M
                .setSliceSizeForUpload(sliceSize > 0 ? sliceSize : 1048576)
                // 设置是否强制使用简单上传, 禁止分块上传
                .setForceSimpleUpload(false)
                .build();
        /**
         * 初始化 {@link QCloudCredentialProvider} 对象，来给 SDK 提供临时密钥。
         */
        QCloudCredentialProvider credentialProvider = new ShortTimeCredentialProvider(secretId, secretKey, 300);
        cosXmlService = new CosXmlService(context, cosXmlServiceConfig, credentialProvider);

        // 初始化 TransferManager
        multiTransferManager = new TransferManager(cosXmlService, transferConfig);
        return multiTransferManager;
    }

}

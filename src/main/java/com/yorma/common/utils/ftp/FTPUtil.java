package com.yorma.common.utils.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zxh
 * @version 1.0.0
 * @date 2019/03/28
 * @since 1.0.0
 */
@SuppressWarnings({"unused", "ResultOfMethodCallIgnored", "BooleanMethodIsAlwaysInverted"})
public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);
    private FTPClient ftp = new FTPClient();
    private FTPConfig ftpConfig;

    public FTPUtil(FTPConfig ftpConfig) {
        super();
        this.ftpConfig = ftpConfig;
    }

    /**
     * 上传文件
     *
     * @param filename 文件名
     * @param input    文件流
     * @return 上传是否成功
     */
    public boolean upload(String filename, InputStream input) {
        // boolean returnValue = false;

        try {
            if (!connect()) {
                return false;
            }
            if (!changeWorkingDirectory(ftpConfig.getRemoteSendPath())) {
                disconnect();
                return false;
            }
            // 上传文件
            boolean isStore = ftp.storeFile(filename, input);
            if (isStore) {
                logger.info(String.format("文件'%s'上传成功", filename));
            } else {
                logger.info(String.format("文件'%s'上传失败", filename));
            }

            ftp.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            disconnect();
        }
        return true;

    }

    /**
     * 下载文件
     *
     * @return 下载文件的列表
     */
    public List<String> download() {

        List<String> list = new ArrayList<>();

        String filename;
        String localFilePath;
        OutputStream os = null;
        try {
            if (!connect()) {
                logger.info("FTP连接失败");
                return list;
            }
            if (!changeWorkingDirectory(ftpConfig.getRemoteReceiptPath())) {
                this.disconnect();
                return list;
            }

            FTPFile[] fs = ftp.listFiles();
            for (FTPFile ftpFile : fs) {
                // 跳过文件夹
                if (ftpFile.isDirectory()) {
                    continue;
                }
                filename = ftpFile.getName();
                localFilePath = ftpConfig.getLocalReceiptPath() + "/" + filename;
                File localFile = new File(localFilePath);
                os = new FileOutputStream(localFile);

                if (ftp.retrieveFile(filename, os)) {
                    list.add(localFilePath);
                    logger.info(String.format("文件'%s'下载成功", filename));
                    if (ftp.deleteFile(filename)) {
                        logger.info(String.format("远程文件'%s'删除成功", filename));
                    }
                } else {
                    logger.info(String.format("文件'%s'下载失败", filename));
                    localFile.delete();
                }
            }
            ftp.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            disconnect();
        }
        return list;
    }

    private boolean connect() {
        try {
            int reply;
            ftp.setControlEncoding("GBK");
            ftp.connect(ftpConfig.getUrl(), ftpConfig.getPort());
            ftp.login(ftpConfig.getUsername(), ftpConfig.getPassword());
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void disconnect() {
        if (ftp.isConnected()) {
            try {
                ftp.disconnect();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private boolean changeWorkingDirectory(String directory) {
        try {
            return ftp.changeWorkingDirectory(ftp.printWorkingDirectory() + directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
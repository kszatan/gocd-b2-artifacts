/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import com.thoughtworks.go.plugin.api.logging.Logger;
import io.github.kszatan.gocd.b2.utils.storage.api.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BackblazeStorage implements Storage {
    private static final Integer MAX_RETRY_ATTEMPTS = 5;

    private Logger logger = Logger.getLoggerFor(BackblazeStorage.class);
    private String errorMessage = "";
    private String bucketName;
    private String bucketId;
    private String accountId;
    private String applicationKey;
    private BackblazeApiWrapper backblazeApiWrapper;
    private AuthorizeResponse authorizeResponse;
    private GetUploadUrlResponse getUploadUrlResponse;
    private ListBucketsResponse listBucketsResponse;
    private List<ProgressObserver> progressObservers = new ArrayList<>();

    public BackblazeStorage() throws IOException {
        this.backblazeApiWrapper = new BackblazeApiWrapper();
    }

    public BackblazeStorage(String bucketName) throws IOException {
        this.bucketName = bucketName;
        this.backblazeApiWrapper = new BackblazeApiWrapper();
    }

    public BackblazeStorage(String bucketName, BackblazeApiWrapper backblazeApiWrapper) {
        this.bucketName = bucketName;
        this.backblazeApiWrapper = backblazeApiWrapper;
    }

    @Override
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
        this.bucketId = null;
        this.authorizeResponse = null;
        this.getUploadUrlResponse = null;
        this.listBucketsResponse = null;
        notify("Changing bucket name to '" + bucketName + "'");
    }

    @Override
    public void setCredentials(String accountId, String applicationKey) {
        this.accountId = accountId;
        this.applicationKey = applicationKey;
    }

    @Override
    public void addProgressObserver(ProgressObserver observer) {
        progressObservers.add(observer);
    }

    @Override
    public String getLastErrorMessage() {
        return errorMessage;
    }

    @Override
    public Boolean checkConnection(String accountId, String applicationKey) throws StorageException {
        try {
            final Authorize authorize = new Authorize(backblazeApiWrapper, accountId, applicationKey);
            if (!attempt(MAX_RETRY_ATTEMPTS, authorize)) {
                return false;
            }
            AuthorizeResponse authorizeResponse = authorize.getResponse().get();
            final ListBuckets listBuckets = new ListBuckets(backblazeApiWrapper, authorizeResponse);
            if (!attempt(MAX_RETRY_ATTEMPTS, listBuckets)) {
                return false;
            }
            ListBucketsResponse listBucketsResponse = listBuckets.getResponse().get();
            Optional<Bucket> maybeBucket = getBucketId(listBucketsResponse, bucketName);
            if (!maybeBucket.isPresent()) {
                errorMessage = "Bucket '" + bucketName + "' not found";
                return false;
            }
        } catch (StorageException e) {
            logger.info("checkConnection error: " + e.getMessage());
            throw new StorageException("Failed to check connection: " + e.getMessage(), e.getCause());
        }
        notify("Successfully connected to B2.");
        return true;
    }

    @Override
    public Boolean authorize() throws StorageException {
        try {
            final Authorize authorize = new Authorize(backblazeApiWrapper, accountId, applicationKey);
            if (!attempt(MAX_RETRY_ATTEMPTS, authorize)) {
                return false;
            }
            authorizeResponse = authorize.getResponse().get();
            final ListBuckets listBuckets = new ListBuckets(backblazeApiWrapper, authorizeResponse);
            if (!attempt(MAX_RETRY_ATTEMPTS, listBuckets)) {
                return false;
            }
            listBucketsResponse = listBuckets.getResponse().get();
            Optional<Bucket> maybeBucket = getBucketId(listBucketsResponse, bucketName);
            if (!maybeBucket.isPresent()) {
                errorMessage = "Bucket '" + bucketName + "' doesn't exist";
                return false;
            }
            bucketId = maybeBucket.get().id;
        } catch (StorageException  e) {
            authorizeResponse = null;
            logger.info("authorize error: " + e.getMessage());
            throw new StorageException("Failed to authorize: " + e.getMessage(), e.getCause());
        }
        notify("Successfully authorized with B2.");
        return true;
    }

    @Override
    public void upload(Path workDir, String relativeFilePath, String destination)
            throws StorageException {
        if (authorizeResponse == null) {
            throw new StorageException("Authorize not called");
        }
        try {
            try {
                long fileSize = Files.size(workDir.resolve(relativeFilePath));
                if (fileSize > authorizeResponse.recommendedPartSize) {
                    uploadLargeFile(workDir, relativeFilePath, destination);
                } else {
                    uploadSmallFile(workDir, relativeFilePath, destination);
                }
            } catch (LargeFileUploadException e) {
                cancelLargeFileUpload(e.getFileId());
                throw new StorageException(e.getCause());
            }
        } catch (IOException e) {
            logger.info("upload error: " + e.getMessage());
            throw new StorageException("Failed to upload " + relativeFilePath + ": " + e.getMessage(), e);
        }
        notify("Successfully uploaded " + relativeFilePath + " to " + destination + ".");
    }

    private void uploadSmallFile(Path workDir, String relativeFilePath, String destination)
            throws StorageException {
        if (getUploadUrlResponse == null) {
            getUploadUrlResponse = getUploadUrl(authorizeResponse, bucketId).orElseThrow(
                    () -> new StorageException("Get upload URL operation failed")
            );
        }
        Upload upload = new Upload(backblazeApiWrapper, bucketId, workDir, relativeFilePath, destination,
                authorizeResponse, getUploadUrlResponse);
        if (!attempt(MAX_RETRY_ATTEMPTS, upload)) {
            throw new StorageException("Upload operation failed");
        }
    }

    private Optional<GetUploadUrlResponse> getUploadUrl(AuthorizeResponse authorizeResponse, String bucketId) throws StorageException {
        GetUploadUrl getUploadUrl = new GetUploadUrl(backblazeApiWrapper, authorizeResponse, bucketId);
        if (!attempt(MAX_RETRY_ATTEMPTS, getUploadUrl)) {
            return Optional.empty();
        }
        return getUploadUrl.getResponse();
    }

    private Optional<StartLargeFileResponse> startLargeFile(String fileName) throws StorageException {
        StartLargeFile startLargeFile = new StartLargeFile(backblazeApiWrapper, authorizeResponse, fileName, bucketId);
        if (!attempt(MAX_RETRY_ATTEMPTS, startLargeFile)) {
            return Optional.empty();
        }
        return startLargeFile.getResponse();
    }

    private Optional<GetUploadPartUrlResponse> getUploadPartUrl(String fileId) throws StorageException {
        GetUploadPartUrl getUploadPartUrl = new GetUploadPartUrl(backblazeApiWrapper, authorizeResponse, fileId);
        if (!attempt(MAX_RETRY_ATTEMPTS, getUploadPartUrl)) {
            return Optional.empty();
        }
        return getUploadPartUrl.getResponse();
    }

    private Optional<FinishLargeFileResponse> finishLargeFile(String fileId, List<String> partSha1Array) throws StorageException {
        FinishLargeFile finishLargeFile = new FinishLargeFile(backblazeApiWrapper, authorizeResponse, fileId, partSha1Array);
        if (!attempt(MAX_RETRY_ATTEMPTS, finishLargeFile)) {
            return Optional.empty();
        }
        return finishLargeFile.getResponse();
    }

    private Optional<UploadPartResponse> uploadPart(GetUploadPartUrlResponse getUploadPartUrlResponse, int partLength,
                                                    byte[] buf, int partNumber) throws StorageException {
        UploadPart uploadPart = new UploadPart(backblazeApiWrapper, buf, partLength, partNumber,
                authorizeResponse, getUploadPartUrlResponse);
        if (!attempt(MAX_RETRY_ATTEMPTS, uploadPart)) {
            return Optional.empty();
        }
        return uploadPart.getResponse();
    }

    private void uploadLargeFile(Path workDir, String relativeFilePath, String destination)
            throws StorageException, LargeFileUploadException {
        final String fileId = startLargeFile(Paths.get(destination, relativeFilePath).toString()).orElseThrow(
                () -> new StorageException("Failed to start large file upload")
        ).fileId;
        try {
            GetUploadPartUrlResponse getUploadPartUrlResponse = getUploadPartUrl(fileId).orElseThrow(
                    () -> new StorageException("Failed to get upload part URL")
            );
            ArrayList<String> partSha1Array = doUpload(workDir, relativeFilePath, getUploadPartUrlResponse);
            finishLargeFile(fileId, partSha1Array).orElseThrow(
                    () -> new StorageException("Failed to finish large file")
            );
        } catch (IOException | StorageException e) {
            throw new LargeFileUploadException(e.getMessage(), fileId, e);
        }
    }

    private ArrayList<String> doUpload(Path workDir, String relativeFilePath, GetUploadPartUrlResponse getUploadPartUrlResponse)
            throws StorageException, IOException {
        File file = new File(workDir.resolve(relativeFilePath).toString());
        final long fileSize = file.length();
        long totalBytesSent = 0;
        long partLength = authorizeResponse.recommendedPartSize;
        byte[] buf = new byte[(int)partLength];
        ArrayList<String> partSha1Array = new ArrayList<>();
        int partNumber = 1;
        while (totalBytesSent < fileSize) {
            if ((fileSize - totalBytesSent) < authorizeResponse.recommendedPartSize) {
                partLength = (fileSize - totalBytesSent);
            }
            readFilePart(file, totalBytesSent, (int) partLength, buf);
            String partSha1 = uploadPart(getUploadPartUrlResponse, (int) partLength, buf, partNumber).orElseThrow(
                    () -> new StorageException("Failed to get upload part")
            ).contentSha1;
            notify("Successfully uploaded part " + partNumber + " of " + relativeFilePath + ".");
            partSha1Array.add(partSha1);
            totalBytesSent = totalBytesSent + partLength;
            partNumber++;
        }
        return partSha1Array;
    }

    private void readFilePart(File file, long totalBytesSent, int partLength, byte[] buf) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.skip(totalBytesSent);
        fileInputStream.read(buf, 0, partLength);
        fileInputStream.close();
    }

    private Optional<CancelLargeFileResponse> cancelLargeFileUpload(String fileId) throws StorageException {
        CancelLargeFile cancelLargeFile = new CancelLargeFile(backblazeApiWrapper, authorizeResponse, fileId);
        if (!attempt(MAX_RETRY_ATTEMPTS, cancelLargeFile)) {
            return Optional.empty();
        }
        return cancelLargeFile.getResponse();
    }

    @Override
    public Optional<ListFileNamesResponse> listFiles(String startFileName, String prefix, String delimiter) throws StorageException {
        if (authorizeResponse == null) {
            throw new StorageException("Authorize not called");
        }
        Optional<ListFileNamesResponse> response;
        try {
            ListFileNames listFileNames = new ListFileNames(backblazeApiWrapper, authorizeResponse, bucketId, startFileName, prefix, delimiter);
            if (!attempt(MAX_RETRY_ATTEMPTS, listFileNames)) {
                response = Optional.empty();
            } else {
                response = listFileNames.getResponse();
            }
        } catch (StorageException e) {
            logger.info("listFiles error: " + e.getMessage());
            throw new StorageException("Failed to list files: " + e.getMessage(), e.getCause());
        }
        return response;
    }

    @Override
    public Boolean download(String fileName, Path destination) throws StorageException {
        if (authorizeResponse == null) {
            throw new StorageException("Authorize not called");
        }
        try {
            Download download = new Download(backblazeApiWrapper, bucketName, fileName, destination, authorizeResponse);
            if (!attempt(MAX_RETRY_ATTEMPTS, download)) {
                return false;
            }
        } catch (StorageException  e) {
            logger.info("download error: " + e.getMessage());
            throw new StorageException("Failed to download " + fileName + ": " + e.getMessage(), e.getCause());
        }
        notify("Successfully downloaded " + fileName + " to " + destination + ".");
        return true;
    }

    private Boolean attempt(final Integer times, final B2ApiCall action) throws StorageException {
        int attempt;
        for (attempt = 0; attempt < times; attempt++) {
            if (action.call()) {
                break;
            }
            ErrorResponse errorResponse = backblazeApiWrapper.getLastError().orElseThrow(
                    () -> new StorageException("Unknown error from B2 storage layer"));
            try {
                action.handleErrors(errorResponse);
            } catch (UnauthorizedCallException e) {
                authorize();
            }
            notify("Failed to " + action.getName() + ": (" + errorResponse.status + ") " + errorResponse.message);
            notify("Retrying...");
        }
        Boolean success = attempt < times;
        if (!success) {
            errorMessage = "Failed to " + action.getName() + ": maximum number of retry attempts reached";
        }
        return success;
    }

    private Optional<Bucket> getBucketId(ListBucketsResponse listBucketsResponse, String bucketName) {
        return listBucketsResponse.buckets.stream().filter(b -> b.name.equals(bucketName)).findFirst();
    }

    private void notify(String notification) {
        progressObservers.stream().forEach(o -> o.notify(notification));
    }
}

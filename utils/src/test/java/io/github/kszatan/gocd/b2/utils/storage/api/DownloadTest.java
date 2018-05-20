/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.DownloadFileResponse;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DownloadTest {
    private Download download;
    private BackblazeApiWrapper mockApiWrapper;
    private AuthorizeResponse authorizeResponse;
    private final String bucketName = "bukhet";
    private final String backblazeFileName = "dir1/dir2/fileName.txt";
    private final Path destination = Paths.get("path", "to", "dest");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        mockApiWrapper = mock(BackblazeApiWrapper.class);
        authorizeResponse = new AuthorizeResponse();
        authorizeResponse.accountId = "accountId";
        authorizeResponse.absoluteMinimumPartSize = 100;
        authorizeResponse.apiUrl = "https://api001.backblazeb2.com";
        authorizeResponse.authorizationToken = "token_fristajlo";
        authorizeResponse.downloadUrl = "https://f001.backblazeb2.com";
        authorizeResponse.recommendedPartSize = 100000000;
        download = new Download(mockApiWrapper, bucketName, backblazeFileName, destination, authorizeResponse);
    }

    @Test
    public void callShouldReturnTrueOnSuccess() throws Exception {
        Download.MkdirsProvider mkdirsProviderMock = mock(Download.MkdirsProvider.class);
        download.setMkdirsProvider(mkdirsProviderMock);
        DownloadFileResponse response = new DownloadFileResponse();
        doReturn(Optional.of(response)).when(mockApiWrapper).downloadFileByName(bucketName, backblazeFileName, destination, authorizeResponse);
        Boolean result = download.call();
        verify(mkdirsProviderMock).mkdirs(Paths.get("path", "to", "dest", "dir1", "dir2"));
        assertThat(result, equalTo(true));
    }

    @Test
    public void callShouldThrowExceptionWhenMkdirsFails() throws Exception {
        Download.MkdirsProvider mkdirsProviderMock = mock(Download.MkdirsProvider.class);
        download.setMkdirsProvider(mkdirsProviderMock);
        doThrow(new IOException("Cannot create directory")).when(mkdirsProviderMock).mkdirs(any());
        thrown.expect(StorageException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(IOException.class));
        download.call();
    }

    @Test
    public void callShouldReturnFalseOnFailure() throws Exception {
        Download.MkdirsProvider mkdirsProviderMock = mock(Download.MkdirsProvider.class);
        download.setMkdirsProvider(mkdirsProviderMock);
        doReturn(Optional.empty()).when(mockApiWrapper).downloadFileByName(bucketName, backblazeFileName, destination, authorizeResponse);
        Boolean result = download.call();
        assertThat(result, equalTo(false));
    }
}
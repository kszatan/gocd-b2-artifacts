/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.executor;

import java.util.Arrays;
import java.util.List;

public class DefaultDirectoryScanner implements DirectoryScanner {
    private org.apache.maven.shared.utils.io.DirectoryScanner scanner = new org.apache.maven.shared.utils.io.DirectoryScanner();

    @Override
    public void setBaseDir(String baseDir) {
        scanner.setBasedir(baseDir);
    }

    @Override
    public void scan(String pattern) {
        scanner.setIncludes(pattern);
        scanner.scan();
    }

    @Override
    public List<String> getIncludedFiles() {
        return Arrays.asList(scanner.getIncludedFiles());
    }
}

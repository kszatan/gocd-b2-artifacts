/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.executor;

import java.util.List;

public interface DirectoryScanner {
    public void setBaseDir(String baseDir);
    public void scan(String pattern);
    public List<String> getIncludedFiles();
}

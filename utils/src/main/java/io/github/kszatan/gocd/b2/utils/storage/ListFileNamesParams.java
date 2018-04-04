/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

public class ListFileNamesParams {
    public String bucketId;
    public String startFileName;
    public String prefix;
    public String delimiter;
    public Integer maxFileCount = 1000;
}

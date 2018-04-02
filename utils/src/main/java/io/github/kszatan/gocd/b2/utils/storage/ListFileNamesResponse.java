/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import java.util.ArrayList;
import java.util.List;

public class ListFileNamesResponse {
    public List<FileName> fileNames = new ArrayList<>();
    public String nextFileName;
}

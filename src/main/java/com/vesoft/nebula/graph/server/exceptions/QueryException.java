/* Copyright (c) 2023 vesoft inc. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License.
 */

package com.vesoft.nebula.graph.server.exceptions;

public class QueryException extends Exception {
    public QueryException(String message, Throwable e) {
        super(message, e);
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.renren.modules.test.jmeter.report;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * This class contains frequently-used static utility methods.
 * Created by smooth00 on 2021/4/14 10:31.
 * 重写了OrphanUtils类，为了兼容5.1~5.4版本的jmeter（jmeter4.0需要删除该类）
 */
public final class LocalJOrphanUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private LocalJOrphanUtils() {
    }

    /**
     * Check whether we can write to a folder.
     * A folder can be written to if if does not contain any file or folder
     * Throw {@link IllegalArgumentException} if folder cannot be written to either:
     * <ul>
     *  <li>Because it exists but is not a folder</li>
     *  <li>Because it exists but is not empty</li>
     *  <li>Because it does not exist but cannot be created</li>
     * </ul>
     *
     * @param folder to check
     * @throws IllegalArgumentException when folder can't be written to
     */
    public static void canSafelyWriteToFolder(File folder) {
        canSafelyWriteToFolder(folder, false, file -> true);
    }


    /**
     * Check whether we can write to a folder.
     * A folder can be written to if folder.listFiles(exporterFileFilter) does not return any file or folder.
     * Throw {@link IllegalArgumentException} if folder cannot be written to either:
     * <ul>
     *  <li>Because it exists but is not a folder</li>
     *  <li>Because it exists but is not empty using folder.listFiles(exporterFileFilter)</li>
     *  <li>Because it does not exist but cannot be created</li>
     * </ul>
     *
     * @param folder     to check
     * @param fileFilter used to filter listing of folder
     * @throws IllegalArgumentException when folder can't be written to
     */
    public static void canSafelyWriteToFolder(File folder, FileFilter fileFilter) {
        canSafelyWriteToFolder(folder, false, fileFilter);
    }

    /**
     * Check whether we can write to a folder. If {@code deleteFolderContent} is {@code true} the folder or file with
     * the same name will be emptied or deleted.
     *
     * @param folder              to check
     * @param deleteFolderContent flag whether the folder should be emptied or a file with the same name deleted
     * @throws IllegalArgumentException when folder can't be written to
     *                                  Throw IllegalArgumentException if folder cannot be written
     */
    public static void canSafelyWriteToFolder(File folder, boolean deleteFolderContent) {
        canSafelyWriteToFolder(folder, deleteFolderContent, file -> true);
    }


    /**
     * Check whether we can write to a folder.
     *
     * @param folder               which should be checked for writability and emptiness
     * @param deleteFolderIfExists flag whether the folder should be emptied or a file with the same name deleted
     * @param exporterFileFilter   used for filtering listing of the folder
     * @throws IllegalArgumentException when folder can't be written to. That could have the following reasons:
     *                                  <ul>
     *                                   <li>it exists but is not a folder</li>
     *                                   <li>it exists but is not empty</li>
     *                                   <li>it does not exist but cannot be created</li>
     *                                  </ul>
     */
    public static void canSafelyWriteToFolder(File folder, boolean deleteFolderIfExists, FileFilter exporterFileFilter) {
        if (folder.exists()) {
            if (folder.isFile()) {
                if (deleteFolderIfExists) {
                    if (!folder.delete()) {
                        throw new IllegalArgumentException("Cannot write to '"
                                + folder.getAbsolutePath() + "' as it is an existing file and delete failed");
                    }
                } else {
                    throw new IllegalArgumentException("Cannot write to '"
                            + folder.getAbsolutePath() + "' as it is an existing file");
                }
            } else {
                File[] listedFiles = folder.listFiles(exporterFileFilter);
                if (listedFiles != null && listedFiles.length > 0) {
                    if (deleteFolderIfExists) {
                        try {
                            FileUtils.deleteDirectory(folder);
                        } catch (IOException ex) {
                            throw new IllegalArgumentException("Cannot write to '" + folder.getAbsolutePath()
                                    + "' as folder is not empty and cleanup failed with error:" + ex.getMessage(), ex);
                        }
                        if (!folder.mkdir()) {
                            throw new IllegalArgumentException("Cannot create folder " + folder.getAbsolutePath());
                        }
                    } else {
                        throw new IllegalArgumentException("Cannot write to '"
                                + folder.getAbsolutePath() + "' as folder is not empty");
                    }
                }
            }
        } else {
            // check we can create it
            if (!folder.getAbsoluteFile().getParentFile().canWrite()) {
                throw new IllegalArgumentException("Cannot write to '"
                        + folder.getAbsolutePath() + "' as folder does not exist and parent folder is not writable");
            }
        }
    }
}

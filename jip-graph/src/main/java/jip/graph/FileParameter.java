/*
 * Copyright (C) 2012 Thasso Griebel
 *
 * This file is part of JIP.
 *
 * JIP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JIP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package jip.graph;

/**
 * Wraps around a file path or name and allows to extract the simple name
 * and the extension
 *
 */
public class FileParameter {
    /**
     * The file
     */
    private final String file;

    /**
     * Create a new file parameter
     *
     * @param file the source file name or path
     */
    public FileParameter(String file) {
        if(file == null) throw new NullPointerException("NULL file is not permitted");
        this.file = file;
    }

    /**
     * Get the simple name of the file excluding any extensions
     *
     * @return name simple name of the file without extensions
     */
    public String getName() {
        int startIndex = file.lastIndexOf("/");
        if (startIndex < 0) startIndex = 0;
        else {
            startIndex += 1;
        }
        int endIndex = file.lastIndexOf(".");
        if (endIndex < 0) {
            endIndex = file.length();
        }
        return file.substring(startIndex, endIndex);
    }

    /**
     * Get the parent name
     * @return parent the name of the parent directory or empty string
     */
    public String getParent() {
        int i = file.lastIndexOf("/");
        if (i >= 0) {
            return file.substring(0, i + 1);
        }
        return "";
    }

    /**
     * Get the extension of the file
     *
     * @return extension the extension or empty string
     */
    public String getExtension() {
        int i = file.lastIndexOf(".");
        if (i >= 0 && i < file.length() - 2) {
            return file.substring(i + 1);
        }
        return "";
    }

    @Override
    public String toString() {
        return file;
    }
}

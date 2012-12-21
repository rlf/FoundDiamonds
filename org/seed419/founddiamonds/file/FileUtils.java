package org.seed419.founddiamonds.file;

import org.seed419.founddiamonds.FoundDiamonds;

import java.io.Closeable;
import java.io.IOException;
import java.text.MessageFormat;

/*
Copyright 2011-2012 Blake Bartenbach

This file is part of FoundDiamonds.

FoundDiamonds is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FoundDiamonds is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FoundDiamonds.  If not, see <http://www.gnu.org/licenses/>.
*/

public class FileUtils {


    private FoundDiamonds fd;


    public FileUtils(FoundDiamonds fd) {
        this.fd = fd;
    }


    public void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
                fd.getLog().warning(MessageFormat.format("Failure to close a file stream, {0} ", ex));
            }
        }
    }
}

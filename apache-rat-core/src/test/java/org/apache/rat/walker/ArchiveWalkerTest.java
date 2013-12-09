/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */

package org.apache.rat.walker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import org.junit.Assert;
import org.junit.Test;


/**
 * The Class ArchiveWalkerTest.
 */
public class ArchiveWalkerTest {

	/**
	 * Test walker contructor.
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	@Test
	public void testWalkerContructor() throws FileNotFoundException {
		File file = new File("");
		FilenameFilter filter = null;
		ArchiveWalker archiveWalker = new ArchiveWalker(file, filter);
		Assert.assertNotNull(archiveWalker);
	}

}
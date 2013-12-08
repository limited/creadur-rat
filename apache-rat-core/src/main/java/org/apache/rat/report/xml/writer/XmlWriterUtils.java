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
package org.apache.rat.report.xml.writer;

import java.io.IOException;

/**
 * The Class XmlWriterUtils.
 */
public class XmlWriterUtils {

	/**
	 * Write attribute.
	 * 
	 * @param writer
	 *            the writer
	 * @param name
	 *            the name
	 * @param booleanValue
	 *            the boolean value
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static final void writeAttribute(final IXmlWriter writer,
			final String name, final boolean booleanValue) throws IOException {
		final String value = Boolean.toString(booleanValue);
		writer.attribute(name, value);
	}
}

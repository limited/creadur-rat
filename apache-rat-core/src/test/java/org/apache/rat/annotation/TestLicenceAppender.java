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
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.rat.test.utils.Resources;

public class TestLicenceAppender extends TestCase {
    /** Used to ensure that temporary files have unq */
    private Random random = new Random();

    private interface FileCreator {
        void createFile(Writer w) throws IOException;
    }

    private interface NewFileReader {
        void readFile(BufferedReader r) throws IOException;
    }

    private static String qualify(String fileName) {
        return new File(new File(System.getProperty("java.io.tmpdir")),
                        fileName)
            .getAbsolutePath();
    }

    private static void createTestFile(String fileName,
                                       FileCreator creator)
        throws IOException {
        FileWriter w = null;
        try {
            creator.createFile(w = new FileWriter(fileName));
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    private static void tryToDelete(File f) throws IOException {
        if (f != null && f.exists() && !f.delete()) {
            f.deleteOnExit();
        }
    }

    private static void commonTestTemplate(String relativeName,
                                           FileCreator creator,
                                           NewFileReader reader)
        throws IOException {
        String name = qualify(relativeName);
        try {
            createTestFile(name, creator);

            ApacheV2LicenceAppender appender =
                new ApacheV2LicenceAppender();
            appender.append(new File(name));

            BufferedReader r = null;
            try {
                r = new BufferedReader(new FileReader(name + ".new"));
                reader.readFile(r);
            } finally {
                if (r != null) {
                    r.close();
                }
            }
        } finally {
            tryToDelete(new File(name));
            tryToDelete(new File(name + ".new"));
        }
    }

    private static NewFileReader checkLines(final String firstLine,
                                            final String secondLine) {
        return new NewFileReader() {
            public void readFile(BufferedReader r) throws IOException {
                String line = r.readLine();
                assertEquals("First line is incorrect",
                             firstLine, line);
                if (secondLine != null) {
                    line = r.readLine();
                    assertEquals("Second line is incorrect",
                                 secondLine, line);
                }
            }
        };
    }

    public void testAddLicenceToUnknownFile() throws IOException {
        String filename = qualify("tmp" + random.nextLong()
                                  + ".unknownType");
        File file = null;
        File newFile = null;
        try {
            createTestFile(filename, new FileCreator() {
                    public void createFile(Writer writer)
                        throws IOException {
                        writer.write("Unknown file type\n");
                    }
                });

            file = new File(filename);
            ApacheV2LicenceAppender appender =
                new ApacheV2LicenceAppender();
            appender.append(file);

            newFile = new File(filename + ".new");
            assertFalse("No new file should have been written",
                        newFile.exists());
        } finally {
            tryToDelete(file);
            tryToDelete(newFile);
        }
    }

    public void testAddLicenceToJava() throws IOException {
        String filename = "tmp.java";
        final String firstLine = "package foo;";
        String secondLine = "/*";
        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write(firstLine + "\n");
                    writer.write("\n");
                    writer.write("public class test {\n");
                    writer.write("}\n");
                }
            },
            checkLines(firstLine, secondLine));
    }

    public void testAddLicenceToJavaWithoutPackage() throws IOException {
        String filename = "tmp.java";
        String commentLine = "/*";
        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("public class test {\n");
                    writer.write("}\n");
                }
            },
            checkLines(commentLine, null));
    }

    public void testAddLicenceToXML() throws IOException {
        String filename = "tmp.xml";
        final String firstLine = "<?xml version='1.0'?>";
        final String secondLine = "<!--";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write(firstLine + "\n");
                    writer.write("\n");
                    writer.write("<xml>\n");
                    writer.write("</xml>\n");
                }
            },
            checkLines(firstLine, secondLine));
    }

    public void testAddLicenceToXMLWithoutDecl() throws IOException {
        String filename = "tmp.xml";
        final String firstLine = "<?xml version='1.0'?>";
        final String secondLine = "<!--";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("<xml>\n");
                    writer.write("</xml>\n");
                }
            },
            checkLines(firstLine, secondLine));
    }

    public void testAddLicenceToHTML() throws IOException {
        String filename = "tmp.html";
        String commentLine = "<!--";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("<html>\n");
                    writer.write("\n");
                    writer.write("</html>\n");
                }
            },
            checkLines(commentLine, null));
    }

    public void testAddLicenceToCSS() throws IOException {
        String filename = "tmp.css";
        String firstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write(".class {\n");
                    writer.write(" background-color: red;");
                    writer.write("}\n");
                }
            },
            checkLines(firstLine, null));
    }

    public void testAddLicenceToJavascript() throws IOException {
        String filename = "tmp.js";
        String firstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("if (a ==b) {>\n");
                    writer.write(" alert(\"how useful!\");");
                    writer.write("}\n");
                }
            },
            checkLines(firstLine, null));
    }

    public void testAddLicenceToAPT() throws IOException {
        String filename = "tmp.apt";
        String firstLine = "~~";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("A Simple APT file");
                    writer.write(" This file contains nothing\n");
                    writer.write(" of any importance\n");
                }
            },
            checkLines(firstLine, null));
    }

    public void testAddLicenceToProperties() throws IOException {
        String filename = "tmp.properties";
        String firstLine = "#";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("property = value\n");
                    writer.write("fun = true\n");
                    writer.write("cool = true\n");
                }
            },
            checkLines(firstLine, null));
    }

    public void testAddLicenceToScala() throws IOException {
        String filename = "tmp.scala";
        final String firstLine = "package foo {";
        final String newFirstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write(firstLine + "\n");
                    writer.write("\n");
                    writer.write("    object X { val x = 1; }\n");
                    writer.write("}\n");
                }
            },
            new NewFileReader() {
                public void readFile(BufferedReader reader)
                    throws IOException {
                    String line = reader.readLine();
                    assertEquals("First line is incorrect",
                                 newFirstLine, line);
                    while ((line = reader.readLine()) != null) {
                        if (line.length() == 0) {
                            line = reader.readLine();
                            break;
                        }
                    }
                    assertEquals("Package line is incorrect",
                                 firstLine, line);
                }
            });
    }

    public void testAddLicenseToRubyWithoutHashBang()
        throws IOException {
        String filename = "tmp.rb";
        String firstLine = "#";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("class Foo\n");
                    writer.write("end\n");
                }
            },
            checkLines(firstLine, null));
    }

    public void testAddLicenseToRubyWithHashBang() throws IOException {
        String filename = "tmp.rb";
        final String firstLine = "#!/usr/bin/env ruby";
        String secondLine = "#";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write(firstLine + "\n");
                    writer.write("class Foo\n");
                    writer.write("end\n");
                }
            },
            checkLines(firstLine, secondLine));
    }

    public void testAddLicenseToPerlWithoutHashBang()
        throws IOException {
        String filename = "tmp.pl";
        String firstLine = "#";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("print \"Hello world\"\n");
                }
            },
            checkLines(firstLine, null));
    }

    public void testAddLicenseToPerlWithHashBang() throws IOException {
        String filename = "tmp.pl";
        final String firstLine = "#!/usr/bin/env perl";
        String secondLine = "#";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write(firstLine + "\n");
                    writer.write("print \"Hello world\"\n");
                }
            },
            checkLines(firstLine, secondLine));
    }

    public void testAddLicenseToTclWithoutHashBang()
        throws IOException {
        String filename = "tmp.tcl";
        String firstLine = "#";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("puts \"Hello world\"\n");
                }
            },
            checkLines(firstLine, null));
    }

    public void testAddLicenseToTclWithHashBang() throws IOException {
        String filename = "tmp.tcl";
        final String firstLine = "#!/usr/bin/env tcl";
        String secondLine = "#";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write(firstLine + "\n");
                    writer.write("puts \"Hello world\"\n");
                }
            },
            checkLines(firstLine, secondLine));
    }

    public void testAddLicenceToPHP() throws IOException {
        String filename = "tmp.php";
        final String firstLine = "<?php";
        String secondLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write(firstLine + "\n");
                    writer.write("echo 'Hello World'\n");
                    writer.write("?>\n");
                }
            },
            checkLines(firstLine, secondLine));
    }

    public void testAddLicenceToCSharp() throws IOException {
        String filename = "tmp.cs";
        String firstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("namespace org.example {\n");
                    writer.write("    public class Foo {\n");
                    writer.write("    }\n");
                    writer.write("}\n");
                }
            },
            checkLines(firstLine, null));
    }

    public void testAddLicenceToGroovy() throws IOException {
        String filename = "tmp.groovy";
        String firstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("package org.example \n");
                    writer.write("    class Foo {\n");
                    writer.write("    }\n");
                }
            },
            checkLines(firstLine, null));
    }

    public void testAddLicenceToCPlusPlus() throws IOException {
        String filename = "tmp.cpp";
        String firstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write("namespace org.example {\n");
                    writer.write("    public class Foo {\n");
                    writer.write("    }\n");
                    writer.write("}\n");
                }
            },
            checkLines(firstLine, null));
    }

    public void testFileWithBOM() throws IOException {
        File f = Resources.getResourceFile("violations/FilterTest.cs");
        try {
            ApacheV2LicenceAppender appender =
                new ApacheV2LicenceAppender();
            appender.append(f);

            BufferedReader r = null;
            try {
                r = new BufferedReader(new FileReader(f.getAbsolutePath()
                                                      + ".new"));
                assertEquals("/*", r.readLine());
                String line = null;
                while ((line = r.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        break;
                    }
                }
                assertEquals("#if NET_2_0", r.readLine());
            } finally {
                if (r != null) {
                    r.close();
                }
            }
        } finally {
            tryToDelete(new File(f.getAbsolutePath() + ".new"));
        }
    }

    public void testAddLicenceToVS2003solution() throws IOException {
        String filename = "tmp.sln";
        final String firstLine = "Microsoft Visual Studio Solution File,"
            + " Format Version 8.0";
        String secondLine = "#";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write(firstLine + "\n");
                    writer.write("Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"ConsoleApp\", \"Tutorials\\ConsoleApp\\cs\\src\\ConsoleApp.csproj\", \"{933969DF-2BC5-44E6-8B1A-400FC276A23F}\"\n");
                    writer.write("	ProjectSection(WebsiteProperties) = preProject\n");
                    writer.write("		Debug.AspNetCompiler.Debug = \"True\"\n");
                    writer.write("		Release.AspNetCompiler.Debug = \"False\"\n");
                    writer.write("	EndProjectSection\n");
                    writer.write("EndProject\n");
                }
            },
            checkLines(firstLine, secondLine));
    }

    public void testAddLicenceToVS2005solution() throws IOException {
        String filename = "tmp.sln";
        final String firstLine = "Microsoft Visual Studio Solution File,"
            + " Format Version 9.0";
        final String secondLine = "# Visual Studio 2005";
        final String thirdLine = "#";

        commonTestTemplate(filename, new FileCreator() {
                public void createFile(Writer writer)
                    throws IOException {
                    writer.write(firstLine + "\n");
                    writer.write(secondLine + "\n");
                    writer.write("Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"ConsoleApp\", \"Tutorials\\ConsoleApp\\cs\\src\\ConsoleApp.csproj\", \"{933969DF-2BC5-44E6-8B1A-400FC276A23F}\"\n");
                    writer.write("	ProjectSection(WebsiteProperties) = preProject\n");
                    writer.write("		Debug.AspNetCompiler.Debug = \"True\"\n");
                    writer.write("		Release.AspNetCompiler.Debug = \"False\"\n");
                    writer.write("	EndProjectSection\n");
                    writer.write("EndProject\n");
                }
            },
            new NewFileReader() {
                public void readFile(BufferedReader r) throws IOException {
                    String line = r.readLine();
                    assertEquals("First line is incorrect",
                                 firstLine, line);
                    line = r.readLine();
                    assertEquals("Second line is incorrect",
                                 secondLine, line);
                    line = r.readLine();
                    assertEquals("Third line is incorrect",
                                 thirdLine, line);
                }
            });
    }

}

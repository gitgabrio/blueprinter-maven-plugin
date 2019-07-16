/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.maven.blueprinter.plugin


class PrintMojoTest/* : AbstractMojoTestCase() */{

//    @Rule
//    @JvmField
//    val resources: TestResources = TestResources()
//
//    @Rule
//    @JvmField
//    val maven: TestMavenRuntime = TestMavenRuntime()
//
//    private var testDir: File? = null
//
//    // TestCase methods -------------------------------------------------------
//
////    /*
////     * @see org.apache.maven.plugin.testing.AbstractMojoTestCase#setUp()
////     */
////    @Before
////    override fun setUp() {
////        // required for mojo lookups to work
////        super.setUp()
//////
//////        testDir = resources.getBasedir("print")
//////        assertTrue(testDir != null && testDir!!.exists() && testDir!!.canRead())
////    }
//
//    // tests ------------------------------------------------------------------
//
//    fun testVoid() {
//        // TODO: tests disabled during MDEP-339 work, to be reactivated
//    }
//
//    /**
//     * Tests the proper discovery and configuration of the mojo.
//     * @throws Exception in case of an error.
//     */
//    @Throws(Exception::class)
//    @Test
//    fun testPrint() {
//        val testPom = File(getBasedir(), "target/test-classes/unit/print-test/plugin-config.xml")
//        val mojo = PrintMojo()
//        TestCase.assertNotNull(mojo)
//        mojo.execute()
//        //        assertNotNull(mojo.getProject());
//        //        MavenProject project = mojo.getProject();
//        //        project.setArtifact(this.stubFactory.createArtifact("testGroupId", "project", "1.0"));
//        //
//        //        Set<Artifact> artifacts = this.stubFactory.getScopedArtifacts();
//        //        Set<Artifact> directArtifacts = this.stubFactory.getReleaseAndSnapshotArtifacts();
//        //        artifacts.addAll(directArtifacts);
//        //
//        //        project.setArtifacts(artifacts);
//        //        project.setDependencyArtifacts(directArtifacts);
//        //
//        //        mojo.execute();
//        //
//        //        DependencyNode rootNode = mojo.getDependencyGraph();
//        //        assertNodeEquals("testGroupId:project:jar:1.0:compile", rootNode);
//        //        assertEquals(2, rootNode.getChildren().size());
//        //        assertChildNodeEquals("testGroupId:snapshot:jar:2.0-SNAPSHOT:compile", rootNode, 0);
//        //        assertChildNodeEquals("testGroupId:release:jar:1.0:compile", rootNode, 1);
//    }
//
//    //    /**
//    //     * Test the DOT format serialization
//    //     * @throws Exception in case of an error.
//    //     */
//    //    public void _testTreeDotSerializing()
//    //            throws Exception {
//    //        List<String> contents = runTreeMojo("tree1.dot", "dot");
//    //        assertTrue(findString(contents, "digraph \"testGroupId:project:jar:1.0:compile\" {"));
//    //        assertTrue(findString(contents,
//    //                              "\"testGroupId:project:jar:1.0:compile\" -> \"testGroupId:snapshot:jar:2.0-SNAPSHOT:compile\""));
//    //        assertTrue(findString(contents,
//    //                              "\"testGroupId:project:jar:1.0:compile\" -> \"testGroupId:release:jar:1.0:compile\""));
//    //    }
//    //
//    //    /**
//    //     * Test the GraphML format serialization
//    //     * @throws Exception in case of an error.
//    //     */
//    //    public void _testTreeGraphMLSerializing()
//    //            throws Exception {
//    //        List<String> contents = runTreeMojo("tree1.graphml", "graphml");
//    //
//    //        assertTrue(findString(contents, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
//    //        assertTrue(findString(contents, "<y:NodeLabel>testGroupId:project:jar:1.0:compile</y:NodeLabel>"));
//    //        assertTrue(findString(contents,
//    //                              "<y:NodeLabel>testGroupId:snapshot:jar:2.0-SNAPSHOT:compile</y:NodeLabel>"));
//    //        assertTrue(findString(contents, "<y:NodeLabel>testGroupId:release:jar:1.0:compile</y:NodeLabel>"));
//    //        assertTrue(findString(contents, "<key for=\"node\" id=\"d0\" yfiles.type=\"nodegraphics\"/>"));
//    //        assertTrue(findString(contents, "<key for=\"edge\" id=\"d1\" yfiles.type=\"edgegraphics\"/>"));
//    //    }
//    //
//    //    /**
//    //     * Test the TGF format serialization
//    //     * @throws Exception in case of an error.
//    //     */
//    //    public void _testTreeTGFSerializing()
//    //            throws Exception {
//    //        List<String> contents = runTreeMojo("tree1.tgf", "tgf");
//    //        assertTrue(findString(contents, "testGroupId:project:jar:1.0:compile"));
//    //        assertTrue(findString(contents, "testGroupId:snapshot:jar:2.0-SNAPSHOT:compile"));
//    //        assertTrue(findString(contents, "testGroupId:release:jar:1.0:compile"));
//    //    }
//    //
//    //    /**
//    //     * Help finding content in the given list of string
//    //     * @param outputFile the outputFile.
//    //     * @param format The format.
//    //     * @return list of strings in the output file
//    //     * @throws Exception in case of an error.
//    //     */
//    //    private List<String> runTreeMojo(String outputFile, String format)
//    //            throws Exception {
//    //        File testPom = new File(getBasedir(), "target/test-classes/unit/tree-test/plugin-config.xml");
//    //        String outputFileName = testDir.getAbsolutePath() + outputFile;
//    //        PrintMojo mojo = (PrintMojo) lookupMojo("print", testPom);
//    //        setVariableValueToObject(mojo, "outputType", format);
//    //        setVariableValueToObject(mojo, "outputFile", new File(outputFileName));
//    //
//    //        assertNotNull(mojo);
//    //        assertNotNull(mojo.getProject());
//    //        MavenProject project = mojo.getProject();
//    //        project.setArtifact(this.stubFactory.createArtifact("testGroupId", "project", "1.0"));
//    //
//    //        Set<Artifact> artifacts = this.stubFactory.getScopedArtifacts();
//    //        Set<Artifact> directArtifacts = this.stubFactory.getReleaseAndSnapshotArtifacts();
//    //        artifacts.addAll(directArtifacts);
//    //
//    //        project.setArtifacts(artifacts);
//    //        project.setDependencyArtifacts(directArtifacts);
//    //
//    //        mojo.execute();
//    //
//    //        BufferedReader fp1 = new BufferedReader(new FileReader(outputFileName));
//    //        List<String> contents = new ArrayList<String>();
//    //
//    //        String line;
//    //        while ((line = fp1.readLine()) != null) {
//    //            contents.add(line);
//    //        }
//    //        fp1.close();
//    //
//    //        return contents;
//    //    }
//    //
//    //    /**
//    //     * Help finding content in the given list of string
//    //     * @param contents The contents.
//    //     * @param str The content which should be checked for.
//    //     */
//    //    private boolean findString(List<String> contents, String str) {
//    //        for (String line : contents) {
//    //            if (line.contains(str)) {
//    //                // if match then return here
//    //                return true;
//    //            }
//    //        }
//    //
//    //        // in case no match for the whole list
//    //        return false;
//    //    }
//    //
//    // private methods --------------------------------------------------------
//
//    private fun assertChildNodeEquals(expectedNode: String, actualParentNode: DependencyNode, actualChildIndex: Int) {
//        val actualNode = actualParentNode.children[actualChildIndex]
//        assertNodeEquals(expectedNode, actualNode)
//    }
//
//    private fun assertNodeEquals(expectedNode: String, actualNode: DependencyNode) {
//        val tokens = expectedNode.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//        assertNodeEquals(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], actualNode)
//    }
//
//    private fun assertNodeEquals(expectedGroupId: String, expectedArtifactId: String, expectedType: String,
//                                 expectedVersion: String, expectedScope: String, actualNode: DependencyNode) {
//        val actualArtifact = actualNode.artifact
//        TestCase.assertEquals("group id", expectedGroupId, actualArtifact.groupId)
//        TestCase.assertEquals("artifact id", expectedArtifactId, actualArtifact.artifactId)
//        TestCase.assertEquals("type", expectedType, actualArtifact.type)
//        TestCase.assertEquals("version", expectedVersion, actualArtifact.version)
//        TestCase.assertEquals("scope", expectedScope, actualArtifact.scope)
//    }
}

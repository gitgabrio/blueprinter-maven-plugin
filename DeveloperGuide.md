Developer Guide
===============


This plugin leverage the [PlantUML](http://plantuml.com/) framework to create [component diagrams](http://plantuml.com/component-diagram) representing the relationship between maven modules.
Maven retrieved tree is read through.



Example
=======

    @startuml                                                   // start the document
    left to right direction
    skinparam svgLinkTarget _new
    [org.kie:bom-bb] as Component1 [[org_kie_bom-bb.svg]]       // definition of the component needed to add an hyperlink to it
    [org.kie:bom-ba] as Component2 [[org_kie_bom-ba.svg]]
    [org.kie:blueprinter-test] <-- [org.kie:bom-a] : extend     // direct "extend" definition
    [org.kie:blueprinter-test] <-- [org.kie:bom-b] : extend
    [org.kie:bom-a] ..> Component1 : import                     // referenced component in "import" - referenced component will be navigable 
    [org.kie:bom-a] ..> Component2 : import
    [org.kie:bom-a] <-- [org.kie:bom-aa] : extend
    [org.kie:blueprinter-test] <-- [org.kie:bom-a] : extend
    @enduml                                                     // end the document


Test
----
**src/it** folder contains example projects used for ~~integration tests~~. For such tests, the *maven-invoker-plugin* is used; invoke it with:

    mvn clean verify
    
At the end of execution, built artifacts are put inside **target/its** folder, while reports are written inside **target/invoker-reports**.


**src/test** folder contains ~~unit tests~~. For such tests, the *maven-surefire-plugin* is used; invoke it with:

    mvn clean test
    
Debug
-----
To debug plugin execution on test project, uncomment the *invoker.mavenOpts* line in the **invoker.properties** file. Launch the remote debugger (on port 8000) after
*maven-invoker-plugin:3.2.0:run* has been print on console 
    





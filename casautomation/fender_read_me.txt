Fender is a Data-driven Test-automation framework for CAS (Channel-Ad Server)

Its built in Java and uses the TestNG framework for running tests.

It has 4 modular parts as follows

- src/main/java - All the framework code goes here .Meat of the work happens here.
- src/main/res - Comprises of all the resources that the framework requires , or in other words all that is consumed by src/main/java.
- src/test/java - Comprises of the test cases for DCP ,IX and RTBD .All the test code go in here.
- src/test/res - Comprises of all the data-driven custom test input given by the user and the data providers for the test cases , ideally all that is separately consumed by src/test/java.


CAS configs on the partners/repo/queries/logparams are all found here - /casautomation/src/main/resource/com/inmobi/castest/casconfenums/def and /casautomation/src/main/resource/com/inmobi/castest/casconfenums/impl.
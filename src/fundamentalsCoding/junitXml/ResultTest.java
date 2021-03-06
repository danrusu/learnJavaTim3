package fundamentalsCoding.junitXml;

import utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static fundamentalsCoding.junitXml.JunitXmlTag.*;
import static fundamentalsCoding.junitXml.Severity.ERROR;
import static fundamentalsCoding.junitXml.Severity.WARNING;
import static fundamentalsCoding.junitXml.XmlUtils.xmlEndTag;
import static fundamentalsCoding.junitXml.XmlUtils.xmlStartTag;
import static java.util.Collections.emptyList;

public class ResultTest {

    // Create a Result for 4 processes,
    // each having at least one event, and at least one failure.
    public static void main(String[] args) throws IOException {
        Result result = generateMockResult();

        String xmlString = generateJunitXml(result);

        // System.out.println(xmlString);

        // write xmlString to file in Resources/out/junitXmlReport.xml
        Path junitXmlReportPath = FileUtils.getLocalPath(
                "Resources",
                "out",
                "junitXmlReport.xml"
        );
        Files.write(junitXmlReportPath, xmlString.getBytes());
    }

    private static String generateJunitXml(Result result) {
        String junitXmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        // <testsuites id="20140612_170519"
        // name="New_configuration (14/06/12 17:05:19)"
        // tests="225"
        // failures="1262"
        // time="0.001">

        // Result -> testsuites
        // ProcessResult -> testsuite
        // EventResult -> testcase
        // Failure -> Failure
        Map<String, String> testSuitesAttributes = Map.of(
                "id", result.getResultInfo().getId(),
                "name", result.getResultInfo().getName(),
                "time", result.getResultInfo().getElapsedTime(),
                "tests", result.getProcessCount(),
                "failures", result.getFailedProcessesCount()
        );

        junitXmlString += xmlStartTag(
                TEST_SUITES.toString(),
                testSuitesAttributes);

        junitXmlString += getTestSuiteTags(result.getProcessesResults());

        junitXmlString += xmlEndTag(TEST_SUITES.toString());

        return junitXmlString;
    }

    private static String getTestSuiteTags(List<ProcessResult> processesResults) {
        String testSuiteTagsString = "";
        for(ProcessResult processResult : processesResults){
            testSuiteTagsString += xmlStartTag(
                    TEST_SUITE.toString(),
                    // id="codereview.cobol.analysisProvider"
                    // name="COBOL Code Review"
                    // time="0.001"
                    // tests="45"
                    // failures="17"
                    // testcases tags
                    Map.of(
                            "id", processResult.getProcessResultInfo().getId(),
                            "name", processResult.getProcessResultInfo().getName(),
                            "time", processResult.getProcessResultInfo().getElapsedTime(),
                            "tests", processResult.getEventCount(),
                            "failures", processResult.getFailedEventsCount()
                    ));

                    // add events
                    testSuiteTagsString += getTestCasesTags(processResult.getEventsResults());

                    testSuiteTagsString += xmlEndTag(TEST_SUITE.toString());
        }

        return testSuiteTagsString;
    }

    private static String getTestCasesTags(List<EventResult> eventsResults) {

        String testCasesString = "";

        for(EventResult eventResult : eventsResults) {
            testCasesString += xmlStartTag(
                    TEST_CASE.toString(),
                    // id="codereview.cobol.rules.ProgramIdRule"
                    // name="Use a program name that matches the source file name"
                    // time="0.001">
                    Map.of(
                            "id", eventResult.getEventResultInfo().getId(),
                            "name", eventResult.getEventResultInfo().getName(),
                            "time", eventResult.getEventResultInfo().getElapsedTime())
            );

            testCasesString += getFailuresString(eventResult.getFailures());

            testCasesString += xmlEndTag(TEST_CASE.toString());
        }

        return testCasesString;
    }

    private static String getFailuresString(List<Failure> failures) {
        String failuresString = "";

        for(Failure failure : failures){
            failuresString += xmlStartTag(
                    FAILURE.toString(),
                    // message="PROGRAM.cbl:2 Use a program name that matches the source file name"
                    // type="WARNING">
                    // TextNode: WARNING: Use a program name that matches the source file name
                    Map.of(
                            "message", failure.getMessage(),
                            "type", failure.getSeverity().toString()
                    ));
            failuresString += "\n" + failure.getFailureInfo();

            failuresString += xmlEndTag(FAILURE.toString());
        }

        return failuresString;
    }

    private static Result generateMockResult() {
        // EVENTS PROCESS1
        Info info11 = new Info("LightEvent", "1/1", "100");

        EventResult eventResult11 = new EventResult(info11, emptyList());

        List<EventResult> events1 = List.of(eventResult11);

        // EVENTS PROCESS2
        Info info21 = new Info("SoftEvent", "2/1", "100");
        EventResult eventResult21 = new EventResult(info21, emptyList());
        List<EventResult> events2 = List.of(eventResult21);

        // EVENTS PROCESS3
        Info info31 = new Info("LightEvent", "3/1", "25");
        EventResult eventResult31 = new EventResult(info31, emptyList());
        Info info32 = new Info("StressEvent", "3/2", "25");
        Failure failure32 = new Failure(
                "too much stress!",
                WARNING,
                "event failed at 15:40 with a shutdown");
        EventResult eventResult32 = new EventResult(info32, List.of(failure32));
        List<EventResult> events3 = List.of(eventResult31, eventResult32);

        // EVENTS PROCESS4
        Info info41 = new Info("LightEvent", "4/1", "25");
        EventResult eventResult41 = new EventResult(info41, emptyList());
        Info info42 = new Info("LightEvent", "4/2", "20");
        EventResult eventResult42 = new EventResult(info42, emptyList());
        Info info43 = new Info("DisasterEvent", "4/3", "5");
        Failure failure43 = new Failure(
                "explosion",
                ERROR,
                "EXPLOSION !!!");
        EventResult eventResult43 = new EventResult(info43, List.of(failure43));
        List<EventResult> events4 = List.of(
                eventResult41,
                eventResult42,
                eventResult43
        );

        // PROCESSES
        Info processInfo1 = new Info("LightStressProcess", "1", "100");
        Info processInfo2 = new Info("SoftStressProcess", "2", "100");
        Info processInfo3 = new Info("StressProcess", "3", "50");
        Info processInfo4 = new Info("DisasterStressProcess", "4", "50");
        ProcessResult processResult1 = new ProcessResult(
                processInfo1,
                "1",
                "0",
                events1);

        ProcessResult processResult2 = new ProcessResult(
                processInfo2,
                "1",
                "0",
                events2);
        ProcessResult processResult3 = new ProcessResult(
                processInfo3,
                "2",
                "1",
                events3);
        ProcessResult processResult4 = new ProcessResult(
                processInfo4,
                "3",
                "1",
                events4);

        List<ProcessResult> processesResults = List.of(
                processResult1,
                processResult2,
                processResult3,
                processResult4
        );

        Info resultInfo = new Info("StressResult", "100", "300");

        return new Result(
                resultInfo,
                "4",
                "2",
                processesResults);
    }
}

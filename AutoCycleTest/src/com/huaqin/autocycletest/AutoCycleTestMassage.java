/**
 * 2016-4-8
 * AutoCycleTestMassage.java
 * TODO
 * zhouhui
 */
package com.huaqin.autocycletest;

/**
 * @author zhouhui
 *
 */
public class AutoCycleTestMassage {

    public static final int MSG_START_TEST_SERVICE = 0;
    public static final int MSG_BEGIN_AUTOTEST = 100;
    public static final int MSG_STOP_RUNTIMETEST = 101;
    public static final int MSG_RESUME_RUNTIMETEST = 102;
    public static final int MSG_REFRESH_ADAPTER_LIST = 103;
    
    public static final int MSG_REBOOT_TEST = 110;
    public static final int MSG_REBOOT_TEST_FINISHED = 111;
    
    public static final int MSG_TESTCASE_FINISHED = 200;
    public static final int MSG_TESTCASE_FAILED = 201;
    public static final int MSG_TESTCASE_ABORTED = 202;

    public static final int MSG_TESTCASE_SYSTEMLOAD_UPDATEED = 300;
    
    public static String MSG_FAILED_REASON = "Reason";
    
    public static String NOTIFY_TEST_FINISHED ="com.huaqin.autoCycle.test_finished";
    public static String NOTIFY_TEST_FAILED = "com.huaqin.autoCycle.test_failed";
      
    public static String NOTIFY_TEST_CASE_STARTED = "com.huaqin.autoCycle.test_case_started";
    public static String NOTIFY_TEST_CASE_FINISHED = "com.huaqin.autoCycle.test_case_finished";
    public static String NOTIFY_TEST_CASE_ABORTED = "com.huaqin.autoCycle.test_case_aborted";
    public static String REFRESH_ADAPTER_LIST = "com.huaqin.autoCycle.refresh_list";
}

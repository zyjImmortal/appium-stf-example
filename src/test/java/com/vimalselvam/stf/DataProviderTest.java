package com.vimalselvam.stf;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * @author zhouyajun
 * @date 2019/4/11
 */
public class DataProviderTest {
    private int param;
    @Factory(dataProvider = "dataMethod")
    public DataProviderTest(int param) {
        this.param = param;
    }

    @DataProvider
    public static Object[][] dataMethod() {
        return new Object[][] { new Object[]{ 0 }, new Object[]{ 10 } };
    }

    @BeforeClass
    public void setUp() throws Exception{
        System.out.println("param");
    }

    @Test
    public void testMethodOne() {
        int opValue = param + 1;
        System.out.println("Test method one output: " + opValue);
    }

    @Test
    public void testMethodTwo() {
        int opValue = param + 2;
        System.out.println("Test method two output: " + opValue);
    }
}

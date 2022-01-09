package util;

import org.junit.Assert;
import org.junit.Test;

public class RequestProcessorTest {

    RequestProcessor requestProcessor = new RequestProcessor();

    @Test
    public void test_getRequestType_validInput_return_validResponse() {
        Operation response = RequestProcessor.getRequestType("GET 1");
        Assert.assertEquals(response, Operation.GET);
    }

    @Test
    public void test_getRequestType_invalidInputs_return_null() {
        Operation response = RequestProcessor.getRequestType("PUT Key1 Value1");
        Assert.assertNull(response);
    }

    @Test
    public void getRequestPayload() {
    }
}
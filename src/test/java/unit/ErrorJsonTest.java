package unit;

import com.corn.controller.ErrorJson;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Oleg Zaidullin
 */
public class ErrorJsonTest {
    private ErrorJson errorJson;
    private static final String testString = "test";

    @Before
    public void setUp() {
        errorJson = new ErrorJson();
    }

    @Test
    public void messageTest() {
        errorJson.setMessage(testString);
        assertEquals(testString,errorJson.getMessage());
    }

    @Test
    public void tsTest() {
        errorJson.setTimeStamp(testString);
        assertEquals(testString,errorJson.getTimeStamp());
    }

    @Test
    public void traceTest() {
        errorJson.setTrace(testString);
        assertEquals(testString,errorJson.getTrace());
    }

    @Test
    public void statusTest() {
        errorJson.setStatus(1);
        assertEquals(1, (int) errorJson.getStatus());
    }
}

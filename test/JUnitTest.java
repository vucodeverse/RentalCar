
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;



/**
 *
 * @author DELL
 */
public class JUnitTest {

    @Test
    public void testMockitoWorks() {
        List<String> list = mock(List.class);
        when(list.size()).thenReturn(3);

        assertEquals(3, list.size());
    }

}

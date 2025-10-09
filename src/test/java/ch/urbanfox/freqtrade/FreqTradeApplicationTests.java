package ch.urbanfox.freqtrade;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("Needs context to run => credentials")
@ActiveProfiles(profiles = "test")
@SpringBootTest
public class FreqTradeApplicationTests {

    @Test
    public void contextLoads() {
    }

}

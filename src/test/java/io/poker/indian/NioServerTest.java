package io.poker.indian;

import io.poker.indian.client.NioClient;
import io.poker.indian.extension.NioServerTestExtension;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(NioServerTestExtension.class)
public class NioServerTest {

  @DisplayName("Test server run")
  @Test
  void testRun() throws IOException {

    NioClient nioClient = new NioClient(8000);
    nioClient.run();
  }
}

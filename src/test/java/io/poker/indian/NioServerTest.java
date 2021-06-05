package io.poker.indian;

import static org.assertj.core.api.Assertions.assertThat;

import io.poker.indian.client.NioClient;
import io.poker.indian.extension.NioServerTestExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(NioServerTestExtension.class)
public class NioServerTest {

  @DisplayName("Test server run")
  @Test
  void testRun() throws InterruptedException {

    NioClient nioClient = new NioClient(8000);
    (new Thread(nioClient)).start();

    Thread.sleep(10000);

    assertThat(NioClient.getRecentMessages().get(0)).contains("DONE");
  }
}

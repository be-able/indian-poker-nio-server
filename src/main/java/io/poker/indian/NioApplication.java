package io.poker.indian;

import io.poker.indian.server.NioServer;
import java.io.IOException;
import org.apache.log4j.BasicConfigurator;

public class NioApplication {

  public static void main(String[] args) throws IOException {
    var server = new NioServer(8000);
    (new Thread(server)).start();
    new Thread(() -> {
      try {
        Thread.sleep(1000000000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).start();
  }
}

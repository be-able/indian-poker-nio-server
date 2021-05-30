package io.poker.indian.extension;

import io.poker.indian.server.NioServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class NioServerTestExtension implements BeforeAllCallback, AfterAllCallback,
    TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback,
    AfterTestExecutionCallback, ParameterResolver {

  @Override
  public void afterAll(ExtensionContext context) throws Exception {

  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {

  }

  @Override
  public void afterTestExecution(ExtensionContext context) throws Exception {

  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    var server = new NioServer(8000);
    (new Thread(server)).start();
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {

  }

  @Override
  public void beforeTestExecution(ExtensionContext context) throws Exception {

  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) throws ParameterResolutionException {
    return false;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) throws ParameterResolutionException {
    return null;
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context)
      throws Exception {

  }
}

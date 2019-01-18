package io.github.itfinally.http.parameters;

public class VerifyResultPair {
  private boolean passed;
  private String respondingContent;

  public VerifyResultPair() {
  }

  public VerifyResultPair( boolean passed ) {
    this.passed = passed;
    this.respondingContent = "";
  }

  public VerifyResultPair( boolean passed, String respondingContent ) {
    this.passed = passed;
    this.respondingContent = respondingContent;
  }

  public boolean isPassed() {
    return passed;
  }

  public VerifyResultPair setPassed( boolean passed ) {
    this.passed = passed;
    return this;
  }

  public String getRespondingContent() {
    return respondingContent;
  }

  public VerifyResultPair setRespondingContent( String respondingContent ) {
    this.respondingContent = respondingContent;
    return this;
  }

  @Override
  public String toString() {
    return "VerifyResultPair{" +
        "passed=" + passed +
        ", respondingContent='" + respondingContent + '\'' +
        '}';
  }
}

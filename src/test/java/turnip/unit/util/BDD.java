package turnip.unit.util;

import turnip.util.Log;

/**
 Behaviour Derive Development logging util.
 <p>
 Originally implemented when I ported Kopi away from Spock.
 It doesn't do anything fancy, the methods just log the given string, but it 
 servers to structure the unit tests and having the output in-line in the 
 console logs with what the prod code logging can help a lot in larger 
 codebases.
 <p>
 I like to configure my unit test console output to highlight BDD log 
 statements via the "grep console" IDEA plugin, using this regex:
 (GIVEN:|WHEN:|THEN:|AND:|EXPECT:).*
 <p>
 Obviously, given these are just logging methods - they don't have any of the 
 restrictions that Spock enforces.
 */
public class BDD {
  private final Log log;
  
  private final static BDD bdd = new BDD(Log.to(BDD.class));  

  public BDD(Log log) {
    this.log = log;
  }

  public void log(String classifier, String descrtiption) {
    log.info(classifier.toUpperCase() + ": " + descrtiption);
  }

  /*
  lowercase methods are normal instance methods, would be used if user wanted
  to log the decriptions using the test's logger, for example.  
   */
  public void given(String description) {
    log("given", description);
  }

  public void when(String description) {
    log("when", description);
  }

  public void then(String description) {
    log("then", description);
  }

  public void and(String description) {
    log("and", description);
  }
  
  // used when when()/then() doesn't fit how you want to do it
  public void expect(String description) {
    log("expect", description);
  }
  
  /* 
  uppercase methods are static convenience, the upper case serves to make the
  calls stand out in a test case, and also to differentiate from the instance
  methods (IDEA or javac didn't like that the names only difference in case).
   */
  public static void GIVEN(String description){
    bdd.given(description);
  }
  public static void WHEN(String description){
    bdd.when(description);
  }
  public static void THEN(String description){
    bdd.then(description);
  }
  public static void AND(String description){
    bdd.and(description);
  }
  public static void EXPECT(String description){
    bdd.expect(description);
  }
}


package turnip.unit.util;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class JWT {

  /**
   Allows us to create "valid" JWTs of any shape (even invalid, malicious 
   tokens).
   This let's us test with malformed but valid tokens to ensure our JWT 
   code/libraries can handle them.
   If we use a proper library for creating these - it would reject many 
   "invalid" combinations that we might want to test with.
   <p>
   Other points to note:
   - serves as a simple illustration of exactly how simple bearer tokens are 
   (many people think they're a lot more complex than they really are)
   - explicitly demonstrates the requirements around url-encoding and 
   pading-stripping (which I don't seem to be doing any more?)
   <p>
   Note: this code is not suitable for use in production (edge cases like 
   character encoding not
   considered, etc.)
   <p>
   Taken from https://metamug.com/article/security/jwt-java-tutorial-create-verify.html
   */
  public static String createJwt(String header, String payload, String secret) {
    String message = encodeBase64String(header) + "." + 
      encodeBase64String(payload);
    String signature = hmacSha256(message, secret);
    return message + "." + signature;
  }

  public static String encodeBase64String(String val) {
    return Base64.getEncoder().
      withoutPadding().
      encodeToString(val.getBytes());
  }

  public static String hmacSha256(String data, String secret) {
    try {

      byte[] hash = secret.getBytes(StandardCharsets.UTF_8);
      Mac sha256Hmac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
      sha256Hmac.init(secretKey);

      byte[] signedBytes =
        sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

      return encodeBase64Bytes(signedBytes);
    }
    catch( NoSuchAlgorithmException | InvalidKeyException ex ){
      throw new RuntimeException(ex);
    }
  }

  private static String encodeBase64Bytes(byte[] bytes) {
    // note getUrlEncoder() is why I don't need to do escaping/stripping now
    return Base64.getUrlEncoder().
      withoutPadding().
      encodeToString(bytes);
  }
  
// ----- util functions for analysing a JWT ----

  public static byte[] decodeBase64(String val) {
    // note getUrlDecoder() is why don't need to do unescaping/stripping
    return Base64.getUrlDecoder().decode(val);
  }

  public record JwtToken(
    String header, String payload, byte[] signature
  ) {
  }

  /**
   Doesn't verify the token, just does parsing and base64 decoding so we can 
   look inside it easily
   */
  public static JwtToken debugJwt(String token) {
    var components = token.split("\\.");
    byte[] header = decodeBase64(components[0]);
    byte[] payload = decodeBase64(components[1]);
    byte[] signature = decodeBase64(components[2]);

    return new JwtToken(new String(header), new String(payload), signature);
  }

}

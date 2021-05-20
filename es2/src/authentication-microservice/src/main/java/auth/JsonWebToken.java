package auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonWebToken extends Data {

    private static final String secret = "secret";

    private static final BiFunction<String, String, byte[]> MAC_FUNC = SecurityUtils::simpleMAC;

    private static final Logger logger = LoggerFactory.getLogger(JsonWebToken.class);
    private static final long TOKEN_DURATION = 15;

    private Header header = new Header();

    private Payload payload = new Payload();

    private String signature;

    public JsonWebToken() {

    }

    public JsonWebToken(Header header, Payload payload) {
        this.header = header;
        this.payload = payload;
    }

    public Header getHeader() {
        return header;
    }

    public JsonWebToken setHeader(Header header) {
        this.header = header;
        return this;
    }

    public Payload getPayload() {
        return payload;
    }

    public JsonWebToken setPayload(Payload payload) {
        this.payload = payload;
        return this;
    }

    public String getSignature() {
        return signature;
    }

    public JsonWebToken setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public String toBase64Unsigned() {
        final String headerB64 = SecurityUtils.stringToBase64(header.toJSONString());
        final String payloadB64 = SecurityUtils.stringToBase64(payload.toJSONString());
        return headerB64 + "." + payloadB64;
    }

    public String toBase64Signed(String secret) {
        if (checkSignature(secret)) {
            return toBase64Unsigned() + "." + signature;
        } else {
            throw new IllegalStateException("Invalid signature: " + signature);
        }
    }

    public String createToken(JsonObject user){
        String token;
        String id = user.getString("id");
        String name = user.getString("name");
        String username = user.getString("username");
        String email = user.getString("email");
        String password = user.getString("password");
        String role = user.getString("role");
        getHeader().setAlg(Header.ALG_SIMPLE_HMAC).setTyp(Header.TYP_JWT);
        getPayload().setIat(OffsetDateTime.now())
                .setNbf(OffsetDateTime.now().minus(Duration.ofSeconds(1)))
                .setExp(OffsetDateTime.now().plus(Duration.ofMinutes(TOKEN_DURATION)))
                .setJti(UUID.randomUUID().toString())
                .setUser(id, name, username, email, password, role);
        sign(secret);
        token = toBase64Signed(secret);
        return token;
    }


    public String getRole(String token) {
        String role;
        JsonWebToken jsonWebToken = null;
        try {
            jsonWebToken = fromBase64(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(jsonWebToken.verify(secret)){
            role = jsonWebToken.getPayload().getUser().getRole();
            return role;

        } else {
            return "Unauthorized User";
        }
    }

    public String hashPwd(String pwd){
        return SecurityUtils.bytesToString(SecurityUtils.hmac256(pwd, secret));
    }


    public boolean verifyToken(String token)  {
        JsonWebToken jsonWebToken = null;
        try {
            jsonWebToken = fromBase64(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(jsonWebToken.verify(secret)){
                return true;
            } else {
                return false;
            }
    }

    private byte[] generateSignature(String secret) {
        if (getHeader() == null) {
            throw new IllegalStateException("Header.alg field must be defined");
        }

        if (Header.ALG_HMAC_SHA_256.equals(getHeader().getAlg())) {
            return SecurityUtils.hmac256(toBase64Unsigned(), secret);
        } else if (Header.ALG_HMAC_SHA_512.equals(getHeader().getAlg())) {
            return SecurityUtils.hmac512(toBase64Unsigned(), secret);
        } else if (Header.ALG_SIMPLE_HMAC.equals(getHeader().getAlg())) {
            return SecurityUtils.simpleHMAC(toBase64Unsigned(), secret);
        }  else if (Header.ALG_SIMPLE_MAC.equals(getHeader().getAlg())) {
            return SecurityUtils.simpleMAC(toBase64Unsigned(), secret);
        } else {
            throw new IllegalArgumentException("MAC algorithm not supported: " + getHeader().getAlg());
        }
    }

    /**
     * Produces the signature of the current JWT according to the current value of its header & payload.
     * The algorithm employed for the signature is the one specified within the Header.
     * The signature consist of a base64url-encoded string
     *
     * @return the current JWT
     */
    public JsonWebToken sign(String secret) {
        return setSignature(SecurityUtils.bytesToBase64(generateSignature(secret)));
    }

    /**
     * Check if the JWT signature is correct, by re-signing its header&payload and comparing it with the current signature, if present.
     * If no signature is present, this check must fail.
     *
     * The signature must be produced by means of the algorithm declared into the JWT header
     *
     * @return a boolean
     */
    public boolean checkSignature(String secret) {
        return getSignature() != null && getSignature().equals(SecurityUtils.bytesToBase64(generateSignature(secret)));
    }

    /**
     * Check if the JWT is consistent, i.e.: </br>
     * <ul>
     *     <li>the <code>nbf</code> timestamp is BEFORE OR EXACTLY the current time</li>
     *     <li>the <code>exp</code> timestamp is AFTER OR EXACTLY the current time</li>
     * </ul>
     *
     * @return a boolean
     */
    public boolean checkConsistency() {
        final OffsetDateTime nbf = getPayload().getNbf();
        if (nbf != null && nbf.compareTo(OffsetDateTime.now()) > 0) {
            return false;
        }

        final OffsetDateTime exp = getPayload().getExp();
        if (exp != null && exp.compareTo(OffsetDateTime.now()) < 0) {
            return false;
        }

        return true;
    }

    public boolean verify(String secret) {
        return checkSignature(secret) && checkConsistency();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonWebToken simpleJWT = (JsonWebToken) o;
        return Objects.equals(header, simpleJWT.header) &&
                Objects.equals(payload, simpleJWT.payload) &&
                Objects.equals(signature, simpleJWT.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, payload, signature);
    }

    @Override
    public String toString() {
        return "SimpleJWT{" +
                "header=" + header +
                ", payload=" + payload +
                ", signature='" + signature + '\'' +
                '}';
    }

    public static JsonWebToken fromJSON(String payload) throws IOException {
        return fromJSON(payload, JsonWebToken.class);
    }

    /**
     * Parse the provided string as a dot-separated, base64url-encoded JWT with optional signature
     *
     * @param payload the string to be parsed
     * @return
     * @throws IOException
     */
    public static JsonWebToken fromBase64(String payload) throws IOException {
        final String[] split = payload.split("\\.");
        if (split.length < 2 || split.length > 3) {
            throw new IllegalArgumentException();
        }

        final JsonWebToken result = new JsonWebToken(
                Header.fromBase64(split[0]),
                Payload.fromBase64(split[1])
        );

        if (split.length == 3) {
            result.setSignature(split[2]);
        }

        return result;
    }

    /**
     * An usage example for the JsonWebToken class
     */
    /*public static void main(String[] args) throws IOException {
        JsonWebToken jwt = new JsonWebToken();
        String token;

        jwt.getHeader().setAlg(Header.ALG_SIMPLE_HMAC).setTyp(Header.TYP_JWT);
        jwt.getPayload().setIat(OffsetDateTime.now())
                .setNbf(OffsetDateTime.now().plus(Duration.ofSeconds(1)))
                .setExp(OffsetDateTime.now().plus(Duration.ofHours(1)))
                .setJti(UUID.randomUUID().toString())
                .setUser(new UserData().setUsername("username").setRole(UserData.Role.USER));
        System.out.println(jwt.toBase64Unsigned());
        jwt.sign(secret);

        System.out.println(jwt.toJSONString());
        System.out.println(jwt = fromJSON(jwt.toJSONString()));

        System.out.println(jwt.checkSignature(SecretStorage.getInstance()));
        System.out.println(token = jwt.toBase64Signed(SecretStorage.getInstance()));

        System.out.println(JsonWebToken.fromBase64(token));
    }*/

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Header extends Data {

        public static final String ALG_HMAC_SHA_512 = "HS512";
        public static final String ALG_HMAC_SHA_256 = "HS256";
        public static final String ALG_SIMPLE_HMAC = "SIMPLE_HMAC";
        public static final String ALG_SIMPLE_MAC = "SIMPLE_MAC";

        public static final String TYP_JWT = "JWT";

        private String typ;
        private String alg;
        private String cty;

        public Header() {
        }

        public Header(String alg, String typ) {
            this(alg, typ, null);
        }

        public Header(String alg, String typ, String cty) {
            this.typ = typ;
            this.alg = alg;
            this.cty = cty;
        }

        public String getTyp() {
            return typ;
        }

        public String getAlg() {
            return alg;
        }

        public String getCty() {
            return cty;
        }

        public Header setTyp(String typ) {
            this.typ = typ;
            return this;
        }

        public Header setAlg(String alg) {
            this.alg = alg;
            return this;
        }

        public Header setCty(String cty) {
            this.cty = cty;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Header header = (Header) o;
            return Objects.equals(typ, header.typ) &&
                    Objects.equals(alg, header.alg) &&
                    Objects.equals(cty, header.cty);
        }

        @Override
        public int hashCode() {
            return Objects.hash(typ, alg, cty);
        }

        @Override
        public String toString() {
            return "Header{" +
                    "typ='" + typ + '\'' +
                    ", alg='" + alg + '\'' +
                    ", cty='" + cty + '\'' +
                    '}';
        }

        public static Header fromJSON(String payload) throws IOException {
            return fromJSON(payload, Header.class);
        }

        public static Header fromBase64(String payload) throws IOException {
            return fromJSON(SecurityUtils.base64ToString(payload));
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Payload extends Data {
        private String iss;
        private String sub;
        private List<String> aud;
        private OffsetDateTime exp;
        private OffsetDateTime nbf;
        private OffsetDateTime iat;
        private String jti;
        private UserData user;

        public Payload() {

        }

        public Payload(String iss, String sub, List<String> aud, OffsetDateTime exp, OffsetDateTime nbf, OffsetDateTime iat, String jti) {
            this.iss = iss;
            this.sub = sub;
            this.aud = aud;
            this.exp = exp;
            this.nbf = nbf;
            this.iat = iat;
            this.jti = jti;
        }

        public String getIss() {
            return iss;
        }

        public Payload setIss(String iss) {
            this.iss = iss;
            return this;
        }

        public String getSub() {
            return sub;
        }

        public Payload setSub(String sub) {
            this.sub = sub;
            return this;
        }

        public List<String> getAud() {
            return aud;
        }

        public Payload setAud(List<String> aud) {
            this.aud = aud;
            return this;
        }

        public Payload setAudElements(String... aud) {
            this.aud = Arrays.asList(aud);
            return this;
        }

        public OffsetDateTime getExp() {
            return exp;
        }

        public Payload setExp(OffsetDateTime exp) {
            this.exp = exp;
            return this;
        }

        public OffsetDateTime getNbf() {
            return nbf;
        }

        public Payload setNbf(OffsetDateTime nbf) {
            this.nbf = nbf;
            return this;
        }

        public OffsetDateTime getIat() {
            return iat;
        }

        public Payload setIat(OffsetDateTime iat) {
            this.iat = iat;
            return this;
        }

        public String getJti() {
            return jti;
        }

        public Payload setJti(String jti) {
            this.jti = jti;
            return this;
        }

        @JsonProperty("x-user")
        public UserData getUser() {
            return user;
        }

        public void setUser(String id, String name, String username, String email, String password, String role) {
            this.user = new UserData(id, name, username, email, password, role);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Payload payload = (Payload) o;
            return Objects.equals(iss, payload.iss) &&
                    Objects.equals(sub, payload.sub) &&
                    Objects.equals(aud, payload.aud) &&
                    Objects.equals(exp, payload.exp) &&
                    Objects.equals(nbf, payload.nbf) &&
                    Objects.equals(iat, payload.iat) &&
                    Objects.equals(jti, payload.jti) &&
                    Objects.equals(user, payload.user);
        }

        @Override
        public int hashCode() {
            return Objects.hash(iss, sub, aud, exp, nbf, iat, jti);
        }

        @Override
        public String toString() {
            return "Payload{" +
                    "iss='" + iss + '\'' +
                    ", sub='" + sub + '\'' +
                    ", aud=" + aud +
                    ", exp=" + exp +
                    ", nbf=" + nbf +
                    ", iat=" + iat +
                    ", jti='" + jti + '\'' +
                    ", user=" + user +
                    '}';
        }

        public static Payload fromJSON(String payload) throws IOException {
            return fromJSON(payload, Payload.class);
        }

        public static Payload fromBase64(String payload) throws IOException {
            return fromJSON(SecurityUtils.base64ToString(payload));
        }
    }
}

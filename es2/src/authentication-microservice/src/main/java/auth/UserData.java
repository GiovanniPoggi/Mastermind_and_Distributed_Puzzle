package auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.IOException;
import java.util.Objects;

@JacksonXmlRootElement(localName = "user")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserData extends Data {

    private String id = null;
    private String username = null;
    private String fullName = null;
    private String email = null;
    private String password = null;
    private String role = null;

    public UserData() { }

    public UserData(UserData clone) {
        this.id = clone.id;
        this.username = clone.username;
        this.fullName = clone.fullName;
        this.email = clone.email;
        this.password = clone.password;
        this.role = clone.role;
    }

    public UserData(String id, String fullName, String username, String email,String password,  String role) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public String getId() {
        return id;
    }

    public UserData setId(String id) {
        this.id = id;
        return this;
    }


    @JsonProperty("username")
    @JacksonXmlProperty(localName = "username")
    public String getUsername() {
        return username;
    }

    public UserData setUsername(String username) {
        this.username = username;
        return this;
    }


    @JsonProperty("fullName")
    @JacksonXmlProperty(localName = "fullName")
    public String getFullName() {
        return fullName;
    }

    public UserData setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }


    @JsonProperty("email")
    @JacksonXmlProperty(localName = "email")
    public String getEmail() {
        return email;
    }

    public UserData setEmail(String email) {
        this.email = email;
        return this;
    }

    @JsonProperty("password")
    @JacksonXmlProperty(localName = "password")
    public String getPassword() {
        return password;
    }

    public UserData setPassword(String password) {
        this.password = password;
        return this;
    }

    @JsonProperty("role")
    @JacksonXmlProperty(localName = "role")
    public String getRole() {
        return role;
    }

    public UserData setRole(String role) {
        this.role = role;
        return this;
    }

    public UserData setPropertiesToNonNullsOf(UserData other) {
        assignIfNonNull(other::getId, this::setId);
        assignIfNonNull(other::getEmail, this::setEmail);
        assignIfNonNull(other::getFullName, this::setFullName);
        assignIfNonNull(other::getPassword, this::setPassword);
        assignIfNonNull(other::getRole, this::setRole);
        assignIfNonNull(other::getUsername, this::setUsername);
        return this;
    }

    public boolean sameUserOf(UserData user) {
        return user != null && (
                (id != null && Objects.equals(id, user.id))
                        || (username != null && Objects.equals(username, user.username))
                        || (email != null && Objects.equals(email, user.email))
        );
    }

    public boolean isIdentifiedBy(String identifier) {
        return identifier != null && (
                (id != null && Objects.equals(id.toString(), identifier))
                        || Objects.equals(username, identifier)
                        || Objects.equals(email, identifier)
        );
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserData user = (UserData) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(username, user.username) &&
                Objects.equals(fullName, user.fullName) &&
                Objects.equals(email, user.email) &&
                Objects.equals(role, user.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, fullName, email, role);
    }

    public boolean checkPassword(String password) {
        return SecurityUtils.hashPassword(Objects.requireNonNull(password)).equals(getPassword());
    }

    public UserData hashPassword() {
        return setPassword(SecurityUtils.hashPassword(Objects.requireNonNull(getPassword())));
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", password='" + password + '\'' +
                '}';
    }

    public static UserData fromJSON(String representation) throws IOException {
        return fromJSON(representation, UserData.class);
    }

    public static UserData fromYAML(String representation) throws IOException {
        return fromYAML(representation, UserData.class);
    }

    public static UserData fromXML(String representation) throws IOException {
        return fromXML(representation, UserData.class);
    }

    public static UserData parse(String mimeType, String payload) throws IOException {
        return parse(mimeType, payload, UserData.class);
    }

}

package mongo;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * This class represents the information of the User saved into the Database.
 */
@DataObject(generateConverter = true)
public class MongoDBUsers {

    /**
     * Fields that represent the User's information.
     */
    private String id;
    private String name;
    private String username;
    private String email;
    private String password;
    private String role;

    /**
     * Constructor of the User's Account.
     *
     * @param other - Param that have all information of the User to save.
     */
    public MongoDBUsers(MongoDBUsers other) {
        this.id = other.id;
        this.name = other.name;
        this.username = other.username;
        this.email = other.email;
        this.password = other.password;
        this.role = other.role;
    }

    /**
     * Constructor to Convert Json Object to MongoDBUsers Object.
     *
     * @param json - Json Object of the Information
     */
    public MongoDBUsers(JsonObject json) {
        Converter.fromJsonUser(json, this);
    }

    /**
     * Method to Convert MongoDBUSers Object Information into Json.
     *
     * @return json object
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        Converter.toJsonUser(this, json);
        return json;
    }

    /**
     * Get the ID of the User.
     *
     * @return id of the User.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the ID of the User.
     *
     * @param id - New id of the User.
     * @return id of the User.
     */
    public MongoDBUsers setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the Name of the User.
     *
     * @return the name of the User.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the Name of the User.
     *
     * @param name - New Name of the User.
     * @return name of the User.
     */
    public MongoDBUsers setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the Username of the User.
     *
     * @return the Username of the User.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the Username of the User.
     *
     * @param username - New Username of the User.
     * @return username of the User.
     */
    public MongoDBUsers setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Get the Password of the User.
     *
     * @return the Password of the User.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the Password of the User.
     *
     * @param password - New password of the User.
     * @return password of the User.
     */
    public MongoDBUsers setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get the Email of the User.
     *
     * @return the Email of the User.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the Email of the User.
     *
     * @param email - New email of the User.
     * @return email of the User.
     */
    public MongoDBUsers setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Get the Role of the User.
     *
     * @return the Role of the User.
     */
    public String getRole() {
        return role;
    }

    /**
     * Set the Role of the User.
     *
     * @param role - New role of the User.
     * @return role of the User.
     */
    public MongoDBUsers setRole(String role) {
        this.role = role;
        return this;
    }

    /**
     * Print the Json that contains all Information of the User.
     *
     * @return String of User's Information.
     */
    @Override
    public String toString() {
        return toJson().encodePrettily();
    }
}

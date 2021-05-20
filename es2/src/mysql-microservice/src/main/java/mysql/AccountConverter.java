package mysql;

import io.vertx.core.json.JsonObject;

/**
 * This Class manages the Conversion Json-Account and Account-Json.
 */
public class AccountConverter {

    /**
     * Method that convert Json Object to Account Object.
     *
     * @param json - Json of the User Information.
     * @param obj - Reference to Account Object to put inside all User Information.
     */
    public static void fromJson(JsonObject json, Account obj) {
        if (json.getValue("id") instanceof String) {
            obj.setId((String)json.getValue("id"));
        }
        if (json.getValue("name") instanceof String) {
            obj.setName((String)json.getValue("name"));
        }
        if (json.getValue("username") instanceof String) {
            obj.setUsername((String)json.getValue("username"));
        }
        if (json.getValue("email") instanceof String) {
            obj.setEmail((String)json.getValue("email"));
        }
        if (json.getValue("password") instanceof String) {
            obj.setPassword((String)json.getValue("password"));
        }
        if (json.getValue("role") instanceof String) {
            obj.setRole((String)json.getValue("role"));
        }
    }

    /**
     * Method that convert Account Object to Json Object.
     *
     * @param obj - Account Object of the User Information
     * @param json - Reference to Json Object to put inside all User Information.
     */
    public static void toJson(Account obj, JsonObject json) {
        if (obj.getId() != null) {
            json.put("id", obj.getId());
        }
        if (obj.getName() != null) {
            json.put("name", obj.getName());
        }
        if (obj.getUsername() != null) {
            json.put("username", obj.getUsername());
        }
        if (obj.getEmail() != null) {
            json.put("email", obj.getEmail());
        }
        if (obj.getPassword() != null) {
            json.put("password", obj.getPassword());
        }
        if (obj.getRole() != null) {
            json.put("role", obj.getRole());
        }

    }
}
package apiautomation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.Status; // Required to log status

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class allapis {

    private final String BASE_URL = "https://thinking-tester-contact-list.herokuapp.com";
    
    // Constant variables
    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final String INITIAL_EMAIL = "test." + UNIQUE_ID + "@fake.com";
    private static final String UPDATED_EMAIL = "test.updated." + UNIQUE_ID + "@fake.com";
    private static final String INITIAL_PASSWORD = "myPassword";
    private static final String UPDATED_PASSWORD = "myNewPassword";
    private static final String CONTACT_EMAIL = "jdoe@fake.com";
    private static final String UPDATED_CONTACT_EMAIL = "amiller@fake.com";

    private static String authToken;
    private static String contactId;
    
    public static ExtentReports extent; 
    public static ExtentTest test; // Static to be accessible in all test methods

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    // --- ExtentReports Setup ---
    @BeforeSuite 
    public void setupExtent() { 
        // Use 'test-output' folder as the location for the report
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter("test output/ExtentReport.html"); 
        extent = new ExtentReports(); 
        extent.attachReporter(sparkReporter); 
        extent.setSystemInfo("Tester", "Ramgopal"); 
   }
   
    // --- ExtentReports Teardown ---
    @AfterSuite
    public void tearDown() { 
        // This is the critical step to write the data to the HTML file
        extent.flush(); 
    }

    // -----------------------------------------------------
    //                         TEST CASES
    // -----------------------------------------------------

    @Test(priority = 1, description = "Registers a new unique user.")
    public void testAddNewUser() {
        // CRITICAL STEP 1: Create a new test entry for this method
        test = extent.createTest("Test Case 1: Add New User", "Registers a new unique user.");
        
        try {
            Map<String, Object> userPayload = new HashMap<>();
            userPayload.put("firstName", "Test");
            userPayload.put("lastName", "User");
            userPayload.put("email", INITIAL_EMAIL); 
            userPayload.put("password", INITIAL_PASSWORD);

            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(userPayload)
                .when()
                    .post("/users")
                .then()
                    .statusCode(201)
                    .extract().response();

            authToken = response.jsonPath().getString("token");
            test.log(Status.PASS, "User successfully registered. Token extracted.");
        } catch (AssertionError e) {
            test.log(Status.FAIL, "User registration failed: " + e.getMessage());
            throw e; 
        }
    }
    
    // --- Test Case 2: Get user Profile ---
    @Test(priority = 2, description = "Retrieves the newly created user's profile.")
    public void testGetUserProfile() {
        // CRITICAL STEP 1: Create a new test entry for this method
        test = extent.createTest("Test Case 2: Get User Profile", "Retrieves the newly created user's profile.");
        
        try {
            given()
                    .header("Authorization", "Bearer " + authToken)
                .when()
                    .get("/users/me")
                .then()
                    .statusCode(200)
                    .body("email", equalTo(INITIAL_EMAIL)); 
            
            test.log(Status.PASS, "User profile retrieved and email validated: " + INITIAL_EMAIL);
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Get User Profile failed: " + e.getMessage());
            throw e;
        }
    }

    // --- Test Case 3: Update User ---
    @Test(priority = 3, description = "Updates user's details, including email and password.")
    public void testUpdateUser() {
        test = extent.createTest("Test Case 3: Update User", "Updates user's details.");
        try {
            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("firstName", "Updated");
            updatePayload.put("lastName", "Username");
            updatePayload.put("email", UPDATED_EMAIL);
            updatePayload.put("password", UPDATED_PASSWORD);

            given()
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(ContentType.JSON)
                    .body(updatePayload)
                .when()
                    .patch("/users/me")
                .then()
                    .statusCode(200)
                    .body("email", equalTo(UPDATED_EMAIL)); 
            test.log(Status.PASS, "User successfully updated. New email: " + UPDATED_EMAIL);
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Update User failed: " + e.getMessage());
            throw e;
        }
    }

    // --- Test Case 4: Log In User (to get a new token with updated credentials) ---
    @Test(priority = 4, description = "Logs in with updated credentials to get a fresh token.")
    public void testLoginUser() {
        test = extent.createTest("Test Case 4: Login User", "Logs in with updated credentials to get a fresh token.");
        try {
            Map<String, String> loginPayload = new HashMap<>();
            loginPayload.put("email", UPDATED_EMAIL);
            loginPayload.put("password", UPDATED_PASSWORD);

            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(loginPayload)
                .when()
                    .post("/users/login")
                .then()
                    .statusCode(200)
                    .extract().response();
            
            // Update token after successful login
            authToken = response.jsonPath().getString("token");
            test.log(Status.PASS, "Login successful. New token retrieved.");
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Login User failed: " + e.getMessage());
            throw e;
        }
    }

    // -----------------------------------------------------
    //                         CONTACT TESTS
    // -----------------------------------------------------

    // --- Test Case 5: Add Contact ---
    @Test(priority = 5, description = "Adds a new contact for the authenticated user.")
    public void testAddContact() {
        test = extent.createTest("Test Case 5: Add Contact", "Adds a new contact for the authenticated user.");
        try {
            Map<String, Object> contactPayload = new HashMap<>();
            contactPayload.put("firstName", "John");
            contactPayload.put("lastName", "Doe");
            contactPayload.put("birthdate", "1970-01-01");
            contactPayload.put("email", CONTACT_EMAIL);
            contactPayload.put("phone", "8005555555");
            contactPayload.put("street1", "1 Main St.");
            contactPayload.put("city", "Anytown");
            contactPayload.put("stateProvince", "KS");
            contactPayload.put("postalCode", "12345");
            contactPayload.put("country", "USA");

            Response response = given()
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(ContentType.JSON)
                    .body(contactPayload)
                .when()
                    .post("/contacts")
                .then()
                    .statusCode(201)
                    .body("firstName", equalTo("John"))
                    .extract().response();

            // Extract contactId for subsequent contact tests
            contactId = response.jsonPath().getString("_id");
            test.log(Status.PASS, "Contact successfully added. ID: " + contactId);
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Add Contact failed: " + e.getMessage());
            throw e;
        }
    }

    // --- Test Case 6: Get Contact List ---
    @Test(priority = 6, description = "Verifies the newly added contact is in the list.")
    public void testGetContactList() {
        test = extent.createTest("Test Case 6: Get Contact List", "Verifies the newly added contact is in the list.");
        try {
            given()
                    .header("Authorization", "Bearer " + authToken)
                .when()
                    .get("/contacts")
                .then()
                    .statusCode(200)
                    .body("size()", greaterThan(0)) // Ensure at least one contact is returned
                    .body("email", hasItem(CONTACT_EMAIL)); // Check if the added contact is in the list
            
            test.log(Status.PASS, "Contact list retrieved and new contact's email verified: " + CONTACT_EMAIL);
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Get Contact List failed: " + e.getMessage());
            throw e;
        }
    }

    // --- Test Case 7: Get Contact ---
    @Test(priority = 7, description = "Retrieves a specific contact by ID.")
    public void testGetContact() {
        test = extent.createTest("Test Case 7: Get Contact by ID", "Retrieves a specific contact by ID.");
        try {
            given()
                    .header("Authorization", "Bearer " + authToken)
                .when()
                    .get("/contacts/" + contactId)
                .then()
                    .statusCode(200)
                    .body("_id", equalTo(contactId))
                    .body("email", equalTo(CONTACT_EMAIL));
            
            test.log(Status.PASS, "Specific contact retrieved and ID/Email validated.");
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Get Contact failed: " + e.getMessage());
            throw e;
        }
    }

    // --- Test Case 8: Update Full Contact (PUT) ---
    @Test(priority = 8, description = "Performs a full update (PUT) on the contact.")
    public void testUpdateFullContact() {
        test = extent.createTest("Test Case 8: Update Full Contact (PUT)", "Performs a full update (PUT) on the contact.");
        try {
            Map<String, Object> updateContactPayload = new HashMap<>();
            updateContactPayload.put("firstName", "Amy");
            updateContactPayload.put("lastName", "Miller");
            updateContactPayload.put("birthdate", "1992-02-02");
            updateContactPayload.put("email", UPDATED_CONTACT_EMAIL);
            updateContactPayload.put("phone", "8005554242");
            updateContactPayload.put("street1", "13 School St.");
            updateContactPayload.put("city", "Washington");
            updateContactPayload.put("stateProvince", "QC");
            updateContactPayload.put("postalCode", "A1A1A1");
            updateContactPayload.put("country", "Canada");

            given()
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(ContentType.JSON)
                    .body(updateContactPayload)
                .when()
                    .put("/contacts/" + contactId)
                .then()
                    .statusCode(200)
                    .body("email", equalTo(UPDATED_CONTACT_EMAIL)) // Validate email update
                    .body("country", equalTo("Canada"));
            
            test.log(Status.PASS, "Full contact update successful. New email: " + UPDATED_CONTACT_EMAIL);
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Update Full Contact failed: " + e.getMessage());
            throw e;
        }
    }
    
    // --- Test Case 9: Update Partial Contact (PATCH) ---
    @Test(priority = 9, description = "Performs a partial update (PATCH) on the contact's first name.")
    public void testUpdatePartialContact() {
        test = extent.createTest("Test Case 9: Update Partial Contact (PATCH)", "Performs a partial update (PATCH) on the contact's first name.");
        try {
            Map<String, String> partialUpdatePayload = new HashMap<>();
            partialUpdatePayload.put("firstName", "Anna"); // Only updating first name

            given()
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(ContentType.JSON)
                    .body(partialUpdatePayload)
                .when()
                    .patch("/contacts/" + contactId)
                .then()
                    .statusCode(200)
                    .body("firstName", equalTo("Anna")) // Validate first name update
                    .body("email", equalTo(UPDATED_CONTACT_EMAIL)); // Ensure other fields are unchanged
            
            test.log(Status.PASS, "Partial contact update successful. New first name: Anna");
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Update Partial Contact failed: " + e.getMessage());
            throw e;
        }
    }

    // --- Test Case 10: Logout User ---
    @Test(priority = 10, description = "Logs out the user and invalidates the token.")
    public void testLogoutUser() {
        test = extent.createTest("Test Case 10: Logout User", "Logs out the user and invalidates the token.");
        try {
            given()
                    .header("Authorization", "Bearer " + authToken)
                .when()
                    .post("/users/logout")
                .then()
                    .statusCode(200);
            test.log(Status.PASS, "User successfully logged out and token invalidated.");
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Logout failed: " + e.getMessage());
            throw e;
        }
    }
}
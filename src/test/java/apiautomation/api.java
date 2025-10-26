package apiautomation;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class api {
	 private final String BASE_URL = "https://thinking-tester-contact-list.herokuapp.com";
	 private final String email = "ramgopal1234ww576@gmail.com";
	 private final String password = "ramgopal123";
	 private static String authToken;
	 
	 @BeforeClass
	    public void setup() {
	        RestAssured.baseURI = BASE_URL;
	    }

  @Test(priority = 1)
  public void  addnewuser() {
	  Map<String, Object> userPayload = new HashMap<>();
	  userPayload.put("firstName", "Test");
	  userPayload.put("lastName", "User");
	  userPayload.put("email", email);
	  userPayload.put("password", password);
	  Response responce = given()
			  .contentType(ContentType.JSON)
              .body(userPayload)
          .when()
              .post("/users")
          .then()
              .statusCode(201)
              .extract().response();
	  
	  authToken = responce.jsonPath().getString("token");
	  System.out.println(authToken);

  }
  @Test(priority = 2)
  public void GetuserProfile() {
	 given()
	 .header("Authorization", "Bearer" + authToken)
	 .when()
	 
	 .get("/users/me")
	 .then()
	 .statusCode(200)
	 .body("email", equalTo(email));
	  
  }
}

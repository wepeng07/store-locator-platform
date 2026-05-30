package com.mo.store_locator;

import com.mo.store_locator.service.AdminJwtService;
import org.junit.jupiter.api.BeforeEach;
import com.mo.store_locator.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
class StoreLocatorApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private AdminJwtService adminJwtService;

	private String adminToken;

	@BeforeEach
	void setUp() {
		adminToken = "Bearer " + adminJwtService.generateToken("admin-user");
	}

	@Test
	void contextLoads() {
	}

	@Test
	void getStoresReturnsSeededStores() throws Exception {
		mockMvc.perform(get("/stores"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].storeId").value("S001"))
				.andExpect(jsonPath("$[0].addressCity").value("Boston"));
	}

	@Test
	void getStoreByIdReturnsMatchingStore() throws Exception {
		Long storeId = storeRepository.findByAddressCityIgnoreCaseOrderByIdAsc("New York")
				.get(0)
				.getId();

		mockMvc.perform(get("/stores/" + storeId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.storeId").value("S002"))
				.andExpect(jsonPath("$.name").value("New York Store"))
				.andExpect(jsonPath("$.addressCity").value("New York"));
	}

	@Test
	void getStoreByIdReturnsNotFoundForUnknownId() throws Exception {
		mockMvc.perform(get("/stores/99"))
				.andExpect(status().isNotFound());
	}

	@Test
	void searchStoresByCityReturnsMatchingStores() throws Exception {
		mockMvc.perform(get("/stores/search").param("city", "Boston"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].storeId").value("S001"))
				.andExpect(jsonPath("$[0].addressCity").value("Boston"));
	}

	@Test
	void searchStoresByCityReturnsNotFoundWhenNoStoresMatch() throws Exception {
		mockMvc.perform(get("/stores/search").param("city", "Chicago"))
				.andExpect(status().isNotFound());
	}

	@Test
	void searchStoresByCoordinatesReturnsMatchingStores() throws Exception {
		mockMvc.perform(get("/stores/search")
						.param("latitude", "42.3601")
						.param("longitude", "-71.0589")
						.param("radiusMiles", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].storeId").value("S001"))
				.andExpect(jsonPath("$[0].latitude").value(42.3601))
				.andExpect(jsonPath("$[0].longitude").value(-71.0589));
	}

	@Test
	void searchStoresByCoordinatesUsesDefaultRadiusMiles() throws Exception {
		mockMvc.perform(get("/stores/search")
						.param("latitude", "42.3601")
						.param("longitude", "-71.0589"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].storeId").value("S001"))
				.andExpect(jsonPath("$[1].storeId").value("S003"));
	}

	@Test
	void searchStoresByCoordinatesSortsByDistanceAndFiltersByRadiusMiles() throws Exception {
		mockMvc.perform(get("/stores/search")
						.param("latitude", "42.3601")
						.param("longitude", "-71.0589")
						.param("radiusMiles", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].storeId").value("S001"))
				.andExpect(jsonPath("$[1].storeId").value("S003"));
	}

	@Test
	void searchStoresByCoordinatesAppliesLimit() throws Exception {
		mockMvc.perform(get("/stores/search")
						.param("latitude", "42.3601")
						.param("longitude", "-71.0589")
						.param("radiusMiles", "10")
						.param("limit", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].storeId").value("S001"));
	}

	@Test
	void searchStoresByCoordinatesReturnsEmptyListWhenNoStoresWithinRadius() throws Exception {
		mockMvc.perform(get("/stores/search")
						.param("latitude", "10.0")
						.param("longitude", "20.0")
						.param("radiusMiles", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void searchStoresByCoordinatesRequiresLatitudeAndLongitude() throws Exception {
		mockMvc.perform(get("/stores/search").param("latitude", "42.3601"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void searchStoresByCoordinatesRejectsInvalidRadiusMiles() throws Exception {
		mockMvc.perform(get("/stores/search")
						.param("latitude", "42.3601")
						.param("longitude", "-71.0589")
						.param("radiusMiles", "0"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void searchStoresByCoordinatesRejectsRadiusMilesOverMax() throws Exception {
		mockMvc.perform(get("/stores/search")
						.param("latitude", "42.3601")
						.param("longitude", "-71.0589")
						.param("radiusMiles", "101"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void adminEndpointsRequireAuthentication() throws Exception {
		mockMvc.perform(get("/api/admin/stores"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminEndpointsRejectInvalidJwt() throws Exception {
		mockMvc.perform(get("/api/admin/stores")
						.header(HttpHeaders.AUTHORIZATION, "Bearer bad-token"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Unauthorized"));
	}

	@Test
	void adminListStoresReturnsPaginatedResults() throws Exception {
		mockMvc.perform(get("/api/admin/stores")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.param("page", "0")
						.param("size", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.number").value(0))
				.andExpect(jsonPath("$.size").value(2))
				.andExpect(jsonPath("$.content[0].storeId").value("S001"));
	}

	@Test
	void adminListStoresRejectsInvalidPage() throws Exception {
		mockMvc.perform(get("/api/admin/stores")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.param("page", "-1"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("page: must be greater than or equal to 0"))
				.andExpect(jsonPath("$.path").value("/api/admin/stores"));
	}

	@Test
	void adminListStoresRejectsOversizedPageSize() throws Exception {
		mockMvc.perform(get("/api/admin/stores")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.param("size", "101"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("size: must be less than or equal to 100"))
				.andExpect(jsonPath("$.path").value("/api/admin/stores"));
	}

	@Test
	void adminGetStoreReturnsBusinessStoreDetails() throws Exception {
		mockMvc.perform(get("/api/admin/stores/S002")
						.header(HttpHeaders.AUTHORIZATION, adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.storeId").value("S002"))
				.andExpect(jsonPath("$.address.city").value("New York"))
				.andExpect(jsonPath("$.hours.mon").value("08:00-19:00"));
	}

	@Test
	void adminCreateStoreWithoutCoordinatesUsesGeocoder() throws Exception {
		String requestBody = """
				{
				  "storeId": "S0042",
				  "name": "Seattle Store",
				  "storeType": "Urban",
				  "status": "ACTIVE",
				  "address": {
				    "street": "1 Admin Plaza",
				    "city": "Seattle",
				    "state": "WA",
				    "postalCode": "98101",
				    "country": "USA"
				  },
				  "phone": "+1-206-555-0104",
				  "services": "pickup|delivery",
				  "hours": {
				    "mon": "09:00-18:00",
				    "tue": "09:00-18:00",
				    "wed": "09:00-18:00",
				    "thu": "09:00-18:00",
				    "fri": "09:00-18:00",
				    "sat": "10:00-16:00",
				    "sun": "Closed"
				  }
				}
				""";

		mockMvc.perform(post("/api/admin/stores")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.storeId").value("S0042"))
				.andExpect(jsonPath("$.status").value("active"))
				.andExpect(jsonPath("$.latitude").value(47.6062))
				.andExpect(jsonPath("$.longitude").value(-122.3321))
				.andExpect(jsonPath("$.address.city").value("Seattle"));
	}

	@Test
	void adminCreateStoreRejectsDuplicateStoreId() throws Exception {
		String requestBody = """
				{
				  "storeId": "S001",
				  "name": "Duplicate Boston Store",
				  "storeType": "Flagship",
				  "status": "ACTIVE",
				  "latitude": 42.3601,
				  "longitude": -71.0589,
				  "address": {
				    "street": "123 Main St",
				    "city": "Boston",
				    "state": "MA",
				    "postalCode": "02108",
				    "country": "USA"
				  },
				  "phone": "+1-617-555-0101",
				  "services": "pickup",
				  "hours": {
				    "mon": "09:00-18:00",
				    "tue": "09:00-18:00",
				    "wed": "09:00-18:00",
				    "thu": "09:00-18:00",
				    "fri": "09:00-18:00",
				    "sat": "10:00-16:00",
				    "sun": "Closed"
				  }
				}
				""";

		mockMvc.perform(post("/api/admin/stores")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Store with storeId S001 already exists"));
	}

	@Test
	void adminCreateStoreReturnsBadRequestWhenGeocodingFails() throws Exception {
		String requestBody = """
				{
				  "storeId": "S0050",
				  "name": "Unknown Store",
				  "storeType": "Urban",
				  "status": "ACTIVE",
				  "address": {
				    "street": "999 Unknown Road",
				    "city": "Nowhere",
				    "state": "ZZ",
				    "postalCode": "00000",
				    "country": "USA"
				  },
				  "phone": "+1-999-555-0000",
				  "services": "pickup",
				  "hours": {
				    "mon": "09:00-18:00",
				    "tue": "09:00-18:00",
				    "wed": "09:00-18:00",
				    "thu": "09:00-18:00",
				    "fri": "09:00-18:00",
				    "sat": "10:00-16:00",
				    "sun": "Closed"
				  }
				}
				""";

		mockMvc.perform(post("/api/admin/stores")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Unable to geocode store address"));
	}

	@Test
	void adminCreateStoreRejectsInvalidStoreType() throws Exception {
		String requestBody = """
				{
				  "storeId": "S0051",
				  "name": "Invalid Store Type",
				  "storeType": "Mall",
				  "status": "ACTIVE",
				  "latitude": 42.3601,
				  "longitude": -71.0589,
				  "address": {
				    "street": "123 Main St",
				    "city": "Boston",
				    "state": "MA",
				    "postalCode": "02108",
				    "country": "USA"
				  },
				  "phone": "+1-617-555-0101",
				  "services": "pickup",
				  "hours": {
				    "mon": "09:00-18:00",
				    "tue": "09:00-18:00",
				    "wed": "09:00-18:00",
				    "thu": "09:00-18:00",
				    "fri": "09:00-18:00",
				    "sat": "10:00-16:00",
				    "sun": "Closed"
				  }
				}
				""";

		mockMvc.perform(post("/api/admin/stores")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("storeType: storeType must be one of: Flagship, Urban, Neighborhood"))
				.andExpect(jsonPath("$.path").value("/api/admin/stores"));
	}

	@Test
	void adminCreateStoreRejectsInvalidHoursFormat() throws Exception {
		String requestBody = """
				{
				  "storeId": "S0052",
				  "name": "Invalid Hours Store",
				  "storeType": "Urban",
				  "status": "ACTIVE",
				  "latitude": 42.3601,
				  "longitude": -71.0589,
				  "address": {
				    "street": "123 Main St",
				    "city": "Boston",
				    "state": "MA",
				    "postalCode": "02108",
				    "country": "USA"
				  },
				  "phone": "+1-617-555-0101",
				  "services": "pickup",
				  "hours": {
				    "mon": "9am-5pm",
				    "tue": "09:00-18:00",
				    "wed": "09:00-18:00",
				    "thu": "09:00-18:00",
				    "fri": "09:00-18:00",
				    "sat": "10:00-16:00",
				    "sun": "Closed"
				  }
				}
				""";

		mockMvc.perform(post("/api/admin/stores")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("hours.mon: hours.mon must be 'Closed' or formatted as HH:mm-HH:mm"))
				.andExpect(jsonPath("$.path").value("/api/admin/stores"));
	}

	@Test
	void adminPatchStoreUpdatesAllowedFields() throws Exception {
		String requestBody = """
				{
				  "name": "Boston Store Updated",
				  "phone": "+1-617-555-9999",
				  "hours": {
				    "mon": "07:00-20:00"
				  }
				}
				""";

		mockMvc.perform(patch("/api/admin/stores/S001")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Boston Store Updated"))
				.andExpect(jsonPath("$.phone").value("+1-617-555-9999"))
				.andExpect(jsonPath("$.hours.mon").value("07:00-20:00"))
				.andExpect(jsonPath("$.hours.tue").value("09:00-18:00"));
	}

	@Test
	void adminPatchStoreRejectsForbiddenFields() throws Exception {
		String requestBody = """
				{
				  "latitude": 10.0
				}
				""";

		mockMvc.perform(patch("/api/admin/stores/S001")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Field 'latitude' is not allowed"));
	}

	@Test
	void adminPatchStoreRejectsEmptyBody() throws Exception {
		mockMvc.perform(patch("/api/admin/stores/S001")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("At least one updatable field must be provided"))
				.andExpect(jsonPath("$.path").value("/api/admin/stores/S001"));
	}

	@Test
	void adminPatchStoreRejectsInvalidStatus() throws Exception {
		String requestBody = """
				{
				  "status": "paused"
				}
				""";

		mockMvc.perform(patch("/api/admin/stores/S001")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("status: status must be one of: active, inactive"))
				.andExpect(jsonPath("$.path").value("/api/admin/stores/S001"));
	}

	@Test
	void adminPatchStoreRejectsInvalidHoursFormat() throws Exception {
		String requestBody = """
				{
				  "hours": {
				    "mon": "9am-5pm"
				  }
				}
				""";

		mockMvc.perform(patch("/api/admin/stores/S001")
						.header(HttpHeaders.AUTHORIZATION, adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("hours.mon: hours.mon must be 'Closed' or formatted as HH:mm-HH:mm"))
				.andExpect(jsonPath("$.path").value("/api/admin/stores/S001"));
	}

	@Test
	void adminDeleteStoreSoftDeletesStore() throws Exception {
		mockMvc.perform(delete("/api/admin/stores/S001")
						.header(HttpHeaders.AUTHORIZATION, adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.storeId").value("S001"))
				.andExpect(jsonPath("$.status").value("inactive"));

		mockMvc.perform(get("/api/admin/stores/S001")
						.header(HttpHeaders.AUTHORIZATION, adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("inactive"));
	}

}

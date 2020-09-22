package shamu.company.helpers.googlemaps;

import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest.FieldMask;
import com.google.maps.PlacesApi;
import com.google.maps.TimeZoneApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import java.io.IOException;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shamu.company.helpers.exception.GoogleMapApiException;

@Component
public class GoogleMapsHelper {

  private static final String ERROR_MESSAGE = "Error while getting time zone.";

  @Value("${application.googleGeoCodingApiKey}")
  private String googleGeoCodingApiKey;

  public String findTimezoneByLocation(final LatLng latLng)
      throws InterruptedException, ApiException, IOException {
    final GeoApiContext mapApiContext = new GeoApiContext.Builder().apiKey(googleGeoCodingApiKey).build();
    final TimeZone tz = TimeZoneApi.getTimeZone(mapApiContext, latLng).await();
    mapApiContext.shutdown();
    return tz.getID();
  }

  public LatLng findLocationByPlaceId(final String placeId)
      throws InterruptedException, ApiException, IOException {
    final GeoApiContext mapApiContext = new GeoApiContext.Builder().apiKey(googleGeoCodingApiKey).build();
    final PlaceDetails placeDetails;
    placeDetails =
        PlacesApi.placeDetails(mapApiContext, placeId).fields(FieldMask.GEOMETRY_LOCATION).await();
    mapApiContext.shutdown();
    return placeDetails.geometry.location;
  }

  public String findTimezoneByPlaceId(final String placeId) {
    String timezone = "";
    try {
      final LatLng location = findLocationByPlaceId(placeId);
      timezone = findTimezoneByLocation(location);
    } catch (final InterruptedException | ApiException | IOException e) {
      Thread.currentThread().interrupt();
      throw new GoogleMapApiException(ERROR_MESSAGE, e);
    }
    return timezone;
  }
}

package shamu.company.helpers.googlemaps;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.TimeZoneApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.ComponentFilter;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shamu.company.helpers.exception.GoogleMapApiException;

@Component
public class GoogleMapsHelper {

  @Value("${application.googleGeoCodingApiKey}")
  private String googleGeoCodingApiKey;

  @Value("${application.googleMapApiKey}")
  private String googleMapApiKey;

  public Map<String, String> findTimezoneByPostalCode(final Set<String> postalCodes) {
    final Map<String, String> postalTimezone = new HashMap<>();
    postalCodes.forEach(
        postalCode -> {
          try {
            final LatLng latLng;
            final Optional<LatLng> location = findLocationByPostalCode(postalCode);
            if (location.isPresent()) {
              latLng = location.get();
              final String timezone = findTimezoneByLocation(latLng);
              postalTimezone.put(postalCode, timezone);
            }
          } catch (final InterruptedException | ApiException | IOException e) {
            Thread.currentThread().interrupt();
            throw new GoogleMapApiException("Error while getting time zone.", e);
          }
        });
    return postalTimezone;
  }

  public Optional<LatLng> findLocationByPostalCode(final String postalCode)
      throws InterruptedException, ApiException, IOException {
    final GeoApiContext geoCodingApiContext =
        new GeoApiContext.Builder().apiKey(googleGeoCodingApiKey).build();
    final GeocodingResult[] results;
    results =
        GeocodingApi.geocode(geoCodingApiContext, "")
            .components(new ComponentFilter("postal_code", postalCode))
            .await();
    geoCodingApiContext.shutdown();
    return results.length == 0 ? Optional.empty() : Optional.of(results[0].geometry.location);
  }

  public String findTimezoneByLocation(final LatLng latLng)
      throws InterruptedException, ApiException, IOException {
    final GeoApiContext mapApiContext = new GeoApiContext.Builder().apiKey(googleMapApiKey).build();
    final TimeZone tz = TimeZoneApi.getTimeZone(mapApiContext, latLng).await();
    mapApiContext.shutdown();
    return tz.getID();
  }

}

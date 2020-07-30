package shamu.company.helpers;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shamu.company.helpers.googlemaps.GoogleMapsHelper;

public class GoogleMapHelperTests {
  private final GoogleMapsHelper googleMapsHelper = Mockito.mock(GoogleMapsHelper.class);

  @Test
  void whenFindTimezoneByPostalCode_thenShouldSuccess() throws InterruptedException, ApiException, IOException {
    final Optional<LatLng> latLng = Optional.of(new LatLng(1d, 1d));
    Mockito.when(googleMapsHelper.findLocationByPostalCode("1")).thenReturn(latLng);
    Mockito.when(googleMapsHelper.findTimezoneByLocation(latLng.get())).thenReturn(Mockito.anyString());

    assertThatCode(
        () -> googleMapsHelper.findTimezoneByPostalCode(new HashSet(
            Collections.singleton("1"))))
        .doesNotThrowAnyException();
  }



}

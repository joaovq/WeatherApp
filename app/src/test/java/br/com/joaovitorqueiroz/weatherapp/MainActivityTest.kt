package br.com.joaovitorqueiroz.weatherapp

import android.content.Context
import android.location.LocationManager
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MainActivityTest {
    @Mock
    private lateinit var mockContext: Context

    @Test
    fun `is location enabled test`() {
        val mockLocationManager = mock<LocationManager> {
            on {
                mockContext.getSystemService(Context.LOCATION_SERVICE)
            } doReturn LocationManager.GPS_PROVIDER
        }
    }
}

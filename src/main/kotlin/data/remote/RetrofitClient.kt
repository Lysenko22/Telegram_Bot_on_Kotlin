package data.remote

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import data.remote.api.ReversedGeocodingApi
import data.remote.api.WeatherApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val WEATHER_BASE_URL = "https://openweathermap.org/"
private const val REVERSE_GEOCODER_BASE_URL = "https://nominatim.openstreetmap.org"
const val API_KEY = "6c763d5f65d63720f44705e93e53b6f8"

enum class RetrofitType(val baseurl:String){
    WEATHER(WEATHER_BASE_URL),
    REVERSE_GEOCODER_BASE_URL(data.remote.REVERSE_GEOCODER_BASE_URL)
}

class RetrofitClient {

fun getClient(): OkHttpClient{
val logging = HttpLoggingInterceptor()
    logging.level = HttpLoggingInterceptor.Level.BODY
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
    return okHttpClient.build()
}

fun getRetrofit(retrofitType: RetrofitType):Retrofit {
return Retrofit.Builder()
    .baseUrl(retrofitType.baseurl)
        .addCallAdapterFactory(CoroutineCallAdapterFactory.invoke())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
    fun getWeatherApi(retrofit: Retrofit):WeatherApi {
        return retrofit.create(WeatherApi::class.java)
    }
    fun getReversedGeocodingApi(retrofit: Retrofit):ReversedGeocodingApi {
        return retrofit.create(ReversedGeocodingApi::class.java)
    }
}
package Http;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiInterface {
    @POST("getmenu-all")
    Call<JsonElement> getbookmenu(@Header("Authorization") String apiKey);

    @POST("getmenu-0")
    Call<JsonElement> getbookmenuonly(@Header("Authorization") String apiKey);

    @POST("getmenu-{id}")
    Call<JsonElement> getbookmenubyid(@Header("Authorization") String apiKey, @Path("id") String id);

}

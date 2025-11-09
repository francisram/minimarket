package py.com.base.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

//import py.com.bepsa.logs.LogUtil;

public class HttpsClientUtils {


    public static Boolean invocarServicio(String urlServicio, String jsonEntrada, Integer timeout,String lugar) {
    Boolean enviado = null;
    HttpURLConnection con = null;

    try {
        URI uri = URI.create(urlServicio);
        URL url = uri.toURL();
        con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(timeout);
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json; charset=utf8");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");

        try (OutputStream os = con.getOutputStream()) {
            os.write(jsonEntrada.getBytes(StandardCharsets.UTF_8));
        }

        int httpResult = con.getResponseCode();
        if (httpResult == HttpURLConnection.HTTP_OK || httpResult == HttpURLConnection.HTTP_CREATED) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                br.lines().collect(Collectors.joining()); 
            }
            enviado = true;
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorDescrip = httpResult + " -- " + br.lines().collect(Collectors.joining());
                LogUtil.error(String.format("Error en "+lugar +" invocarServicio httpclient: %s json: %s", errorDescrip, jsonEntrada));
            }
            enviado = false;
        }
    } catch (IOException | RuntimeException e) {
        LogUtil.error(String.format("Exception Catch en invocarServicio httpclient en "+lugar +" : %s", e.getMessage()));
        enviado = false;
    } finally {
        if (con != null) {
            con.disconnect();
        }
    }
    return enviado;
}

}

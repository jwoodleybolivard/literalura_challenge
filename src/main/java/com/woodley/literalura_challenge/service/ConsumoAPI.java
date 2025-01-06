package com.woodley.literalura_challenge.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class ConsumoAPI {
    public String consumirApi(String url) {
        // Crea una instancia de HttpClient para realizar la solicitud HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Crea una solicitud HTTP GET con la URL proporcionada
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        // Envía la solicitud y obtiene la respuesta
        HttpResponse<String> response = null;
        try {
            response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException("Error al enviar la solicitud HTTP", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Se interrumpió la solicitud HTTP", e);
        }

        // Obtiene el cuerpo de la respuesta
        String json = response.body();
        return json;
    }
}

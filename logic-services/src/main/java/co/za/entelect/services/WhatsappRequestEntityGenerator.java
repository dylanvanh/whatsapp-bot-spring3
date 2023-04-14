package co.za.entelect.services;

import co.za.entelect.Dtos.Whatsapp.IWhatsappDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class WhatsappRequestEntityGenerator {

    private final Dotenv dotEnv;

    public WhatsappRequestEntityGenerator(Dotenv dotEnv) {
        this.dotEnv = dotEnv;
    }

    public HttpEntity<String> generateEntity(IWhatsappDto dto, boolean requiresAuth) {

        HttpHeaders httpHeaders;
        if (requiresAuth) {
            httpHeaders = generateBasicHeaderWithAuth();
        } else {
            httpHeaders = generateBasicHeader();
        }

        String body;
        try {
            body = new ObjectMapper().writeValueAsString(dto);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

        HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
        return entity;
    }

    private HttpHeaders generateBasicHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }

    private HttpHeaders generateBasicHeaderWithAuth() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(dotEnv.get("WHATSAPP_TOKEN"));
        return httpHeaders;
    }

}

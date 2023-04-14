//package co.za.entelect.services;
//
//import co.za.entelect.Dtos.Whatsapp.Outgoing.SendTextMessageDto;
//import co.za.entelect.Dtos.Whatsapp.Outgoing.UpdateReadReceiptDto;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//
//public class MessageResponseUtilService {
//
//    private HttpHeaders generateBasicHeader() {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//        return httpHeaders;
//    }
//
//
//    private HttpHeaders generateHeaderWithAuth() {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//        httpHeaders.setBearerAuth(dotEnv.get("WHATSAPP_TOKEN"));
//        return httpHeaders;
//    }
//
//    private HttpEntity<String> generateEntity(UpdateReadReceiptDto updateReadReceiptDto) {
//        HttpHeaders httpHeaders = generateHeaderWithAuth();
//
//        String body;
//        try {
//            body = new ObjectMapper().writeValueAsString(updateReadReceiptDto);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            return null;
//        }
//
//        HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
//        return entity;
//    }
//
//
//    private HttpEntity<String> generateEntity(SendTextMessageDto sendTextMessageDto) {
//        HttpHeaders httpHeaders = generateBasicHeader();
//
//        String body;
//        try {
//            body = new ObjectMapper().writeValueAsString(sendTextMessageDto);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//
//        HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
//        return entity;
//    }
//
//
//}

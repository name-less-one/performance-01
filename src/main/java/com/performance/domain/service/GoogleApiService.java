package com.performance.domain.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class GoogleApiService {

    public void execute() throws ClientProtocolException, IOException, URISyntaxException {

        RestTemplate template = new RestTemplate();
        String url = "https://www.googleapis.com/oauth2/v4/token";

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("client_id", "455165694555-gekiq4j8upinbq5kejusq7t92r7t560v.apps.googleusercontent.com"); //コンソールにAPI認証情報のクライアントID
        requestMap.add("client_secret", "6r7S3FqjssNuXM8FbKa-GLX0"); //コンソールにAPI認証情報のクライアントシークレット
        requestMap.add("refresh_token", "1//0eZTyKVLG43gACgYIARAAGA4SNwF-L9IrRRath-TLFkbhmbvE6IvKDW7xXFM6bIiNheOtcFIA8Lvr25Adkm8xNIU-L07WwxFSbCo"); //設定した承認済みのリダイレクトURI
        requestMap.add("grant_type", "refresh_token"); //固定
        
        RequestEntity<MultiValueMap<String, String>> request = RequestEntity.post(new URI(url))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestMap);

        ResponseEntity<String> responseEntity = template.exchange(request, String.class);
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> responseMap = null;
        if(responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
            String result = responseEntity.getBody();
            responseMap = mapper.readValue(responseEntity.getBody(), new TypeReference<Map<String, String>>(){});
            System.out.println(result);
            System.out.println(responseMap.get("access_token"));
        }
    }
}

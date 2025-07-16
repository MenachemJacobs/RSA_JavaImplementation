import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.util.Map;

public class MockClient {
    private static final String SERVER_URL = "http://localhost:7000";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // 1. Get server's public key (returns Map<String, String> as JSON)
        Map<String, String> serverPublicKey = restTemplate.getForObject(SERVER_URL + "/key", Map.class);
        BigInteger serverE = new BigInteger(serverPublicKey.get("e"));
        BigInteger serverN = new BigInteger(serverPublicKey.get("n"));

        // 2. Encrypt message
        String message = "Hi server!";
        int[] encryptedMessage = RSAEncryptDecryptMethods.encrypt(message, serverE, serverN);

        // 3. Create client keys
        BigInteger clientP = BigInteger.valueOf(10007);
        BigInteger clientQ = BigInteger.valueOf(10009);
        BigInteger clientN = clientP.multiply(clientQ);
        BigInteger clientPhi = clientP.subtract(BigInteger.ONE).multiply(clientQ.subtract(BigInteger.ONE));
        BigInteger clientE = BigInteger.valueOf(65537);
        BigInteger clientD = clientE.modInverse(clientPhi);

        // 4. Build Message object to send
        Message outgoing = new Message(encryptedMessage, clientE, clientN);

        // 5. Prepare HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 6. Convert message to JSON string
        String jsonRequest = objectMapper.writeValueAsString(outgoing);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequest, headers);

        // 7. POST encrypted message and receive encrypted response
        System.out.println("Sending message: " + message);
        String jsonResponse = restTemplate.postForObject(SERVER_URL + "/message", requestEntity, String.class);

        // 8. Parse encrypted response
        int[] encryptedResponse = objectMapper.readValue(jsonResponse, int[].class);

        // 9. Decrypt response with private key
        String decryptedResponse = RSAEncryptDecryptMethods.decrypt(encryptedResponse, clientD, clientN);

        System.out.println("Server response: " + decryptedResponse);
    }
}

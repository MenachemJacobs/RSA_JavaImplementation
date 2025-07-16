import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.Javalin;
import io.javalin.http.Context;
import kotlin.collections.builders.MapBuilder;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class PublicServer {
    private final BigInteger[] publicKey;
    private final BigInteger[] privateKey;

    private PublicServer() {
        BigInteger p = BigInteger.valueOf(10301);
        BigInteger q = BigInteger.valueOf(11311);
        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = BigInteger.valueOf(65537);
        BigInteger d = modInverse(e, phi);

        publicKey = new BigInteger[]{e, n};
        privateKey = new BigInteger[]{d, n};
    }

    private BigInteger modInverse(BigInteger a, BigInteger m) {
        BigInteger m0 = m;
        BigInteger x0 = BigInteger.ZERO;
        BigInteger x1 = BigInteger.ONE;

        if (m.equals(BigInteger.ONE)) return BigInteger.ZERO;

        while (a.compareTo(BigInteger.ONE) > 0) {
            BigInteger q = a.divide(m);
            BigInteger t = m;

            m = a.mod(m);
            a = t;

            t = x0;
            x0 = x1.subtract(q.multiply(x0));
            x1 = t;
        }

        if (x1.compareTo(BigInteger.ZERO) < 0)
            x1 = x1.add(m0);

        return x1.mod(m0);
    }

    public static void main(String[] args) {
        PublicServer server = new PublicServer();
        server.startAPI().start(7000);  // Start the server on port 7000
    }

    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.get("/key", this::getPublicKeysHandler);
        app.post("/message", this::postMessagesHandler);

        return app;
    }

    private void getPublicKeysHandler(Context ctx) {
        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("e", publicKey[0].toString());
        keyMap.put("n", publicKey[1].toString());
        ctx.json(keyMap);
    }

    private void postMessagesHandler(Context ctx) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Message incoming = objectMapper.readValue(ctx.body(), Message.class);

            int[] encryptedMessage = incoming.getText();
            BigInteger clientE = incoming.getE();
            BigInteger clientN = incoming.getN();

            String message = RSAEncryptDecryptMethods.decrypt(encryptedMessage, privateKey[0], privateKey[1]);
            System.out.println("Incoming message: " + message);

            // Create a response message
            String responseMessage = "Server received: " + message;

            System.out.println("Sending response: " + responseMessage);
            int[] encryptedResponse = RSAEncryptDecryptMethods.encrypt(responseMessage, clientE, clientN);
            ctx.json(encryptedResponse);
        } catch (Exception e) {
            ctx.status(400).result("Invalid input: " + e.getMessage());
        }
    }
}

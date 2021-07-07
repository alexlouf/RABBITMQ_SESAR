package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import okhttp3.*;
import org.slf4j.*;

import java.io.IOException;
import java.util.Map;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();


        String QUEUE_NAME = "v1.airbus.woc.atcvolumes";
        ConnectionFactory factory = new ConnectionFactory();
        factory.useSslProtocol();
        factory.setHost("api.dev.pansa.pl");
        factory.setPort(5671);
        factory.setVirtualHost("sesar-sol40");
        factory.setPassword(getToken(System.getenv("OFFLINE_TOKEN")));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }


    public static void main(String[] args) {
        launch(args);
    }

    private String getToken(String offlineToken) throws IOException {
        String token = "";

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "client_id=token-provider&grant_type=refresh_token&refresh_token="+offlineToken);
        Request request = new Request.Builder()
                .url("https://sso.dev.pansa.pl/auth/realms/api/protocol/openid-connect/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
        return token;
    }
}

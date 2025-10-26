package patent.patientmanagement.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc; // (1)
import io.grpc.ManagedChannel; // (2)
import io.grpc.ManagedChannelBuilder; // (3)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub; // (4)


    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.grpc.port:9001}") int serverPort
    ){ // (5)
        log.info("Connecting to billing Service at {}:{}",serverAddress,serverPort);
        // (6) بنبني القناة اللي هنكلم السيرفر من خلالها
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress,serverPort)
                .usePlaintext() // (7) بنقوله استخدم اتصال غير مشفر (للتطوير بس)
                .build();

        // (8) بنعمل حاجة اسمها "stub" وده اللي بنستخدمه عشان نكلم السيرفر
        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public BillingResponse createBillingAccount(String patientId,String name, String email){ // (9)
        // (10) بنبني الـ Request object بالبيانات اللي معانا
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(patientId).setName(name)
                .setEmail(email).build();

        // (11) بننادي الـ method على السيرفر ونستنى الرد
        BillingResponse response = blockingStub.createBillingAccount(request);
        log.info("received response from billing service: {}  ",response);
        return response; // (12)
    }
}
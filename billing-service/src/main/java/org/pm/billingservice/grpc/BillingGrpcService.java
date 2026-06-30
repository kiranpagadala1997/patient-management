package org.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

     private static final Logger logger = LoggerFactory.getLogger(BillingGrpcService.class);
    @Override
    public void createBillingAccount(BillingRequest billingRequest,
                                     StreamObserver<BillingResponse> responseObserver){

        logger.info("createBilling request received {}", billingRequest.toString());

        BillingResponse response = BillingResponse.newBuilder().setAccountId("122245").setStatus("Active").build();
        responseObserver.onNext(response);       // ← send the response
        responseObserver.onCompleted();
    }

}

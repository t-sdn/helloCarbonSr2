package kr.re.etri.hello.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev170803.*;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by labry on 02/08/2017.
 */
public class HelloWorldImpl implements HelloListener, HelloService, DataTreeChangeListener<HelloWorld> {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldImpl.class);
    private DataBroker db;
    private Long helloCounter = 0L;
    private NotificationPublishService notificationservice;

    public static final InstanceIdentifier<HelloWorld> HELLO_IID = InstanceIdentifier.builder(HelloWorld.class).build();

    public HelloWorldImpl(final DataBroker db,
                          final NotificationPublishService notificationservice) {

        this.db = db;
        this.notificationservice = notificationservice;
        LOG.info("helloWorldImpl labry init 1.3");
    }

    @Override
    public Future<RpcResult<HelloWorldWriteOutput>> helloWorldWrite(HelloWorldWriteInput input) {
        final ReadWriteTransaction tx = db.newReadWriteTransaction();

        tx.put(LogicalDatastoreType.OPERATIONAL, HELLO_IID, new HelloWorldBuilder().setNeutral(77L).setValue(input.getStrin()).build());

        tx.put(LogicalDatastoreType.CONFIGURATION, HELLO_IID,
                new HelloWorldBuilder().setCounter(++helloCounter).setNeutral(77L).build());
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("[labry]Exception: ", e);
            e.printStackTrace();
        }
        LOG.info("[labry]helloCount(write): " + helloCounter);

        HelloWorldWriteOutputBuilder helloWriteBuilder = new HelloWorldWriteOutputBuilder();
        helloWriteBuilder.setStrout(input.getStrin());

        return RpcResultBuilder.success(helloWriteBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<HelloWorldReadOutput>> helloWorldRead(HelloWorldReadInput input) {
        final ReadWriteTransaction tx = db.newReadWriteTransaction();

        Future<Optional<HelloWorld>> readFuture =
                tx.read(LogicalDatastoreType.OPERATIONAL, HELLO_IID);

        HelloWorldReadOutputBuilder helloReadBuilder = new HelloWorldReadOutputBuilder();
        try {
            helloReadBuilder.setStrout(input.getStrin() + ", " + readFuture.get().get().getValue());
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("[labry]Exception: ", e);
            e.printStackTrace();
        }

        tx.put(LogicalDatastoreType.CONFIGURATION, HELLO_IID,
                new HelloWorldBuilder().setCounter(++helloCounter).build());
        try {
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("[labry]Exception: ", e);
            e.printStackTrace();
        }
        LOG.info("[labry]helloCount(read): " + helloCounter);

        return RpcResultBuilder.success(helloReadBuilder.build()).buildFuture();
    }


    @Override
    public Future<RpcResult<Void>> noinputOutput() {

        LOG.info("noinputOutput called!");
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }


    @Override
    public Future<RpcResult<HelloWorldOutput>> helloWorld(HelloWorldInput input) {
        HelloWorldOutputBuilder outputBuilder = new HelloWorldOutputBuilder();
        outputBuilder.setGreating("Hello, " + input.getStrin());
        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
    }

//    @Override
//    public void onDataChanged(
//            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> arg0) {
//        DataObject dataObject = arg0.getUpdatedSubtree();
//        if (dataObject instanceof HelloWorld) {
//            HelloWorld helloWorld = (HelloWorld) dataObject;
//            Long helloCount = helloWorld.getCounter();
//            if (helloCount != null) {
//                LOG.info("[labry]onDataChanged - HelloWorldImpl: " + helloCounter);
//            }
//
//
//            if((helloCount % 10L) == 0L) {
//                MultipleOfTens multipleOfTensNotification = new MultipleOfTensBuilder().build();
//                notificationservice.offerNotification(multipleOfTensNotification);
//            }
//
//        }
//
//    }

    @Override
    public void onMultipleOfTens(MultipleOfTens notification) {
        LOG.info("on Multiple of Tens.");
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<HelloWorld>> changes) {

        for (DataTreeModification<HelloWorld> change : changes) {

            DataObjectModification<HelloWorld> rootNode = change.getRootNode();
            Long helloCount = rootNode.getDataAfter().getCounter();
            if (helloCount != null) {
                LOG.info("[labry]onDataChanged - HelloWorldImpl: " + helloCounter);
            }

            if ((helloCount % 10L) == 0L) {
                MultipleOfTens multipleOfTensNotification = new MultipleOfTensBuilder().build();
                notificationservice.offerNotification(multipleOfTensNotification);
            }
        }
    }
}
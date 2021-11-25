package io.qiot.manufacturing.edge.machinery.service.production;

import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.enterprise.context.ApplicationScoped;

import io.qiot.manufacturing.all.commons.domain.production.ItemDTO;
import io.qiot.manufacturing.all.commons.domain.production.ProductionChainStageEnum;

/**
 * @author andreabattaglia
 *
 */
@ApplicationScoped
public class ConveyorBeltServiceImpl implements ConveyorBeltService {

    // TSHIRT,COLOR,DRAWING,PACKAGING
    private final Queue<ItemDTO> weavingQueue;
    private final Map<Integer, ItemDTO> weavingUnderValidation;
    private final Queue<ItemDTO> coloringQueue;
    private final Map<Integer, ItemDTO> coloringUnderValidation;
    private final Queue<ItemDTO> printingQueue;
    private final Map<Integer, ItemDTO> printingUnderValidation;
    private final Queue<ItemDTO> packagingQueue;
    private final Map<Integer, ItemDTO> packagingUnderValidation;

    public ConveyorBeltServiceImpl() {

        this.weavingQueue = new ConcurrentLinkedQueue<ItemDTO>();
        weavingUnderValidation = Collections.synchronizedMap(new TreeMap<>());

        this.coloringQueue = new ConcurrentLinkedQueue<ItemDTO>();
        coloringUnderValidation = Collections.synchronizedMap(new TreeMap<>());

        this.printingQueue = new ConcurrentLinkedQueue<ItemDTO>();
        printingUnderValidation = Collections.synchronizedMap(new TreeMap<>());

        this.packagingQueue = new ConcurrentLinkedQueue<ItemDTO>();
        packagingUnderValidation = Collections.synchronizedMap(new TreeMap<>());
    }

    public void createNewItem(UUID productLineId, int itemId) {
        ItemDTO item = new ItemDTO();
        item.id = itemId;
        item.productLineId = productLineId;
        // item.stage=ProductionChainStageEnum.WEAVING;
        weavingQueue.offer(item);
    }

    @Override
    public boolean isValidItem(UUID productLineId, int itemId,
            ProductionChainStageEnum stage) {
        switch (stage) {
        case WEAVING:
            return weavingUnderValidation.containsKey(itemId);
        case COLORING:
            return coloringUnderValidation.containsKey(itemId);
        case PRINTING:
            return printingUnderValidation.containsKey(itemId);
        case PACKAGING:
            return packagingUnderValidation.containsKey(itemId);
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }

    @Override
    public ItemDTO nextItemInQueue(ProductionChainStageEnum stage) {
        switch (stage) {
        case WEAVING:
            return weavingQueue.poll();
        case COLORING:
            return coloringQueue.poll();
        case PRINTING:
            return printingQueue.poll();
        case PACKAGING:
            return packagingQueue.poll();
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }

    @Override
    public void moveToWaitingQueue(ItemDTO item) {
        switch (item.stage) {
        case WEAVING:
            weavingUnderValidation.put(item.id, item);
            break;
        case COLORING:
            coloringUnderValidation.put(item.id, item);
            break;
        case PRINTING:
            printingUnderValidation.put(item.id, item);
            break;
        case PACKAGING:
            packagingUnderValidation.put(item.id, item);
            break;
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }

    @Override
    public ItemDTO dropItem(int itemId, ProductionChainStageEnum stage) {
        switch (stage) {
        case WEAVING:
            return weavingUnderValidation.remove(itemId);
        case COLORING:
            return coloringUnderValidation.remove(itemId);
        case PRINTING:
            return printingUnderValidation.remove(itemId);
        case PACKAGING:
            return packagingUnderValidation.remove(itemId);
        default:
            throw new RuntimeException("Not yet implemented");
        }
    }

    @Override
    public ItemDTO moveToNextStage(int itemId, ProductionChainStageEnum stage) {
        ItemDTO item = null;
        switch (stage) {
        case WEAVING:
            item = weavingUnderValidation.remove(itemId);
            coloringQueue.offer(item);
            break;
        case COLORING:
            item = coloringUnderValidation.remove(itemId);
            printingQueue.offer(item);
            break;
        case PRINTING:
            item = printingUnderValidation.remove(itemId);
            packagingQueue.offer(item);
            break;
        case PACKAGING:
            item = packagingUnderValidation.remove(itemId);
            break;
        default:
            throw new RuntimeException("Not yet implemented");
        }
        return item;
    }
}

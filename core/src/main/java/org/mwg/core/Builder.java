package org.mwg.core;

import org.mwg.GraphBuilder;
import org.mwg.core.chunk.ChunkSpace;
import org.mwg.core.chunk.heap.HeapChunkSpace;
import org.mwg.core.chunk.offheap.OffHeapChunkSpace;
import org.mwg.plugin.NodeFactory;
import org.mwg.plugin.Scheduler;
import org.mwg.plugin.Storage;

public class Builder implements GraphBuilder.InternalBuilder {

    @Override
    public org.mwg.Graph newGraph(Storage p_storage, Scheduler p_scheduler, NodeFactory[] p_factories, boolean p_usingGC, boolean p_usingOffHeapMemory, long p_memorySize, long p_autoSaveSize) {
        Storage storage = p_storage;
        if (storage == null) {
            storage = new NoopStorage();
        }
        Scheduler scheduler = p_scheduler;
        if (scheduler == null) {
            scheduler = new NoopScheduler();
        }
        NodeTracker nodeTracker;
        if (p_usingGC) {
            throw new RuntimeException("Not implemented yet !!!");
        } else {
            nodeTracker = new NoopNodeTracker();
        }
        ChunkSpace space;
        long memorySize = p_memorySize;
        if (memorySize == -1) {
            memorySize = 100_000;
        }
        long autoSaveSize = p_autoSaveSize;
        if (p_autoSaveSize == -1) {
            autoSaveSize = memorySize;
        }
        if (p_usingOffHeapMemory) {
            space = new OffHeapChunkSpace(memorySize, autoSaveSize);
        } else {
            space = new HeapChunkSpace((int) memorySize, (int) autoSaveSize);
        }
        org.mwg.core.CoreGraph graph = new org.mwg.core.CoreGraph(storage, space, scheduler, new MWGResolver(storage, space, nodeTracker, scheduler), p_factories);
        if (p_usingOffHeapMemory) {
            graph.offHeapBuffer = true;
        }
        return graph;
    }
}

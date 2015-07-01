package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.tree.KLongTree;
import sun.misc.Unsafe;

/**
 * @ignore ts
 */
public class OffHeapLongTree extends AbstractOffHeapTree implements KLongTree, KOffHeapMemoryElement {

    private static final Unsafe UNSAFE = getUnsafe();

    public OffHeapLongTree() {
    }

    @Override
    public int getNodeSize() {
        return 5;
    }

    public synchronized void insert(long key) {

        if ((size() + 1) > _threshold) {
            int length = (size() == 0 ? 1 : size() << 1);

            int size_base_segment = internal_size_base_segment();
            int size_raw_segment = length * getNodeSize() * 8;
            _start_address = UNSAFE.reallocateMemory(_start_address, size_base_segment + size_raw_segment);

            _threshold = (int) (length * _loadFactor);
        }

        //long insertedNode = key;//size() * SIZE_NODE;
        long insertedNodeIndex = size();

        if (size() == 0) {
            UNSAFE.putInt(internal_ptr_size(), 1);

            setKey(insertedNodeIndex, key);
            setColor(insertedNodeIndex, 0);
            setLeft(insertedNodeIndex, -1);
            setRight(insertedNodeIndex, -1);
            setParent(insertedNodeIndex, -1);

            UNSAFE.putLong(internal_ptr_root_index(), insertedNodeIndex);
        } else {
            long rootIndex = UNSAFE.getLong(internal_ptr_root_index());
            while (true) {
                if (key == key(rootIndex)) {
                    //nop _size
                    return;
                } else if (key < key(rootIndex)) {
                    if (left(rootIndex) == -1) {

                        setKey(insertedNodeIndex, key);
                        setColor(insertedNodeIndex, 0);
                        setLeft(insertedNodeIndex, -1);
                        setRight(insertedNodeIndex, -1);
                        setParent(insertedNodeIndex, -1);

                        setLeft(rootIndex, insertedNodeIndex);

                        UNSAFE.putInt(internal_ptr_size(), size() + 1);
                        break;
                    } else {
                        rootIndex = left(rootIndex);
                    }
                } else {
                    if (right(rootIndex) == -1) {

                        setKey(insertedNodeIndex, key);
                        setColor(insertedNodeIndex, 0);
                        setLeft(insertedNodeIndex, -1);
                        setRight(insertedNodeIndex, -1);
                        setParent(insertedNodeIndex, -1);

                        setRight(rootIndex, insertedNodeIndex);

                        UNSAFE.putInt(internal_ptr_size(), size() + 1);
                        break;
                    } else {
                        rootIndex = right(rootIndex);
                    }
                }
            }

            setParent(insertedNodeIndex, rootIndex);
        }
        insertCase1(insertedNodeIndex);
    }

    @Override
    public long previousOrEqual(long key) {
        long result = internal_previousOrEqual_index(key);
        if (result != -1) {
            return key(result);
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long getMemoryAddress() {
        return _start_address;
    }

    @Override
    public void setMemoryAddress(long address) {
        _start_address = address;

        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (size() * _loadFactor);
    }
}

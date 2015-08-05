package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.format.json.JsonObjectReader;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.util.Checker;
import org.kevoree.modeling.util.maths.Base64;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class HeapObjectChunk implements KObjectChunk {

    private final KChunkSpace _space;

    private final AtomicLong _flags;

    private final AtomicInteger _counter;

    private final long _universe;

    private final long _time;

    private final long _obj;

    private Object[] raw;

    private int _metaClassIndex = -1;

    public HeapObjectChunk(long p_universe, long p_time, long p_obj, KChunkSpace p_space) {
        this._universe = p_universe;
        this._time = p_time;
        this._obj = p_obj;
        this._flags = new AtomicLong(0);
        this._counter = new AtomicInteger(0);
        this._space = p_space;
    }

    @Override
    public KChunkSpace space() {
        return _space;
    }

    @Override
    public int metaClassIndex() {
        return _metaClassIndex;
    }

    @Override
    public String serialize(KMetaModel metaModel) {
        KMetaClass metaClass = metaModel.metaClass(_metaClassIndex);
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean isFirst = true;
        KMeta[] metaElements = metaClass.metaElements();
        if (raw != null && metaElements != null) {
            for (int i = 0; i < raw.length && i < metaElements.length; i++) {
                if (raw[i] != null) {
                    if (isFirst) {
                        builder.append("\"");
                        isFirst = false;
                    } else {
                        builder.append(",\"");
                    }
                    builder.append(metaElements[i].metaName());
                    builder.append("\":");
                    if (metaElements[i].metaType() == MetaType.ATTRIBUTE) {
                        KMetaAttribute metaAttribute = (KMetaAttribute) metaElements[i];
                        int metaAttId = metaAttribute.attributeType().id();
                        switch (metaAttId) {
                            case KPrimitiveTypes.STRING_ID:
                                builder.append("\"");
                                builder.append(JsonString.encode((String) raw[i]));
                                builder.append("\"");
                                break;
                            case KPrimitiveTypes.LONG_ID:
                                builder.append("\"");
                                Base64.encodeLongToBuffer((long) raw[i], builder);
                                builder.append("\"");
                                break;
                            case KPrimitiveTypes.CONTINUOUS_ID:
                                doubleArrayToBuffer(builder, i, true);
                                break;
                            case KPrimitiveTypes.BOOL_ID:
                                if ((boolean) raw[i]) {
                                    builder.append("1");
                                } else {
                                    builder.append("0");
                                }
                                break;
                            case KPrimitiveTypes.DOUBLE_ID:
                                builder.append("\"");
                                Base64.encodeDoubleToBuffer((double) raw[i], builder);
                                builder.append("\"");
                                break;
                            case KPrimitiveTypes.INT_ID:
                                builder.append("\"");
                                Base64.encodeIntToBuffer((int) raw[i], builder);
                                builder.append("\"");
                                break;
                            default:
                                if (metaAttribute.attributeType().isEnum()) {
                                    Base64.encodeIntToBuffer((int) raw[i], builder);
                                }
                                break;
                        }
                    } else if (metaElements[i].metaType() == MetaType.REFERENCE) {
                        longArrayToBuffer(builder, i, true);
                    } else if (metaElements[i].metaType() == MetaType.DEPENDENCIES || metaElements[i].metaType() == MetaType.INPUT || metaElements[i].metaType() == MetaType.OUTPUT) {
                        doubleArrayToBuffer(builder, i, true);
                    }
                }
            }
        }
        builder.append("}");
        return builder.toString();
    }

    private void doubleArrayToBuffer(StringBuilder builder, int i, boolean encoded) {
        builder.append("[");
        double[] castedArr = (double[]) raw[i];
        for (int j = 0; j < castedArr.length; j++) {
            if (j != 0) {
                builder.append(",");
            }
            if(encoded){
                builder.append("\"");
                Base64.encodeDoubleToBuffer(castedArr[j], builder);
                builder.append("\"");
            } else {
                builder.append(castedArr[j]);
            }
        }
        builder.append("]");
    }

    private void longArrayToBuffer(StringBuilder builder, int i, boolean encoded) {
        builder.append("[");
        long[] castedArr = (long[]) raw[i];
        for (int j = 0; j < castedArr.length; j++) {
            if (j != 0) {
                builder.append(",");
            }
            if(encoded){
                builder.append("\"");
                Base64.encodeLongToBuffer(castedArr[j], builder);
                builder.append("\"");
            } else {
                builder.append(castedArr[j]);
            }
        }
        builder.append("]");
    }

    @Override
    public void init(String payload, KMetaModel metaModel, int metaClassIndex) {
        this._metaClassIndex = metaClassIndex;
        this.raw = new Object[metaModel.metaClass(metaClassIndex).metaElements().length];
        if (payload != null) {
            JsonObjectReader objectReader = new JsonObjectReader();
            objectReader.parseObject(payload);
            KMetaClass metaClass = metaModel.metaClass(_metaClassIndex);
            String[] metaKeys = objectReader.keys();
            for (int i = 0; i < metaKeys.length; i++) {
                Object insideContent = objectReader.get(metaKeys[i]);
                KMeta metaElement = metaClass.metaByName(metaKeys[i]);
                if (insideContent != null) {
                    if (metaElement != null && metaElement.metaType().equals(MetaType.ATTRIBUTE)) {
                        KMetaAttribute metaAttribute = (KMetaAttribute) metaElement;
                        Object converted = null;
                        int metaAttId = metaAttribute.attributeType().id();
                        switch (metaAttId) {
                            case KPrimitiveTypes.STRING_ID:
                                converted = JsonString.unescape((String) insideContent);
                                break;
                            case KPrimitiveTypes.LONG_ID:
                                converted = Base64.decodeToLong((String) insideContent);
                                break;
                            case KPrimitiveTypes.INT_ID:
                                converted = Base64.decodeToInt((String) insideContent);
                                break;
                            case KPrimitiveTypes.BOOL_ID:
                                if (insideContent.equals("1")) {
                                    converted = true;
                                } else {
                                    converted = false;
                                }
                                break;
                            case KPrimitiveTypes.DOUBLE_ID:
                                converted = Base64.decodeToDouble((String) insideContent);
                                break;
                            case KPrimitiveTypes.CONTINUOUS_ID:
                                String[] plainRawSet = objectReader.getAsStringArray(metaKeys[i]);
                                double[] convertedRaw = new double[plainRawSet.length];
                                for (int l = 0; l < plainRawSet.length; l++) {
                                    try {
                                        convertedRaw[l] = Base64.decodeToDouble(plainRawSet[l]);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                converted = convertedRaw;
                                break;
                        }
                        raw[metaAttribute.index()] = converted;
                    }
                    if (metaElement != null && metaElement.metaType().equals(MetaType.REFERENCE)) {
                        try {
                            String[] plainRawSet = objectReader.getAsStringArray(metaKeys[i]);
                            long[] convertedRaw = new long[plainRawSet.length];
                            for (int l = 0; l < plainRawSet.length; l++) {
                                try {
                                    convertedRaw[l] = Base64.decodeToLong(plainRawSet[l]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            raw[metaElement.index()] = convertedRaw;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (metaElement != null && (metaElement.metaType().equals(MetaType.DEPENDENCIES) || metaElement.metaType().equals(MetaType.INPUT) || metaElement.metaType().equals(MetaType.OUTPUT))) {
                        try {
                            String[] plainRawSet = objectReader.getAsStringArray(metaKeys[i]);
                            double[] convertedRaw = new double[plainRawSet.length];
                            for (int l = 0; l < plainRawSet.length; l++) {
                                try {
                                    convertedRaw[l] = Base64.decodeToDouble(plainRawSet[l]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            raw[metaElement.index()] = convertedRaw;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public final int counter() {
        return this._counter.get();
    }

    @Override
    public final int inc() {
        return this._counter.incrementAndGet();
    }

    @Override
    public final int dec() {
        return this._counter.decrementAndGet();
    }

    @Override
    public void free(KMetaModel metaModel) {
        raw = null;
    }

    @Override
    public short type() {
        return KChunkTypes.OBJECT_CHUNK;
    }

    @Override
    public Object getPrimitiveType(int index, KMetaClass p_metaClass) {
        if (raw != null) {
            return raw[index];
        } else {
            return null;
        }
    }

    @Override
    public int getLongArraySize(int index, KMetaClass metaClass) {
        long[] existing = (long[]) raw[index];
        if (existing != null) {
            return existing.length;
        }
        return 0;
    }

    @Override
    public long getLongArrayElem(int index, int refIndex, KMetaClass metaClass) {
        long[] existing = (long[]) raw[index];
        if (existing != null) {
            return existing[refIndex];
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long[] getLongArray(int index, KMetaClass p_metaClass) {
        if (raw != null) {
            Object previousObj = raw[index];
            if (previousObj != null) {
                try {
                    return (long[]) previousObj;
                } catch (Exception e) {
                    e.printStackTrace();
                    raw[index] = null;
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean addLongToArray(int index, long newRef, KMetaClass metaClass) {
        if (raw != null) {
            long[] previous = (long[]) raw[index];
            if (previous == null) {
                previous = new long[1];
                previous[0] = newRef;
            } else {
                for (int i = 0; i < previous.length; i++) {
                    if (previous[i] == newRef) {
                        return false;
                    }
                }
                long[] incArray = new long[previous.length + 1];
                System.arraycopy(previous, 0, incArray, 0, previous.length);
                incArray[previous.length] = newRef;
                previous = incArray;
            }
            raw[index] = previous;
            internal_set_dirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeLongToArray(int index, long refToRemove, KMetaClass metaClass) {
        if (raw != null) {
            long[] previous = (long[]) raw[index];
            if (previous != null) {
                int indexToRemove = -1;
                for (int i = 0; i < previous.length; i++) {
                    if (previous[i] == refToRemove) {
                        indexToRemove = i;
                        break;
                    }
                }
                if (indexToRemove != -1) {
                    if ((previous.length - 1) == 0) {
                        raw[index] = null;
                    } else {
                        long[] newArray = new long[previous.length - 1];
                        System.arraycopy(previous, 0, newArray, 0, indexToRemove);
                        System.arraycopy(previous, indexToRemove + 1, newArray, indexToRemove, previous.length - indexToRemove - 1);
                        raw[index] = newArray;
                    }
                    internal_set_dirty();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void clearLongArray(int index, KMetaClass metaClass) {
        raw[index] = null;
    }

    @Override
    public double[] getDoubleArray(int index, KMetaClass metaClass) {
        if (raw != null) {
            Object previousObj = raw[index];
            if (previousObj != null) {
                try {
                    return (double[]) previousObj;
                } catch (Exception e) {
                    e.printStackTrace();
                    raw[index] = null;
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public int getDoubleArraySize(int index, KMetaClass metaClass) {
        Object previousObj = raw[index];
        if (previousObj != null) {
            return ((double[]) previousObj).length;
        }
        return 0;
    }

    @Override
    public double getDoubleArrayElem(int index, int arrayIndex, KMetaClass metaClass) {
        double[] res = getDoubleArray(index, metaClass);
        if (Checker.isDefined(res)) {
            return res[arrayIndex];
        }
        return 0;
    }

    @Override
    public void setDoubleArrayElem(int index, int arrayIndex, double valueToInsert, KMetaClass metaClass) {
        double[] res = getDoubleArray(index, metaClass);
        if (Checker.isDefined(res)) {
            res[arrayIndex] = valueToInsert;
            internal_set_dirty();
        }
    }

    @Override
    public void extendDoubleArray(int index, int newSize, KMetaClass metaClass) {
        if (raw != null) {
            double[] previous = (double[]) raw[index];
            if (previous == null) {
                previous = new double[newSize];
            } else {
                double[] incArray = new double[newSize];
                System.arraycopy(previous, 0, incArray, 0, previous.length);
                previous = incArray;
            }
            raw[index] = previous;
            internal_set_dirty();
        }
    }

    @Override
    public synchronized void setPrimitiveType(int index, Object content, KMetaClass p_metaClass) {
        raw[index] = content;
        internal_set_dirty();
    }

    @Override
    public KObjectChunk clone(long p_universe, long p_time, long p_obj, KMetaModel p_metaClass) {
        if (raw == null) {
            return new HeapObjectChunk(p_universe, p_time, p_obj, _space);
        } else {
            Object[] cloned = new Object[raw.length];
            System.arraycopy(raw, 0, cloned, 0, raw.length);
            HeapObjectChunk clonedEntry = new HeapObjectChunk(p_universe, p_time, p_obj, _space);
            clonedEntry.raw = cloned;
            clonedEntry._metaClassIndex = _metaClassIndex;
            clonedEntry.internal_set_dirty();
            return clonedEntry;
        }
    }

    @Override
    public String toJSON(KMetaModel metaModel) {
        KMetaClass metaClass = metaModel.metaClass(_metaClassIndex);
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean isFirst = true;
        KMeta[] metaElements = metaClass.metaElements();
        if (raw != null && metaElements != null) {
            for (int i = 0; i < raw.length && i < metaElements.length; i++) {
                if (raw[i] != null) {
                    if (isFirst) {
                        builder.append("\"");
                        isFirst = false;
                    } else {
                        builder.append(",\"");
                    }
                    builder.append(metaElements[i].metaName());
                    builder.append("\":");
                    if (metaElements[i].metaType() == MetaType.ATTRIBUTE) {
                        KMetaAttribute metaAttribute = (KMetaAttribute) metaElements[i];
                        int metaAttId = metaAttribute.attributeType().id();
                        switch (metaAttId) {
                            case KPrimitiveTypes.STRING_ID:
                                builder.append("\"");
                                builder.append(JsonString.encode((String) raw[i]));
                                builder.append("\"");
                                break;
                            case KPrimitiveTypes.LONG_ID:
                                builder.append(raw[i]);
                                break;
                            case KPrimitiveTypes.CONTINUOUS_ID:
                                doubleArrayToBuffer(builder, i, false);
                                break;
                            case KPrimitiveTypes.BOOL_ID:
                                if ((boolean) raw[i]) {
                                    builder.append("1");
                                } else {
                                    builder.append("0");
                                }
                                break;
                            case KPrimitiveTypes.DOUBLE_ID:
                                builder.append(raw[i]);
                                break;
                            case KPrimitiveTypes.INT_ID:
                                builder.append(raw[i]);
                                break;
                            default:
                                if (metaAttribute.attributeType().isEnum()) {
                                    Base64.encodeIntToBuffer((int) raw[i], builder);
                                }
                                break;
                        }
                    } else if (metaElements[i].metaType() == MetaType.REFERENCE) {
                        longArrayToBuffer(builder, i, false);
                    } else if (metaElements[i].metaType() == MetaType.DEPENDENCIES || metaElements[i].metaType() == MetaType.INPUT || metaElements[i].metaType() == MetaType.OUTPUT) {
                        doubleArrayToBuffer(builder, i, false);
                    }
                }
            }
        }
        builder.append("}");
        return builder.toString();
    }

    private void internal_set_dirty() {
        if (_space != null) {
            if ((_flags.get() & KChunkFlags.DIRTY_BIT) != KChunkFlags.DIRTY_BIT) {
                _space.declareDirty(this);
                //the synchronization risk is minim here, at worse the object will be saved twice for the next iteration
                setFlags(KChunkFlags.DIRTY_BIT, 0);
            }
        } else {
            setFlags(KChunkFlags.DIRTY_BIT, 0);
        }
    }

    @Override
    public long getFlags() {
        return _flags.get();
    }

    @Override
    public void setFlags(long bitsToEnable, long bitsToDisable) {
        long val;
        long nval;
        do {
            val = _flags.get();
            nval = val & ~bitsToDisable | bitsToEnable;
        } while (!_flags.compareAndSet(val, nval));
    }

    @Override
    public long universe() {
        return this._universe;
    }

    @Override
    public long time() {
        return this._time;
    }

    @Override
    public long obj() {
        return this._obj;
    }

}